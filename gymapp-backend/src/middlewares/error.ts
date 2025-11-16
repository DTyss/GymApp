import { Request, Response, NextFunction } from "express";
import { AppError } from "../utils/errors";

export function notFound(_req: Request, res: Response) {
  res.status(404).json({ code: "NOT_FOUND", message: "Route not found" });
}

export function errorHandler(err: any, req: Request, res: Response, _next: NextFunction) {
  const status = err instanceof AppError ? err.status : 500;
  const code   = err instanceof AppError ? err.code   : "INTERNAL_ERROR";
  const message = err.message || "Internal error";

  // log đầy đủ (pino-http đã có req.log)
  (req as any).log?.error({ err, code, status }, "request_error");

  res.status(status).json({ code, message, details: err.details ?? undefined });
}
