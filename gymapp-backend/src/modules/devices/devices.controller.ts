import { Request, Response } from "express";
import { prisma } from "../../libs/prisma";

export async function registerToken(req: Request & { user?: any }, res: Response) {
  const userId = BigInt(req.user!.id);
  const { fcmToken, platform } = req.body || {};
  if (!fcmToken) return res.status(400).json({ message: "fcmToken required" });

  // một token chỉ thuộc về 1 user – upsert theo fcmToken
  const dev = await prisma.device.upsert({
    where: { fcmToken },
    create: { userId, fcmToken, platform },
    update: { userId, platform }
  });
  res.json({ ok: true, deviceId: dev.id });
}

export async function myDevices(req: Request & { user?: any }, res: Response) {
  const userId = BigInt(req.user!.id);
  const list = await prisma.device.findMany({ where: { userId }, orderBy: { createdAt: "desc" } });
  res.json(list);
}
