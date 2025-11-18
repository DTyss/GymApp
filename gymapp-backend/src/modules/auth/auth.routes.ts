import { Router } from "express";
import * as ctl from "./auth.controller";
import { requireAuth } from "../../middlewares/auth";
import { loginLimiter } from "../../middlewares/rate";

const r = Router();

r.post("/register", ctl.register); // tùy chọn: bật khi cần tạo nhanh member
r.post("/login", loginLimiter, ctl.login); // Apply rate limiter cho login
r.get("/me", requireAuth, ctl.me);
r.post("/change-password", requireAuth, ctl.changePassword); // tùy chọn

export default r;
