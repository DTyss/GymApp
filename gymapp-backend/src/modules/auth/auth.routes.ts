import { Router } from "express";
import * as ctl from "./auth.controller";
import { requireAuth } from "../../middlewares/auth";

const r = Router();

r.post("/register", ctl.register); // tùy chọn: bật khi cần tạo nhanh member
r.post("/login", ctl.login);
r.get("/me", requireAuth, ctl.me);
r.post("/change-password", requireAuth, ctl.changePassword); // tùy chọn

export default r;
