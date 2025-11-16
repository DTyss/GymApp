import { Router } from "express";
import * as ctl from "./notifications.controller";
import { requireAuth } from "../../middlewares/auth";
import { allow } from "../../middlewares/rbac";
import { Policy } from "../../config/policy";

const r = Router();
r.post("/test", requireAuth, allow(...Policy.notifications.send), ctl.sendTest);
r.get("/my",    requireAuth, allow(...Policy.notifications.listMy), ctl.myNoti);
r.put("/:id/read", requireAuth, allow(...Policy.notifications.listMy), ctl.markAsRead);
r.put("/read-all", requireAuth, allow(...Policy.notifications.listMy), ctl.markAllAsRead);
export default r;
