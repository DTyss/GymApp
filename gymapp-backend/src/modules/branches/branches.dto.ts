import { z } from "zod";

export const CreateBranchDto = z.object({
  name: z.string().min(3, "Tên chi nhánh phải có ít nhất 3 ký tự").max(100),
  address: z.string().max(255).optional(),
  isActive: z.boolean().optional().default(true)
});

export const UpdateBranchDto = z.object({
  name: z.string().min(3).max(100).optional(),
  address: z.string().max(255).optional(),
  isActive: z.boolean().optional()
}).refine(
  (data) => Object.keys(data).length > 0,
  { message: "Phải có ít nhất 1 trường để cập nhật" }
);

export type CreateBranchInput = z.infer<typeof CreateBranchDto>;
export type UpdateBranchInput = z.infer<typeof UpdateBranchDto>;