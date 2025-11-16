import { z } from "zod";

/**
 * DTO cho admin cập nhật user
 */
export const UpdateUserDto = z.object({
  fullName: z.string().min(2, "Tên phải có ít nhất 2 ký tự").max(100).optional(),
  email: z.string().email("Email không hợp lệ").optional(),
  phone: z.string().regex(/^[0-9]{10,11}$/, "Số điện thoại không hợp lệ").optional(),
  role: z.enum(["member", "trainer", "admin", "receptionist"]).optional()
}).refine(
  (data) => Object.keys(data).length > 0,
  { message: "Phải có ít nhất 1 trường để cập nhật" }
);

/**
 * DTO cho user tự cập nhật thông tin
 */
export const UpdateSelfDto = z.object({
  fullName: z.string().min(2).max(100).optional(),
  email: z.string().email().optional(),
  phone: z.string().regex(/^[0-9]{10,11}$/).optional()
}).refine(
  (data) => Object.keys(data).length > 0,
  { message: "Phải có ít nhất 1 trường để cập nhật" }
);

/**
 * DTO cho cập nhật trạng thái user
 */
export const UpdateStatusDto = z.object({
  status: z.enum(["active", "inactive", "banned"], {
    message: "Trạng thái phải là active, inactive hoặc banned"
  })
});

export type UpdateUserInput = z.infer<typeof UpdateUserDto>;
export type UpdateSelfInput = z.infer<typeof UpdateSelfDto>;
export type UpdateStatusInput = z.infer<typeof UpdateStatusDto>;