import { Request, Response } from "express";
import { prisma } from "../../libs/prisma";
import { CreateBranchDto, UpdateBranchDto } from "./branches.dto";
import { AppError } from "../../utils/errors";

export async function list(req: Request, res: Response) {
  const { active } = req.query;
  
  const where: any = {};
  if (active === "true") {
    where.isActive = true;
  }

  const branches = await prisma.branch.findMany({
    where,
    orderBy: { name: "asc" },
    select: {
      id: true,
      name: true,
      address: true,
      isActive: true,
      createdAt: true,
      _count: {
        select: {
          classes: true,
          checkins: true
        }
      }
    }
  });

  res.json(branches);
}

export async function getById(req: Request, res: Response) {
  const id = BigInt(req.params.id);
  
  const branch = await prisma.branch.findUnique({
    where: { id },
    include: {
      _count: {
        select: {
          classes: true,
          checkins: true
        }
      }
    }
  });

  if (!branch) {
    throw new AppError("BRANCH_NOT_FOUND", 404, "Không tìm thấy chi nhánh");
  }

  res.json(branch);
}

export async function create(req: Request, res: Response) {
  const parse = CreateBranchDto.safeParse(req.body);
  if (!parse.success) {
    return res.status(400).json({
      code: "VALIDATION_ERROR",
      errors: parse.error.flatten()
    });
  }

  const { name, address, isActive } = parse.data;

  const existed = await prisma.branch.findFirst({
    where: { name }
  });

  if (existed) {
    throw new AppError("BRANCH_NAME_EXISTS", 409, "Tên chi nhánh đã tồn tại");
  }

  const branch = await prisma.branch.create({
    data: {
      name,
      address,
      isActive: isActive ?? true
    }
  });

  res.status(201).json(branch);
}

export async function update(req: Request, res: Response) {
  const id = BigInt(req.params.id);

  const parse = UpdateBranchDto.safeParse(req.body);
  if (!parse.success) {
    return res.status(400).json({
      code: "VALIDATION_ERROR",
      errors: parse.error.flatten()
    });
  }

  const existed = await prisma.branch.findUnique({ where: { id } });
  if (!existed) {
    throw new AppError("BRANCH_NOT_FOUND", 404, "Không tìm thấy chi nhánh");
  }

  if (parse.data.name && parse.data.name !== existed.name) {
    const duplicate = await prisma.branch.findFirst({
      where: { name: parse.data.name, id: { not: id } }
    });
    if (duplicate) {
      throw new AppError("BRANCH_NAME_EXISTS", 409, "Tên chi nhánh đã tồn tại");
    }
  }

  const updated = await prisma.branch.update({
    where: { id },
    data: parse.data
  });

  res.json(updated);
}

export async function deleteBranch(req: Request, res: Response) {
  const id = BigInt(req.params.id);

  const branch = await prisma.branch.findUnique({
    where: { id },
    include: {
      _count: {
        select: {
          classes: true,
          checkins: true
        }
      }
    }
  });

  if (!branch) {
    throw new AppError("BRANCH_NOT_FOUND", 404, "Không tìm thấy chi nhánh");
  }

  if (branch._count.classes > 0 || branch._count.checkins > 0) {
    throw new AppError(
      "BRANCH_IN_USE",
      409,
      `Không thể xóa. Chi nhánh có ${branch._count.classes} lớp học và ${branch._count.checkins} lượt check-in`
    );
  }

  await prisma.branch.delete({ where: { id } });

  res.json({ ok: true, message: "Đã xóa chi nhánh" });
}