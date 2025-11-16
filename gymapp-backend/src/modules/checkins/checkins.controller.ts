import { Request, Response } from "express";
import { prisma } from "../../libs/prisma";
import { verifyQr } from "../../libs/crypto";
import { Prisma } from "@prisma/client";
import { signQr } from "../../libs/crypto";
import { AppError } from "../../utils/errors";

// body: { payload: { userId, nonce, exp, sig }, branchId }
export async function verifyQrAndCheckin(req: Request & { user?: any }, res: Response) {
  const { payload, branchId } = req.body as { payload: any; branchId?: number };

  if (!verifyQr(payload)) throw new AppError("INVALID_QR", 400);
  const targetUserId = BigInt(payload.userId);
  const branch = branchId ? BigInt(branchId) : 1n;

  try {
    const result = await prisma.$transaction(async (tx: Prisma.TransactionClient) => {
      // 1) membership hợp lệ
      const mem = await tx.membership.findFirst({
        where: {
          userId: targetUserId,
          status: "active",
          endDate: { gte: new Date() },
          remainingSessions: { gt: 0 }
        },
        orderBy: { endDate: "desc" }
      });
      if (!mem) throw new AppError("NO_MEMBERSHIP", 400);

      // 2) ghi checkin
      await tx.checkin.create({
        data: {
          userId: targetUserId,
          branchId: branch,
          method: "qr",
          status: "success"
        }
      });

      // 3) trừ buổi
      const updated = await tx.membership.update({
        where: { id: mem.id },
        data: { remainingSessions: { decrement: 1 } }
      });

      return {
        remainingSessions: updated.remainingSessions,
        membershipId: updated.id
      };
    });

    return res.json({ ok: true, ...result });
  } catch (e: any) {
    const message = e?.message || "CHECKIN_ERROR";
    const code = message === "NO_MEMBERSHIP" ? 400 : 500;
    return res.status(code).json({ message });
  }
  
}

export async function newQr(req: Request & { user?: any }, res: Response) {
  const uid = String(req.user!.id);
  const ttl = Number(process.env.QR_TTL_SECONDS || 120);
  const exp = Math.floor(Date.now() / 1000) + ttl;
  const nonce = cryptoRandom();
  const signed = signQr({ userId: uid, nonce, exp });
  res.json(signed);
}

function cryptoRandom(len = 16) {
  return [...crypto.getRandomValues(new Uint8Array(len))].map(b => b.toString(16).padStart(2, "0")).join("");
}
