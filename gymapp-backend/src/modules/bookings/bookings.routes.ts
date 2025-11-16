import { Router } from "express";
import * as ctl from "./bookings.controller";
import { requireAuth } from "../../middlewares/auth";
import { allow } from "../../middlewares/rbac";
import { Policy } from "../../config/policy";

const r = Router();
r.get("/my",     requireAuth, allow(...Policy.bookings.listMy), ctl.myBookings);
r.post("/",      requireAuth, allow(...Policy.bookings.create), ctl.create);
r.delete("/:id", requireAuth, allow(...Policy.bookings.cancel), ctl.cancel);
export default r;