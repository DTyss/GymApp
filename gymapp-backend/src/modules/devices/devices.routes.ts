import { Router } from "express";
import * as ctl from "./devices.controller";
import { requireAuth } from "../../middlewares/auth";

const r = Router();
r.post("/", requireAuth, ctl.registerToken);
r.get("/my", requireAuth, ctl.myDevices);
export default r;
