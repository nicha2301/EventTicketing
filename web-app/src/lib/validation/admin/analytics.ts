import { z } from "zod";

export const adminDateRangeSchema = z.object({
  startDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, "Ngày bắt đầu phải có định dạng YYYY-MM-DD"),
  endDate: z.string().regex(/^\d{4}-\d{2}-\d{2}$/, "Ngày kết thúc phải có định dạng YYYY-MM-DD"),
}).refine((data) => {
  const start = new Date(data.startDate);
  const end = new Date(data.endDate);
  return start <= end;
}, {
  message: "Ngày bắt đầu phải trước hoặc bằng ngày kết thúc",
  path: ["endDate"],
});

export const adminAnalyticsFiltersSchema = z.object({
  dateRange: adminDateRangeSchema.optional(),
  categoryId: z.string().optional(),
  locationId: z.string().optional(),
  organizerId: z.string().optional(),
  status: z.enum(["DRAFT", "PUBLISHED", "CANCELLED", "COMPLETED"]).optional(),
  page: z.number().min(0).default(0),
  size: z.number().min(1).max(100).default(10),
});

export type AdminDateRangeInput = z.infer<typeof adminDateRangeSchema>;
export type AdminAnalyticsFiltersInput = z.infer<typeof adminAnalyticsFiltersSchema>;


