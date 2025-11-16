import { Router } from "express";
import * as ctl from "../plans/plans.controller";
import { requireAuth } from "../../middlewares/auth";
import { allow } from "../../middlewares/rbac";
import { Policy } from "../../config/policy";

const r = Router();

// Public route - member có thể xem để chọn gói
r.get("/", requireAuth, ctl.list);
r.get("/:id", requireAuth, ctl.getById);

// Admin only
r.post("/", requireAuth, allow(...Policy.plans.crud), ctl.create);
r.put("/:id", requireAuth, allow(...Policy.plans.crud), ctl.update);
r.delete("/:id", requireAuth, allow(...Policy.plans.crud), ctl.deletePlan);

export default r;