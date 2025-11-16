import { Router } from "express";
import * as ctl from "../branches/branches.controller";
import { requireAuth } from "../../middlewares/auth";
import { allow } from "../../middlewares/rbac";

const r = Router();

r.get("/", requireAuth, ctl.list);
r.get("/:id", requireAuth, ctl.getById);
r.post("/", requireAuth, allow("admin"), ctl.create);
r.put("/:id", requireAuth, allow("admin"), ctl.update);
r.delete("/:id", requireAuth, allow("admin"), ctl.deleteBranch);

export default r;