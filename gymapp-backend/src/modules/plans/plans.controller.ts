import { Request, Response } from "express";
import { prisma } from "../../libs/prisma";
import { CreatePlanDto, UpdatePlanDto } from "./plans.dto";
import { AppError } from "../../utils/errors";

/**
 * GET /plans - Lấy danh sách tất cả gói tập
 */
export async function list(req: Request, res: Response) {
  const { active } = req.query;
  
  const where: any = {};
  if (active === "true") {
    where.isActive = true;
  }

  const plans = await prisma.plan.findMany({
    where,
    orderBy: { price: "asc" },
    select: {
      id: true,
      name: true,
      price: true,
      sessions: true,
      durationDays: true,
      isActive: true,
      createdAt: true,
      _count: {
        select: { memberships: true }
      }
    }
  });

  res.json(plans);
}

/**
 * GET /plans/:id - Lấy chi tiết 1 gói tập
 */
export async function getById(req: Request, res: Response) {
  const id = BigInt(req.params.id);
  
  const plan = await prisma.plan.findUnique({
    where: { id },
    include: {
      _count: {
        select: { memberships: true }
      }
    }
  });

  if (!plan) {
    throw new AppError("PLAN_NOT_FOUND", 404, "Không tìm thấy gói tập");
  }

  res.json(plan);
}

/**
 * POST /plans - Tạo gói tập mới (Admin only)
 */
export async function create(req: Request, res: Response) {
  const parse = CreatePlanDto.safeParse(req.body);
  if (!parse.success) {
    return res.status(400).json({
      code: "VALIDATION_ERROR",
      errors: parse.error.flatten()
    });
  }

  const { name, price, sessions, durationDays, isActive } = parse.data;

  const existed = await prisma.plan.findFirst({
    where: { name }
  });

  if (existed) {
    throw new AppError("PLAN_NAME_EXISTS", 409, "Tên gói tập đã tồn tại");
  }

  const plan = await prisma.plan.create({
    data: {
      name,
      price,
      sessions,
      durationDays,
      isActive: isActive ?? true
    }
  });

  res.status(201).json(plan);
}

/**
 * PUT /plans/:id - Cập nhật gói tập (Admin only)
 */
export async function update(req: Request, res: Response) {
  const id = BigInt(req.params.id);

  const parse = UpdatePlanDto.safeParse(req.body);
  if (!parse.success) {
    return res.status(400).json({
      code: "VALIDATION_ERROR",
      errors: parse.error.flatten()
    });
  }

  const existed = await prisma.plan.findUnique({ where: { id } });
  if (!existed) {
    throw new AppError("PLAN_NOT_FOUND", 404, "Không tìm thấy gói tập");
  }

  if (parse.data.name && parse.data.name !== existed.name) {
    const duplicate = await prisma.plan.findFirst({
      where: { name: parse.data.name, id: { not: id } }
    });
    if (duplicate) {
      throw new AppError("PLAN_NAME_EXISTS", 409, "Tên gói tập đã tồn tại");
    }
  }

  const updated = await prisma.plan.update({
    where: { id },
    data: parse.data
  });

  res.json(updated);
}

/**
 * DELETE /plans/:id - Xóa gói tập (Admin only)
 */
export async function deletePlan(req: Request, res: Response) {
  const id = BigInt(req.params.id);

  const plan = await prisma.plan.findUnique({
    where: { id },
    include: {
      _count: { select: { memberships: true } }
    }
  });

  if (!plan) {
    throw new AppError("PLAN_NOT_FOUND", 404, "Không tìm thấy gói tập");
  }

  if (plan._count.memberships > 0) {
    throw new AppError(
      "PLAN_IN_USE",
      409,
      `Không thể xóa. Có ${plan._count.memberships} hội viên đang sử dụng gói này`
    );
  }

  await prisma.plan.delete({ where: { id } });

  res.json({ ok: true, message: "Đã xóa gói tập" });
}