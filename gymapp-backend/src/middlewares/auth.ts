import { Request, Response, NextFunction } from "express";
import jwt from "jsonwebtoken";

export interface AuthUser { id: string; role: "member"|"trainer"|"admin"|"receptionist" }

export function requireAuth(req: Request & { user?: AuthUser }, res: Response, next: NextFunction) {
  const token = (req.headers.authorization || "").replace("Bearer ", "");
  if (!token) return res.status(401).json({ message: "Unauthorized" });
  try {
    const decoded = jwt.verify(token, process.env.JWT_SECRET!) as AuthUser;
    req.user = { id: String(decoded.id), role: decoded.role };
    next();
  } catch {
    return res.status(401).json({ message: "Invalid token" });
  }
}
