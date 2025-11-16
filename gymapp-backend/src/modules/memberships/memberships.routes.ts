import { Router } from "express";
import * as ctl from "./memberships.controller";
import { requireAuth } from "../../middlewares/auth";
import { allow } from "../../middlewares/rbac";

const r = Router();

// Admin/Receptionist - Quản lý memberships
r.get("/", requireAuth, allow("admin", "receptionist"), ctl.list);
r.get("/:id", requireAuth, allow("admin", "receptionist"), ctl.getById);
r.post("/", requireAuth, allow("admin", "receptionist"), ctl.create);
r.put("/:id/extend", requireAuth, allow("admin", "receptionist"), ctl.extend);
r.put("/:id/pause", requireAuth, allow("admin"), ctl.pause);
r.put("/:id/resume", requireAuth, allow("admin"), ctl.resume);

// Member - Xem memberships của chính mình
r.get("/my/list", requireAuth, ctl.myMemberships);

export default r;