import { OpenAPIV3 as O } from "openapi-types";

const info: O.InfoObject = {
  title: "GymApp API",
  version: "0.1.0",
  description:
    "Monolithic API for GymApp (auth, classes, bookings, checkins, notifications)",
};

export const openapi: O.Document = {
  openapi: "3.0.3",
  info: {
    title: "GymApp API",
    version: "0.1.0",
    description: "Monolithic API for GymApp (auth, classes, bookings, checkins, notifications)"
  },
  servers: [{ url: "http://localhost:8080" }],
  tags: [
    { name: "Auth" }, { name: "Classes" }, { name: "Bookings" },
    { name: "Checkins" }, { name: "Notifications" }, { name: "Devices" }
  ],
  paths: {
    "/auth/login": {
      post: {
        tags: ["Auth"],
        summary: "Login and get JWT",
        requestBody: {
          required: true,
          content: { "application/json": {
            schema: { type: "object", properties: {
              email: { type: "string", nullable: true },
              phone: { type: "string", nullable: true },
              password: { type: "string" }
            }, required: ["password"] }
          }}
        },
        responses: {
          "200": { description: "OK", content: { "application/json": {
            schema: { type: "object", properties: {
              token: { type: "string" },
              user: { $ref: "#/components/schemas/UserLite" }
            } }
          } } },
          "400": { description: "Bad Request" }
        }
      }
    },
    "/auth/me": {
      get: {
        tags: ["Auth"],
        summary: "Get current user",
        security: [{ bearerAuth: [] }],
        responses: {
          "200": { description: "OK", content: { "application/json": {
            schema: { $ref: "#/components/schemas/UserMe" }
          } } },
          "401": { description: "Unauthorized" }
        }
      }
    },
    "/classes": {
      get: {
        tags: ["Classes"],
        summary: "List classes with paging",
        security: [{ bearerAuth: [] }],
        parameters: [
          { name: "from", in: "query", schema: { type: "string", format: "date" } },
          { name: "to", in: "query", schema: { type: "string", format: "date" } },
          { name: "branchId", in: "query", schema: { type: "integer" } },
          { name: "trainerId", in: "query", schema: { type: "integer" } },
          { name: "page", in: "query", schema: { type: "integer", default: 1 } },
          { name: "pageSize", in: "query", schema: { type: "integer", default: 20 } },
          { name: "sortBy", in: "query", schema: { type: "string", enum: ["startTime","title","capacity"], default: "startTime" } },
          { name: "sortDir", in: "query", schema: { type: "string", enum: ["asc","desc"], default: "asc" } }
        ],
        responses: {
          "200": { description: "OK", content: { "application/json": {
            schema: { $ref: "#/components/schemas/PaginatedClasses" }
          } } }
        }
      }
    },
    "/bookings": {
      post: {
        tags: ["Bookings"],
        summary: "Create booking",
        security: [{ bearerAuth: [] }],
        requestBody: {
          required: true,
          content: { "application/json": {
            schema: { type: "object", properties: {
              classId: { type: "integer" }
            }, required: ["classId"] }
          } }
        },
        responses: {
          "200": { description: "OK", content: { "application/json": {
            schema: { $ref: "#/components/schemas/Booking" }
          } } },
          "400": { description: "Validation/No membership" },
          "409": { description: "Class full/Already booked" }
        }
      }
    },
    "/bookings/my": {
      get: {
        tags: ["Bookings"],
        summary: "List my bookings",
        security: [{ bearerAuth: [] }],
        parameters: [
          { name: "page", in: "query", schema: { type: "integer", default: 1 } },
          { name: "pageSize", in: "query", schema: { type: "integer", default: 20 } },
          { name: "sortBy", in: "query", schema: { type: "string", enum: ["createdAt","class.startTime"], default: "createdAt" } },
          { name: "sortDir", in: "query", schema: { type: "string", enum: ["asc","desc"], default: "desc" } }
        ],
        responses: {
          "200": { description: "OK", content: { "application/json": {
            schema: { $ref: "#/components/schemas/PaginatedBookings" }
          } } }
        }
      }
    },
    "/checkins/qr": {
      post: {
        tags: ["Checkins"],
        summary: "Verify QR and create checkin",
        security: [{ bearerAuth: [] }],
        requestBody: { required: true, content: { "application/json": {
          schema: { type: "object", properties: {
            payload: { $ref: "#/components/schemas/QrPayload" },
            branchId: { type: "integer" }
          }, required: ["payload"] }
        } } },
        responses: {
          "200": { description: "OK", content: { "application/json": {
            schema: { type: "object", properties: {
              ok: { type: "boolean" },
              membershipId: { type: "integer" },
              remainingSessions: { type: "integer" }
            } }
          } } },
          "400": { description: "INVALID_QR/NO_MEMBERSHIP" }
        }
      }
    },
    "/devices": {
      post: {
        tags: ["Devices"],
        summary: "Register FCM token",
        security: [{ bearerAuth: [] }],
        requestBody: { required: true, content: { "application/json": {
          schema: { type: "object", properties: {
            fcmToken: { type: "string" },
            platform: { type: "string", enum: ["android","ios","web"] }
          }, required: ["fcmToken"] }
        } } },
        responses: { "200": { description: "OK" } }
      }
    },
    "/notifications/test": {
      post: {
        tags: ["Notifications"],
        summary: "Send test notification to self or userId",
        security: [{ bearerAuth: [] }],
        requestBody: { required: false, content: { "application/json": {
          schema: { type: "object", properties: {
            title: { type: "string" }, body: { type: "string" }, userId: { type: "integer" }
          } }
        } } },
        responses: { "200": { description: "OK" } }
      }
    },
    "/notifications/my": {
      get: {
        tags: ["Notifications"],
        summary: "List my notifications",
        security: [{ bearerAuth: [] }],
        parameters: [
          { name: "page", in: "query", schema: { type: "integer", default: 1 } },
          { name: "pageSize", in: "query", schema: { type: "integer", default: 20 } },
          { name: "isRead", in: "query", schema: { type: "boolean" } },
          { name: "sortDir", in: "query", schema: { type: "string", enum: ["asc","desc"], default: "desc" } }
        ],
        responses: { "200": { description: "OK" } }
      }
    }
  },
  components: {
    securitySchemes: {
      bearerAuth: { type: "http", scheme: "bearer", bearerFormat: "JWT" }
    },
    schemas: {
      UserLite: {
        type: "object",
        properties: { id: { type: "integer" }, fullName: { type: "string" }, role: { type: "string" } }
      },
      UserMe: {
        type: "object",
        properties: {
          id: { type: "integer" }, fullName: { type: "string" },
          role: { type: "string" }, status: { type: "string" },
          email: { type: "string", nullable: true }, phone: { type: "string", nullable: true },
          memberships: { type: "array", items: {
            type: "object", properties: {
              id: { type: "integer" },
              endDate: { type: "string", format: "date-time" },
              remainingSessions: { type: "integer" },
              status: { type: "string" }
            }
          } }
        }
      },
      ClassItem: {
        type: "object",
        properties: {
          id: { type: "integer" }, title: { type: "string" }, description: { type: "string", nullable: true },
          startTime: { type: "string", format: "date-time" }, endTime: { type: "string", format: "date-time" },
          capacity: { type: "integer" }, available: { type: "integer" },
          branch: { type: "object", properties: { id: { type: "integer" }, name: { type: "string" } } },
          trainer: { type: "object", properties: { id: { type: "integer" }, fullName: { type: "string" } } }
        }
      },
      PaginatedClasses: {
        type: "object",
        properties: {
          items: { type: "array", items: { $ref: "#/components/schemas/ClassItem" } },
          total: { type: "integer" }, page: { type: "integer" }, pageSize: { type: "integer" }
        }
      },
      Booking: {
        type: "object",
        properties: {
          id: { type: "integer" }, classId: { type: "integer" }, userId: { type: "integer" },
          status: { type: "string" }, createdAt: { type: "string", format: "date-time" }
        }
      },
      PaginatedBookings: {
        type: "object",
        properties: {
          items: { type: "array", items: { $ref: "#/components/schemas/Booking" } },
          total: { type: "integer" }, page: { type: "integer" }, pageSize: { type: "integer" }
        }
      },
      QrPayload: {
        type: "object",
        properties: {
          userId: { type: "string" },
          nonce: { type: "string" },
          exp: { type: "integer" },
          sig: { type: "string" }
        },
        required: ["userId","nonce","exp","sig"]
      }
    }
  },
  security: []
};
