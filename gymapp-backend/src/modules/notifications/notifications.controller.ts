import { Request, Response } from "express";
import { prisma } from "../../libs/prisma";
import { sendToTokens } from "../../libs/fcm";
import { parsePaging, toSkipTake } from "../../utils/paging";
import { asyncHandler } from "../../utils/errors";

export const sendTest = asyncHandler(async (req: Request & { user?: any }, res: Response) => {
  const senderId = BigInt(req.user!.id);
  const { title = "GymApp", body = "Hello from server", userId } = req.body || {};
  const targetUserId = userId ? BigInt(userId) : senderId;

  // lấy tất cả token của user đích
  const devs = await prisma.device.findMany({ where: { userId: targetUserId } });
  const tokens = devs.map(d => d.fcmToken);

  // ghi log notification (dù có token hay không)
  await prisma.notification.create({
    data: { userId: targetUserId, title, body }
  });

  if (tokens.length === 0) {
    return res.json({ ok: true, sent: 0, note: "No tokens" });
  }

  const result = await sendToTokens(tokens, title, body);
  res.json({ ok: true, sent: result.successCount, failed: result.failureCount });
});

export const myNoti = asyncHandler(async (req: Request & { user?: any }, res: Response) => {
  const userId = BigInt(req.user!.id);
  const paging = parsePaging(req.query);
  const { skip, take } = toSkipTake(paging);

  const isReadQ = typeof req.query.isRead !== "undefined"
    ? (String(req.query.isRead).toLowerCase() === "true")
    : undefined;

  const where: any = { userId };
  if (typeof isReadQ === "boolean") where.isRead = isReadQ;

  const sortDir = (String(req.query.sortDir).toLowerCase()==="asc" ? "asc" : "desc");

  const [items, total] = await Promise.all([
    prisma.notification.findMany({
      where,
      orderBy: { sentAt: sortDir },
      skip, take
    }),
    prisma.notification.count({ where })
  ]);

  res.json({ items, total, page: paging.page, pageSize: paging.pageSize });
});

export const markAsRead = asyncHandler(async (req: Request & { user?: any }, res: Response) => {
  const userId = BigInt(req.user!.id);
  const notificationId = BigInt(req.params.id);

  const notification = await prisma.notification.findUnique({
    where: { id: notificationId }
  });

  if (!notification || notification.userId !== userId) {
    return res.status(404).json({ message: "NOTIFICATION_NOT_FOUND" });
  }

  const updated = await prisma.notification.update({
    where: { id: notificationId },
    data: { isRead: true }
  });

  res.json(updated);
});

export const markAllAsRead = asyncHandler(async (req: Request & { user?: any }, res: Response) => {
  const userId = BigInt(req.user!.id);

  const result = await prisma.notification.updateMany({
    where: {
      userId,
      isRead: false
    },
    data: {
      isRead: true
    }
  });

  res.json({ 
    ok: true, 
    message: "Đã đánh dấu tất cả đã đọc",
    count: result.count  
  });
});
