import crypto from "crypto";

export type QrPayload = { userId: string; nonce: string; exp: number; sig?: string };

const SECRET = process.env.QR_SECRET || "dev_qr_secret";

export function signQr(payload: { userId: string; nonce: string; exp: number }) {
  const data = `${payload.userId}.${payload.nonce}.${payload.exp}`;
  const sig = crypto.createHmac("sha256", SECRET).update(data).digest("hex");
  return { ...payload, sig };
}

export function verifyQr(p: QrPayload): boolean {
  if (!p.userId || !p.nonce || !p.exp || !p.sig) return false;
  const now = Math.floor(Date.now() / 1000);
  if (now > p.exp) return false;
  const data = `${p.userId}.${p.nonce}.${p.exp}`;
  const sig = crypto.createHmac("sha256", SECRET).update(data).digest("hex");
  return sig === p.sig;
}
