import { Request, Response } from "express";
import { prisma } from "../../libs/prisma";
import { parsePaging, toSkipTake } from "../../utils/paging";
import { UpdateUserDto, UpdateSelfDto, UpdateStatusDto } from "./users.dto";
import { AppError } from "../../utils/errors";

/**
 * GET /users - Lấy danh sách users (Admin)
 * Query: role, status, search, page, pageSize
 */
export async function list(req: Request, res: Response) {
  const { role, status, search } = req.query;
  const paging = parsePaging(req.query);
  const { skip, take } = toSkipTake(paging);

  const where: any = {};

  if (role && ["member", "trainer", "admin", "receptionist"].includes(String(role))) {
    where.role = role;
  }

  if (status && ["active", "inactive", "banned"].includes(String(status))) {
    where.status = status;
  }

  // Tìm kiếm theo tên, email, hoặc phone
  if (search) {
    where.OR = [
      { fullName: { contains: String(search) } },
      { email: { contains: String(search) } },
      { phone: { contains: String(search) } }
    ];
  }

  const [items, total] = await Promise.all([
    prisma.user.findMany({
      where,
      orderBy: { createdAt: "desc" },
      skip,
      take,
      select: {
        id: true,
        email: true,
        phone: true,
        fullName: true,
        role: true,
        status: true,
        createdAt: true,
        _count: {
          select: {
            memberships: true,
            bookings: true,
            checkins: true
          }
        }
      }
    }),
    prisma.user.count({ where })
  ]);

  res.json({ items, total, page: paging.page, pageSize: paging.pageSize });
}

/**
 * GET /users/:id - Lấy chi tiết user (Admin)
 */
export async function getById(req: Request, res: Response) {
  const id = BigInt(req.params.id);

  const user = await prisma.user.findUnique({
    where: { id },
    select: {
      id: true,
      email: true,
      phone: true,
      fullName: true,
      role: true,
      status: true,
      createdAt: true,
      updatedAt: true,
      memberships: {
        orderBy: { createdAt: "desc" },
        take: 5,
        include: { plan: true }
      },
      _count: {
        select: {
          memberships: true,
          bookings: true,
          checkins: true,
          classesTaught: true
        }
      }
    }
  });

  if (!user) {
    throw new AppError("USER_NOT_FOUND", 404, "Không tìm thấy người dùng");
  }

  res.json(user);
}

/**
 * PUT /users/:id - Cập nhật thông tin user (Admin)
 * Body: { fullName?, email?, phone?, role? }
 */
export async function update(req: Request, res: Response) {
  const id = BigInt(req.params.id);

  const parse = UpdateUserDto.safeParse(req.body);
  if (!parse.success) {
    return res.status(400).json({
      code: "VALIDATION_ERROR",
      errors: parse.error.flatten()
    });
  }

  const user = await prisma.user.findUnique({ where: { id } });
  if (!user) {
    throw new AppError("USER_NOT_FOUND", 404, "Không tìm thấy người dùng");
  }

  // Kiểm tra email/phone trùng lặp nếu có thay đổi
  if (parse.data.email && parse.data.email !== user.email) {
    const duplicate = await prisma.user.findFirst({
      where: { email: parse.data.email, id: { not: id } }
    });
    if (duplicate) {
      throw new AppError("EMAIL_EXISTS", 409, "Email đã được sử dụng");
    }
  }

  if (parse.data.phone && parse.data.phone !== user.phone) {
    const duplicate = await prisma.user.findFirst({
      where: { phone: parse.data.phone, id: { not: id } }
    });
    if (duplicate) {
      throw new AppError("PHONE_EXISTS", 409, "Số điện thoại đã được sử dụng");
    }
  }

  const updated = await prisma.user.update({
    where: { id },
    data: parse.data,
    select: {
      id: true,
      email: true,
      phone: true,
      fullName: true,
      role: true,
      status: true
    }
  });

  res.json(updated);
}

/**
 * PUT /users/:id/status - Cập nhật trạng thái user (Admin)
 * Body: { status: "active" | "inactive" | "banned" }
 */
export async function updateStatus(req: Request, res: Response) {
  const id = BigInt(req.params.id);

  const parse = UpdateStatusDto.safeParse(req.body);
  if (!parse.success) {
    return res.status(400).json({
      code: "VALIDATION_ERROR",
      errors: parse.error.flatten()
    });
  }

  const user = await prisma.user.findUnique({ where: { id } });
  if (!user) {
    throw new AppError("USER_NOT_FOUND", 404, "Không tìm thấy người dùng");
  }

  // Không cho phép ban admin
  if (parse.data.status === "banned" && user.role === "admin") {
    throw new AppError("CANNOT_BAN_ADMIN", 403, "Không thể khóa tài khoản admin");
  }

  const updated = await prisma.user.update({
    where: { id },
    data: { status: parse.data.status },
    select: {
      id: true,
      fullName: true,
      status: true
    }
  });

  res.json(updated);
}

/**
 * PUT /users/me/profile - User tự cập nhật thông tin của mình
 * Body: { fullName?, email?, phone? }
 */
export async function updateSelf(req: Request & { user?: any }, res: Response) {
  const id = BigInt(req.user!.id);

  const parse = UpdateSelfDto.safeParse(req.body);
  if (!parse.success) {
    return res.status(400).json({
      code: "VALIDATION_ERROR",
      errors: parse.error.flatten()
    });
  }

  const user = await prisma.user.findUnique({ where: { id } });
  if (!user) {
    throw new AppError("USER_NOT_FOUND", 404, "Không tìm thấy người dùng");
  }

  // Kiểm tra trùng lặp
  if (parse.data.email && parse.data.email !== user.email) {
    const duplicate = await prisma.user.findFirst({
      where: { email: parse.data.email, id: { not: id } }
    });
    if (duplicate) {
      throw new AppError("EMAIL_EXISTS", 409, "Email đã được sử dụng");
    }
  }

  if (parse.data.phone && parse.data.phone !== user.phone) {
    const duplicate = await prisma.user.findFirst({
      where: { phone: parse.data.phone, id: { not: id } }
    });
    if (duplicate) {
      throw new AppError("PHONE_EXISTS", 409, "Số điện thoại đã được sử dụng");
    }
  }

  const updated = await prisma.user.update({
    where: { id },
    data: parse.data,
    select: {
      id: true,
      email: true,
      phone: true,
      fullName: true,
      role: true,
      status: true
    }
  });

  res.json(updated);
}