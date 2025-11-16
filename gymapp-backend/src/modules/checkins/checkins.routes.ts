import { Router } from "express";
import * as ctl from "./checkins.controller";
import { requireAuth } from "../../middlewares/auth";
import { allow } from "../../middlewares/rbac";
import { Policy } from "../../config/policy";

const r = Router();

// Member tạo QR code để check-in
r.get("/qr/generate", requireAuth, ctl.newQr);

// Admin/Receptionist quét QR và thực hiện check-in
r.post("/qr", requireAuth, allow(...Policy.checkins.qr), ctl.verifyQrAndCheckin);

// Lấy lịch sử check-in của user
r.get("/my", requireAuth, ctl.myCheckins);

export default r;