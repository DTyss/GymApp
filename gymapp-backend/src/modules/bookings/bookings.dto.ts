import { z } from "zod";
export const CreateBookingDto = z.object({
  classId: z.coerce.number().int().positive()
});
export type CreateBookingInput = z.infer<typeof CreateBookingDto>;
