import { Router } from "express";
import * as ctl from "./stats.controller";
import { requireAuth } from "../../middlewares/auth";
import { allow } from "../../middlewares/rbac";

const r = Router();

// Admin only - Statistics
r.get("/dashboard", requireAuth, allow("admin"), ctl.dashboard);
r.get("/members", requireAuth, allow("admin"), ctl.memberStats);
r.get("/checkins", requireAuth, allow("admin"), ctl.checkinStats);
r.get("/revenue", requireAuth, allow("admin"), ctl.revenueStats);

export default r;