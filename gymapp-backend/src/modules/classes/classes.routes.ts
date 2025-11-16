import { Router } from "express";
import * as ctl from "./classes.controller";
import { requireAuth } from "../../middlewares/auth";
import { allow } from "../../middlewares/rbac";
import { Policy } from "../../config/policy";

const r = Router();

// Public routes (tất cả authenticated users)
r.get("/", requireAuth, allow(...Policy.classes.list), ctl.list);
r.get("/:id", requireAuth, allow(...Policy.classes.list), ctl.getById);

// Trainer & Admin - Tạo và quản lý lớp
r.post("/", requireAuth, allow(...Policy.classes.create), ctl.create);
r.put("/:id", requireAuth, allow(...Policy.classes.update), ctl.update);
r.delete("/:id", requireAuth, allow(...Policy.classes.delete), ctl.deleteClass);

export default r;