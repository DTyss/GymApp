import express from "express";
import cors from "cors";
import helmet from "helmet";
import classRoutes from "./modules/classes/classes.routes";
import bookingRoutes from "./modules/bookings/bookings.routes";
import authRoutes from "./modules/auth/auth.routes";
import checkinRoutes from "./modules/checkins/checkins.routes";
import deviceRoutes from "./modules/devices/devices.routes";
import notificationRoutes from "./modules/notifications/notifications.routes";
import planRoutes from "./modules/plans/plans.routes";
import branchRoutes from "./modules/branches/branches.routes";
import userRoutes from "./modules/users/users.routes";
import membershipRoutes from "./modules/memberships/memberships.routes";
import statsRoutes from "./modules/stats/stats.routes";
import { loginLimiter } from "./middlewares/rate";
import swaggerUi from "swagger-ui-express";
import { openapi } from "./docs/openapi";
import { httpLogger } from "./middlewares/httpLogger";

const app = express();
app.use(helmet({
  crossOriginResourcePolicy: { policy: "cross-origin" },
}));
app.use(cors({
  origin: [/^http:\/\/localhost:\d+$/],
  credentials: true
}));

app.get("/health", (_req, res) => res.json({ ok: true }));
app.use(express.json());

// Routes
app.use("/classes", classRoutes);
app.use("/bookings", bookingRoutes);
app.post("/auth/login", loginLimiter, (req,res,next)=>next());
app.use("/auth", authRoutes);
app.use("/checkins", checkinRoutes);
app.use("/devices", deviceRoutes);
app.use("/notifications", notificationRoutes);
app.use("/plans", planRoutes);
app.use("/branches", branchRoutes);
app.use("/users", userRoutes);
app.use("/memberships", membershipRoutes);
app.use("/stats", statsRoutes);


// Documentation
app.get("/openapi.json", (_req, res) => res.json(openapi));
app.use("/docs", swaggerUi.serve, swaggerUi.setup(openapi));

app.use(httpLogger);

// Error handlers
import { notFound, errorHandler } from "./middlewares/error";
app.use(notFound);
app.use(errorHandler);

// BigInt serialization fix
(BigInt.prototype as any).toJSON = function () { return this.toString(); };

export default app;