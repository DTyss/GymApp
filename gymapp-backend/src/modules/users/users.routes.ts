import { Router } from "express";
import * as ctl from "../users/users.controller";
import { requireAuth } from "../../middlewares/auth";
import { allow } from "../../middlewares/rbac";
import { Policy } from "../../config/policy";

const r = Router();

// Admin - Quản lý users
r.get("/", requireAuth, allow(...Policy.users.list), ctl.list);
r.get("/:id", requireAuth, allow(...Policy.users.list), ctl.getById);
r.put("/:id", requireAuth, allow(...Policy.users.list), ctl.update);
r.put("/:id/status", requireAuth, allow(...Policy.users.list), ctl.updateStatus);

// Self update - tất cả user có thể update thông tin của chính mình
r.put("/me/profile", requireAuth, allow(...Policy.users.updateSelf), ctl.updateSelf);

export default r;