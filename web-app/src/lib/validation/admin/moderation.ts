import { z } from "zod";

export const adminRatingStatusUpdateSchema = z.object({
  status: z.enum(["APPROVED", "REJECTED"], {
    errorMap: () => ({ message: "Trạng thái không hợp lệ" }),
  }),
  reason: z.string().max(500, "Lý do không được quá 500 ký tự").optional(),
});

export const adminRatingFiltersSchema = z.object({
  status: z.enum(["PENDING", "APPROVED", "REJECTED", "REPORTED"]).optional(),
  page: z.number().min(0).default(0),
  size: z.number().min(1).max(100).default(10),
});

export const adminBulkRatingActionSchema = z.object({
  ratingIds: z.array(z.string()).min(1, "Phải chọn ít nhất 1 đánh giá"),
  action: z.enum(["APPROVE", "REJECT", "DELETE"], {
    errorMap: () => ({ message: "Hành động không hợp lệ" }),
  }),
  reason: z.string().max(500, "Lý do không được quá 500 ký tự").optional(),
});

export type AdminRatingStatusUpdateInput = z.infer<typeof adminRatingStatusUpdateSchema>;
export type AdminRatingFiltersInput = z.infer<typeof adminRatingFiltersSchema>;
export type AdminBulkRatingActionInput = z.infer<typeof adminBulkRatingActionSchema>;


