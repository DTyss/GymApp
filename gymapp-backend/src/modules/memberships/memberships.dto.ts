import { z } from "zod";

/**
 * DTO cho việc tạo membership mới
 */
export const CreateMembershipDto = z.object({
  userId: z.coerce.number().int().positive("User ID không hợp lệ"),
  planId: z.coerce.number().int().positive("Plan ID không hợp lệ"),
  startDate: z.string().datetime().optional() // Nếu không truyền thì dùng ngày hiện tại
});

/**
 * DTO cho việc gia hạn membership
 */
export const ExtendMembershipDto = z.object({
  additionalDays: z.coerce.number().int().positive("Số ngày gia hạn phải lớn hơn 0").optional(),
  additionalSessions: z.coerce.number().int().positive("Số buổi thêm phải lớn hơn 0").optional()
}).refine(
  (data) => data.additionalDays || data.additionalSessions,
  { message: "Phải có ít nhất additionalDays hoặc additionalSessions" }
);

export type CreateMembershipInput = z.infer<typeof CreateMembershipDto>;
export type ExtendMembershipInput = z.infer<typeof ExtendMembershipDto>;