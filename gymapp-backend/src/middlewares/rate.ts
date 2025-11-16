import rateLimit from "express-rate-limit";
export const loginLimiter = rateLimit({
  windowMs: 10 * 60 * 1000, // 10 phút
  max: 30, // 30 lần/10p cho /auth/login
  standardHeaders: true,
  legacyHeaders: false
});
