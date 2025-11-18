import { Request, Response } from "express";
import { prisma } from "../../libs/prisma";
import { hashPassword, verifyPassword, signJwt } from "../../libs/auth";
import { asyncHandler } from "../../utils/errors";

export const register = asyncHandler(async (req: Request, res: Response) => {
  const { email, phone, fullName, password } = req.body || {};
  if ((!email && !phone) || !fullName || !password) {
    return res.status(400).json({ message: "email/phone, fullName, password required" });
  }
  try {
    const passwordHash = await hashPassword(password);
    const user = await prisma.user.create({
      data: { email, phone, fullName, passwordHash, role: "member" }
    });
    return res.status(201).json({ id: user.id, fullName: user.fullName });
  } catch (e: any) {
    // unique email/phone có thể trùng
    return res.status(400).json({ message: "REGISTER_FAILED", detail: e?.message });
  }
});

export const login = asyncHandler(async (req: Request, res: Response) => {
  const { email, phone, password } = req.body || {};
  if (!password || (!email && !phone)) return res.status(400).json({ message: "Missing credentials" });

  const user = await prisma.user.findFirst({ where: { OR: [{ email }, { phone }] } });
  if (!user) return res.status(400).json({ message: "Account not found" });

  const ok = await verifyPassword(password, user.passwordHash);
  if (!ok) return res.status(400).json({ message: "Wrong password" });
  if (user.status !== "active") return res.status(403).json({ message: "Inactive user" });

  const token = signJwt({ id: String(user.id), role: user.role });
  return res.json({
    token,
    user: { id: user.id, fullName: user.fullName, role: user.role }
  });
});

export const me = asyncHandler(async (req: Request & { user?: any }, res: Response) => {
  const id = BigInt(req.user!.id);
  const user = await prisma.user.findUnique({
    where: { id },
    select: {
      id: true, fullName: true, role: true, status: true, email: true, phone: true,
      memberships: {
        where: { status: "active", endDate: { gte: new Date() } },
        orderBy: { endDate: "desc" },
        select: { id: true, endDate: true, remainingSessions: true, status: true }
      }
    }
  });
  if (!user) return res.status(404).json({ message: "USER_NOT_FOUND" });
  res.json(user);
});

export const changePassword = asyncHandler(async (req: Request & { user?: any }, res: Response) => {
  const id = BigInt(req.user!.id);
  const { currentPassword, newPassword } = req.body || {};
  if (!currentPassword || !newPassword) return res.status(400).json({ message: "currentPassword & newPassword required" });

  const user = await prisma.user.findUnique({ where: { id } });
  if (!user) return res.status(404).json({ message: "USER_NOT_FOUND" });

  const ok = await verifyPassword(currentPassword, user.passwordHash);
  if (!ok) return res.status(400).json({ message: "WRONG_CURRENT_PASSWORD" });

  const passwordHash = await hashPassword(newPassword);
  await prisma.user.update({ where: { id }, data: { passwordHash } });
  res.json({ ok: true });
});
