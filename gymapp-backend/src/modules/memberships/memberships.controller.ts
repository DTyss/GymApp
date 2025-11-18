import { Request, Response } from "express";
import { prisma } from "../../libs/prisma";
import { parsePaging, toSkipTake } from "../../utils/paging";
import { CreateMembershipDto, ExtendMembershipDto } from "./memberships.dto";
import { AppError, asyncHandler } from "../../utils/errors";

/**
 * GET /memberships - Lấy danh sách memberships (Admin/Receptionist)
 * Query: userId, status, page, pageSize
 */
export const list = asyncHandler(async (req: Request, res: Response) => {
  const { userId, status } = req.query;
  const paging = parsePaging(req.query);
  const { skip, take } = toSkipTake(paging);

  const where: any = {};

  if (userId) where.userId = BigInt(String(userId));
  
  if (status && ["active", "expired", "paused"].includes(String(status))) {
    where.status = status;
  }

  const [items, total] = await Promise.all([
    prisma.membership.findMany({
      where,
      orderBy: { createdAt: "desc" },
      skip,
      take,
      include: {
        user: {
          select: { id: true, fullName: true, email: true, phone: true }
        },
        plan: true
      }
    }),
    prisma.membership.count({ where })
  ]);

  res.json({ items, total, page: paging.page, pageSize: paging.pageSize });
});

/**
 * GET /memberships/:id - Lấy chi tiết membership
 */
export const getById = asyncHandler(async (req: Request, res: Response) => {
  const id = BigInt(req.params.id);

  const membership = await prisma.membership.findUnique({
    where: { id },
    include: {
      user: {
        select: { id: true, fullName: true, email: true, phone: true }
      },
      plan: true
    }
  });

  if (!membership) {
    throw new AppError("MEMBERSHIP_NOT_FOUND", 404, "Không tìm thấy gói tập");
  }

  res.json(membership);
});

/**
 * POST /memberships - Tạo membership mới cho user (Admin/Receptionist)
 * Body: { userId, planId, startDate? }
 */
export const create = asyncHandler(async (req: Request, res: Response) => {
  const parse = CreateMembershipDto.safeParse(req.body);
  if (!parse.success) {
    return res.status(400).json({
      code: "VALIDATION_ERROR",
      errors: parse.error.flatten()
    });
  }

  const { userId, planId, startDate } = parse.data;

  // Kiểm tra user tồn tại
  const user = await prisma.user.findUnique({
    where: { id: BigInt(userId) }
  });
  if (!user) {
    throw new AppError("USER_NOT_FOUND", 404, "Không tìm thấy người dùng");
  }

  // Kiểm tra plan tồn tại và active
  const plan = await prisma.plan.findUnique({
    where: { id: BigInt(planId) }
  });
  if (!plan) {
    throw new AppError("PLAN_NOT_FOUND", 404, "Không tìm thấy gói tập");
  }
  if (!plan.isActive) {
    throw new AppError("PLAN_INACTIVE", 400, "Gói tập không còn hoạt động");
  }

  // Tính toán ngày bắt đầu và kết thúc
  const start = startDate ? new Date(startDate) : new Date();
  const end = new Date(start);
  end.setDate(end.getDate() + plan.durationDays);

  const membership = await prisma.membership.create({
    data: {
      userId: BigInt(userId),
      planId: BigInt(planId),
      startDate: start,
      endDate: end,
      remainingSessions: plan.sessions,
      status: "active"
    },
    include: {
      user: { select: { id: true, fullName: true } },
      plan: true
    }
  });

  res.status(201).json(membership);
});

/**
 * PUT /memberships/:id/extend - Gia hạn membership (Admin/Receptionist)
 * Body: { additionalDays?, additionalSessions? }
 */
export const extend = asyncHandler(async (req: Request, res: Response) => {
  const id = BigInt(req.params.id);

  const parse = ExtendMembershipDto.safeParse(req.body);
  if (!parse.success) {
    return res.status(400).json({
      code: "VALIDATION_ERROR",
      errors: parse.error.flatten()
    });
  }

  const membership = await prisma.membership.findUnique({
    where: { id },
    include: { plan: true }
  });

  if (!membership) {
    throw new AppError("MEMBERSHIP_NOT_FOUND", 404, "Không tìm thấy gói tập");
  }

  const data: any = {};

  // Gia hạn thời gian
  if (parse.data.additionalDays) {
    const newEndDate = new Date(membership.endDate);
    newEndDate.setDate(newEndDate.getDate() + parse.data.additionalDays);
    data.endDate = newEndDate;
    
    // Nếu gói đã hết hạn, kích hoạt lại
    if (membership.status === "expired") {
      data.status = "active";
    }
  }

  // Thêm buổi tập
  if (parse.data.additionalSessions) {
    data.remainingSessions = membership.remainingSessions + parse.data.additionalSessions;
  }

  const updated = await prisma.membership.update({
    where: { id },
    data,
    include: {
      user: { select: { id: true, fullName: true } },
      plan: true
    }
  });

  res.json(updated);
});

/**
 * PUT /memberships/:id/pause - Tạm dừng membership (Admin)
 */
export const pause = asyncHandler(async (req: Request, res: Response) => {
  const id = BigInt(req.params.id);

  const membership = await prisma.membership.findUnique({ where: { id } });
  if (!membership) {
    throw new AppError("MEMBERSHIP_NOT_FOUND", 404, "Không tìm thấy gói tập");
  }

  if (membership.status !== "active") {
    throw new AppError("INVALID_STATUS", 400, "Chỉ có thể tạm dừng gói đang active");
  }

  const updated = await prisma.membership.update({
    where: { id },
    data: { status: "paused" }
  });

  res.json(updated);
});

/**
 * PUT /memberships/:id/resume - Kích hoạt lại membership (Admin)
 */
export const resume = asyncHandler(async (req: Request, res: Response) => {
  const id = BigInt(req.params.id);

  const membership = await prisma.membership.findUnique({ where: { id } });
  if (!membership) {
    throw new AppError("MEMBERSHIP_NOT_FOUND", 404, "Không tìm thấy gói tập");
  }

  if (membership.status !== "paused") {
    throw new AppError("INVALID_STATUS", 400, "Chỉ có thể kích hoạt gói đang paused");
  }

  // Kiểm tra còn hạn không
  if (new Date() > membership.endDate) {
    throw new AppError("MEMBERSHIP_EXPIRED", 400, "Gói tập đã hết hạn, vui lòng gia hạn");
  }

  const updated = await prisma.membership.update({
    where: { id },
    data: { status: "active" }
  });

  res.json(updated);
});

/**
 * GET /memberships/my/list - Member xem memberships của mình
 */
export const myMemberships = asyncHandler(async (req: Request & { user?: any }, res: Response) => {
  const userId = BigInt(req.user!.id);

  const memberships = await prisma.membership.findMany({
    where: { userId },
    orderBy: { createdAt: "desc" },
    include: { plan: true }
  });

  res.json(memberships);
});