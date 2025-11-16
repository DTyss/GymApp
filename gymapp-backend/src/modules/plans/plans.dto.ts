import { z } from "zod";

export const CreatePlanDto = z.object({
  name: z.string().min(3, "Tên gói phải có ít nhất 3 ký tự").max(100),
  price: z.coerce.number().positive("Giá phải lớn hơn 0"),
  sessions: z.coerce.number().int().positive("Số buổi phải là số nguyên dương"),
  durationDays: z.coerce.number().int().positive("Thời hạn phải là số nguyên dương"),
  isActive: z.boolean().optional().default(true)
});

export const UpdatePlanDto = z.object({
  name: z.string().min(3).max(100).optional(),
  price: z.coerce.number().positive().optional(),
  sessions: z.coerce.number().int().positive().optional(),
  durationDays: z.coerce.number().int().positive().optional(),
  isActive: z.boolean().optional()
}).refine(
  (data) => Object.keys(data).length > 0,
  { message: "Phải có ít nhất 1 trường để cập nhật" }
);

export type CreatePlanInput = z.infer<typeof CreatePlanDto>;
export type UpdatePlanInput = z.infer<typeof UpdatePlanDto>;