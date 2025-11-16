export class AppError extends Error {
  status: number;
  code: string;
  details?: any;
  constructor(code: string, status = 400, message?: string, details?: any) {
    super(message || code);
    this.code = code;
    this.status = status;
    this.details = details;
  }
}

// helper wrapper cho async controllers để tự catch
import { Request, Response, NextFunction } from "express";
export const asyncHandler = (fn: any) =>
  (req: Request, res: Response, next: NextFunction) =>
    Promise.resolve(fn(req, res, next)).catch(next);
