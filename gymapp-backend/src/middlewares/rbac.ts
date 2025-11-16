import { Request, Response, NextFunction } from "express";

export type Role = "member" | "trainer" | "admin" | "receptionist";

export function allow(...roles: Role[]) {
  return (req: Request & { user?: { role: Role } }, res: Response, next: NextFunction) => {
    if (!req.user) return res.status(401).json({ message: "Unauthorized" });
    if (!roles.includes(req.user.role)) return res.status(403).json({ message: "Forbidden" });
    next();
  };
}
