import { Request, Response } from "express";
import { prisma } from "../../libs/prisma";
import { Prisma } from "@prisma/client";
import { CreateBookingDto } from "./bookings.dto";
import { parsePaging, toSkipTake } from "../../utils/paging";
import { parseSort } from "../../utils/sort";
import { AppError } from "../../utils/errors";

export async function myBookings(req: Request & { user?: any }, res: Response) {
  const userId = BigInt(req.user!.id);
  const paging = parsePaging(req.query);
  const { skip, take } = toSkipTake(paging);
  // cho phép sort theo createdAt hoặc theo thời gian lớp học
  const allow = ["createdAt", "class.startTime"];
  let sortBy = String(req.query.sortBy ?? "createdAt");
  let sortDir: "asc"|"desc" = (String(req.query.sortDir).toLowerCase() === "asc" ? "asc" : "desc");

  // Prisma không hỗ trợ "class.startTime" trực tiếp như string — map thủ công:
  const orderBy = sortBy === "class.startTime"
    ? { class: { startTime: sortDir } } 
    : { createdAt: sortDir };

  const [items, total] = await Promise.all([
    prisma.booking.findMany({
      where: { userId },
      orderBy,
      skip, take,
      include: {
        class: {
          select: { id: true, title: true, startTime: true, endTime: true, branch: true, trainer: { select: { fullName: true } } }
        }
      }
    }),
    prisma.booking.count({ where: { userId } })
  ]);

  res.json({ items, total, page: paging.page, pageSize: paging.pageSize });
}

export async function create(req: Request & { user?: any }, res: Response) {
  // 1) Validate body bằng Zod
  const parse = CreateBookingDto.safeParse(req.body);
  if (!parse.success) {
    return res.status(400).json({
      code: "VALIDATION_ERROR",
      errors: parse.error.flatten() // gồm fieldErrors & formErrors
    });
  }
  const { classId } = parse.data;

  const userId = BigInt(req.user!.id);
  const classIdBig = BigInt(classId);

  try {
    // 2) Transaction đảm bảo kiểm tra slot và tạo booking là nguyên tử
    const result = await prisma.$transaction(async (tx: Prisma.TransactionClient) => {
      // (a) membership hợp lệ?
      const mem = await tx.membership.findFirst({
        where: {
          userId,
          status: "active",
          endDate: { gte: new Date() },
          remainingSessions: { gt: 0 }
        },
        orderBy: { endDate: "desc" }
      });
      if (!mem) throw new AppError("NO_MEMBERSHIP", 400);

      // (b) lớp tồn tại & còn slot?
      const cls = await tx.class.findUnique({
        where: { id: classIdBig },
        include: { _count: { select: { bookings: { where: { status: "booked" } } } } }
      });
      if (!cls) throw new AppError("CLASS_NOT_FOUND", 404);
      if (cls._count.bookings >= cls.capacity) throw new AppError("CLASS_FULL", 409);

      // (c) đã có booking trùng chưa? (unique [classId, userId] cũng chặn lần 2)
      const existed = await tx.booking.findUnique({
        where: { classId_userId: { classId: classIdBig, userId } }
      });
      if (existed) throw new AppError("ALREADY_BOOKED", 409);

      // (d) tạo booking
      const booking = await tx.booking.create({
        data: { classId: classIdBig, userId, status: "booked" }
      });

      return booking;
    });

    // 3) OK
    return res.json(result);

  } catch (e: any) {
  if (e instanceof AppError) return res.status(e.status).json({ code: e.code, message: e.message });
  // Prisma unique violation?
  if (e.code === "P2002") return res.status(409).json({ code: "UNIQUE_VIOLATION", message: "Already exists" });
  throw e; // để errorHandler bắt và log
}
}

export async function cancel(req: Request & { user?: any }, res: Response) {
  const userId = BigInt(req.user!.id);
  const id = BigInt(req.params.id);

  // rule “hủy đúng hạn” đơn giản: cho phép hủy nếu còn > 2h trước giờ học
  const booking = await prisma.booking.findUnique({
    where: { id },
    include: { class: true }
  });
  if (!booking || booking.userId !== userId) return res.status(404).json({ message: "BOOKING_NOT_FOUND" });

  const now = new Date().getTime();
  const cutoffMs = 2 * 60 * 60 * 1000;
  const classStart = new Date(booking.class.startTime).getTime();

  if (classStart - now <= cutoffMs) {
    // hủy trễ — tùy bạn: từ chối hoặc chuyển trạng thái khác
    return res.status(409).json({ message: "CANCEL_TOO_LATE" });
  }

  const updated = await prisma.booking.update({
    where: { id },
    data: { status: "cancelled" }
  });

  res.json(updated);
}
