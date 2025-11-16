import { z } from "zod";

export const CreateClassDto = z.object({
  title: z.string().min(3, "Tiêu đề phải có ít nhất 3 ký tự").max(200),
  description: z.string().max(1000).optional(),
  trainerId: z.coerce.number().int().positive("Trainer ID không hợp lệ"),
  branchId: z.coerce.number().int().positive("Branch ID không hợp lệ"),
  startTime: z.string().datetime("Thời gian bắt đầu không hợp lệ"),
  endTime: z.string().datetime("Thời gian kết thúc không hợp lệ"),
  capacity: z.coerce.number().int().positive("Sức chứa phải lớn hơn 0").max(100, "Sức chứa tối đa 100")
});

export const UpdateClassDto = z.object({
  title: z.string().min(3).max(200).optional(),
  description: z.string().max(1000).optional(),
  trainerId: z.coerce.number().int().positive().optional(),
  branchId: z.coerce.number().int().positive().optional(),
  startTime: z.string().datetime().optional(),
  endTime: z.string().datetime().optional(),
  capacity: z.coerce.number().int().positive().max(100).optional()
}).refine(
  (data) => Object.keys(data).length > 0,
  { message: "Phải có ít nhất 1 trường để cập nhật" }
);

export type CreateClassInput = z.infer<typeof CreateClassDto>;
export type UpdateClassInput = z.infer<typeof UpdateClassDto>;