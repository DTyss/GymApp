import pinoHttp from "pino-http";
import { logger } from "../config/logger";

export const httpLogger = pinoHttp({
  logger,
  autoLogging: { ignore: (req) => req.url === "/health" },
  genReqId: (req) => req.headers["x-request-id"] as string || cryptoRandom(),
});

function cryptoRandom(len = 12) {
  return Math.random().toString(36).slice(2) + Math.random().toString(36).slice(2);
}
