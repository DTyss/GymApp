import { Request, Response } from "express";
import { prisma } from "../../libs/prisma";
import { parsePaging, toSkipTake } from "../../utils/paging";
import { parseSort } from "../../utils/sort";
import { CreateClassDto, UpdateClassDto } from "./classes.dto";
import { AppError } from "../../utils/errors";

/**
 * GET /classes - Lấy danh sách lớp học với pagination & filters
 */
export async function list(req: Request, res: Response) {
  const { from, to, branchId, trainerId } = req.query;
  const paging = parsePaging(req.query);
  const { skip, take } = toSkipTake(paging);
  const { sortBy, sortDir } = parseSort(
    req.query, 
    ["startTime", "title", "capacity"], 
    { sortBy: "startTime", sortDir: "asc" }
  );

  const where: any = {};
  if (branchId) where.branchId = BigInt(String(branchId));
  if (trainerId) where.trainerId = BigInt(String(trainerId));
  if (from && to) {
    where.startTime = { 
      gte: new Date(String(from)), 
      lte: new Date(String(to)) 
    };
  }

  const [itemsRaw, total] = await Promise.all([
    prisma.class.findMany({
      where,
      orderBy: { [sortBy!]: sortDir },
      skip,
      take,
      include: {
        branch: true,
        trainer: { select: { id: true, fullName: true } },
        _count: { select: { bookings: { where: { status: "booked" } } } }
      }
    }),
    prisma.class.count({ where })
  ]);

  // Transform response để tính available slots
  const items = itemsRaw.map((c: any) => ({
    id: c.id,
    title: c.title,
    description: c.description,
    startTime: c.startTime,
    endTime: c.endTime,
    capacity: c.capacity,
    available: c.capacity - c._count.bookings,
    branch: { id: c.branch.id, name: c.branch.name },
    trainer: c.trainer
  }));

  res.json({ items, total, page: paging.page, pageSize: paging.pageSize });
}

/**
 * GET /classes/:id - Lấy chi tiết 1 lớp học
 */
export async function getById(req: Request, res: Response) {
  const id = BigInt(req.params.id);
  
  const cls = await prisma.class.findUnique({
    where: { id },
    include: {
      branch: true,
      trainer: { 
        select: { 
          id: true, 
          fullName: true, 
          email: true, 
          phone: true 
        } 
      },
      bookings: {
        where: { status: "booked" },
        include: {
          user: { select: { id: true, fullName: true } }
        }
      },
      _count: { 
        select: { 
          bookings: { where: { status: "booked" } } 
        } 
      }
    }
  });

  if (!cls) {
    throw new AppError("CLASS_NOT_FOUND", 404, "Không tìm thấy lớp học");
  }

  // Transform response
  const response = {
    ...cls,
    available: cls.capacity - cls._count.bookings
  };

  res.json(response);
}

/**
 * POST /classes - Tạo lớp học mới (Trainer/Admin)
 * Body: { title, description?, trainerId, branchId, startTime, endTime, capacity }
 */
export async function create(req: Request, res: Response) {
  // Validate input với Zod
  const parse = CreateClassDto.safeParse(req.body);
  if (!parse.success) {
    return res.status(400).json({
      code: "VALIDATION_ERROR",
      errors: parse.error.flatten()
    });
  }

  const { title, description, trainerId, branchId, startTime, endTime, capacity } = parse.data;

  // 1. Kiểm tra trainer tồn tại và có role trainer
  const trainer = await prisma.user.findUnique({
    where: { id: BigInt(trainerId) }
  });
  
  if (!trainer) {
    throw new AppError("TRAINER_NOT_FOUND", 404, "Không tìm thấy huấn luyện viên");
  }
  
  if (trainer.role !== "trainer") {
    throw new AppError("INVALID_TRAINER", 400, "User không phải là huấn luyện viên");
  }

  // 2. Kiểm tra branch tồn tại
  const branch = await prisma.branch.findUnique({
    where: { id: BigInt(branchId) }
  });
  
  if (!branch) {
    throw new AppError("BRANCH_NOT_FOUND", 404, "Không tìm thấy chi nhánh");
  }

  // 3. Kiểm tra thời gian hợp lệ
  const start = new Date(startTime);
  const end = new Date(endTime);
  
  if (start >= end) {
    throw new AppError(
      "INVALID_TIME", 
      400, 
      "Thời gian kết thúc phải sau thời gian bắt đầu"
    );
  }

  // 4. Kiểm tra trainer có bị trùng lịch không
  const conflict = await prisma.class.findFirst({
    where: {
      trainerId: BigInt(trainerId),
      OR: [
        // Case 1: Lớp mới bắt đầu trong khoảng lớp cũ
        { 
          startTime: { lte: start }, 
          endTime: { gt: start } 
        },
        // Case 2: Lớp mới kết thúc trong khoảng lớp cũ
        { 
          startTime: { lt: end }, 
          endTime: { gte: end } 
        },
        // Case 3: Lớp mới bao phủ lớp cũ
        { 
          startTime: { gte: start }, 
          endTime: { lte: end } 
        }
      ]
    }
  });

  if (conflict) {
    throw new AppError(
      "TRAINER_BUSY", 
      409, 
      "Huấn luyện viên đã có lịch dạy vào thời gian này"
    );
  }

  // 5. Tạo lớp học
  const cls = await prisma.class.create({
    data: {
      title,
      description,
      trainerId: BigInt(trainerId),
      branchId: BigInt(branchId),
      startTime: start,
      endTime: end,
      capacity
    },
    include: {
      branch: true,
      trainer: { select: { id: true, fullName: true } }
    }
  });

  res.status(201).json(cls);
}

/**
 * PUT /classes/:id - Cập nhật lớp học (Trainer/Admin)
 * Body: { title?, description?, trainerId?, branchId?, startTime?, endTime?, capacity? }
 */
export async function update(req: Request, res: Response) {
  const id = BigInt(req.params.id);

  // Validate input
  const parse = UpdateClassDto.safeParse(req.body);
  if (!parse.success) {
    return res.status(400).json({
      code: "VALIDATION_ERROR",
      errors: parse.error.flatten()
    });
  }

  // 1. Kiểm tra lớp tồn tại
  const existed = await prisma.class.findUnique({
    where: { id },
    include: {
      _count: { 
        select: { 
          bookings: { where: { status: "booked" } } 
        } 
      }
    }
  });

  if (!existed) {
    throw new AppError("CLASS_NOT_FOUND", 404, "Không tìm thấy lớp học");
  }

  const data: any = {};

  // 2. Validate từng field nếu có
  if (parse.data.title) {
    data.title = parse.data.title;
  }
  
  if (parse.data.description !== undefined) {
    data.description = parse.data.description;
  }
  
  // 3. Nếu đổi trainer
  if (parse.data.trainerId) {
    const trainer = await prisma.user.findUnique({
      where: { id: BigInt(parse.data.trainerId) }
    });
    
    if (!trainer || trainer.role !== "trainer") {
      throw new AppError("INVALID_TRAINER", 400, "Trainer không hợp lệ");
    }
    
    data.trainerId = BigInt(parse.data.trainerId);
  }

  // 4. Nếu đổi branch
  if (parse.data.branchId) {
    const branch = await prisma.branch.findUnique({
      where: { id: BigInt(parse.data.branchId) }
    });
    
    if (!branch) {
      throw new AppError("BRANCH_NOT_FOUND", 404, "Chi nhánh không tồn tại");
    }
    
    data.branchId = BigInt(parse.data.branchId);
  }

  // 5. Nếu đổi thời gian
  if (parse.data.startTime) {
    data.startTime = new Date(parse.data.startTime);
  }
  
  if (parse.data.endTime) {
    data.endTime = new Date(parse.data.endTime);
  }

  // 6. Kiểm tra capacity không được nhỏ hơn số booking hiện tại
  if (parse.data.capacity !== undefined) {
    if (parse.data.capacity < existed._count.bookings) {
      throw new AppError(
        "CAPACITY_TOO_SMALL",
        400,
        `Sức chứa không thể nhỏ hơn số booking hiện tại (${existed._count.bookings})`
      );
    }
    data.capacity = parse.data.capacity;
  }

  // 7. Update
  const updated = await prisma.class.update({
    where: { id },
    data,
    include: {
      branch: true,
      trainer: { select: { id: true, fullName: true } }
    }
  });

  res.json(updated);
}

/**
 * DELETE /classes/:id - Xóa lớp học (Admin only)
 * Chỉ xóa được nếu chưa có booking nào
 */
export async function deleteClass(req: Request, res: Response) {
  const id = BigInt(req.params.id);

  // 1. Kiểm tra lớp tồn tại
  const cls = await prisma.class.findUnique({
    where: { id },
    include: {
      _count: { select: { bookings: true } }
    }
  });

  if (!cls) {
    throw new AppError("CLASS_NOT_FOUND", 404, "Không tìm thấy lớp học");
  }

  // 2. Không cho xóa nếu có booking
  if (cls._count.bookings > 0) {
    throw new AppError(
      "CLASS_HAS_BOOKINGS",
      409,
      `Không thể xóa. Lớp có ${cls._count.bookings} booking`
    );
  }

  // 3. Xóa lớp
  await prisma.class.delete({ where: { id } });

  res.json({ 
    ok: true, 
    message: "Đã xóa lớp học thành công" 
  });
}