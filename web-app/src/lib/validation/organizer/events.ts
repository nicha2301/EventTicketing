import { z } from "zod";

// Event create (new event wizard - step 1)
export const organizerEventCreateSchema = z
  .object({
    title: z.string().min(3, "Tiêu đề phải có ít nhất 3 ký tự").max(200, "Tiêu đề không được quá 200 ký tự"),
    shortDescription: z
      .string()
      .min(10, "Mô tả ngắn phải có ít nhất 10 ký tự")
      .max(500, "Mô tả ngắn không được quá 500 ký tự"),
    description: z.string().min(20, "Mô tả phải có ít nhất 20 ký tự").max(2000, "Mô tả không được quá 2000 ký tự"),
    categoryId: z.string().min(1, "Vui lòng chọn danh mục"),
    locationId: z.string().min(1, "Vui lòng chọn địa điểm"),
    address: z.string().min(3, "Địa chỉ phải có ít nhất 3 ký tự").max(255, "Địa chỉ không được quá 255 ký tự"),
    city: z.string().min(2, "Thành phố phải có ít nhất 2 ký tự").max(100, "Thành phố không được quá 100 ký tự"),
    latitude: z.coerce
      .number()
      .min(-90, "Vĩ độ phải từ -90 đến 90")
      .max(90, "Vĩ độ phải từ -90 đến 90"),
    longitude: z.coerce
      .number()
      .min(-180, "Kinh độ phải từ -180 đến 180")
      .max(180, "Kinh độ phải từ -180 đến 180"),
    maxAttendees: z.coerce
      .number()
      .min(1, "Số người tối đa phải lớn hơn 0")
      .max(100000, "Số người tối đa không được quá 100,000"),
    startDate: z.string().min(1, "Vui lòng chọn thời gian bắt đầu"),
    endDate: z.string().min(1, "Vui lòng chọn thời gian kết thúc"),
    isFree: z.boolean().default(false),
    isPrivate: z.boolean().default(false),
    isDraft: z.boolean().default(true),
  })
  .refine(
    (data) => {
      const start = new Date(data.startDate);
      const end = new Date(data.endDate);
      return start < end;
    },
    {
      message: "Thời gian bắt đầu phải trước thời gian kết thúc",
      path: ["endDate"],
    }
  )
  .refine(
    (data) => {
      const start = new Date(data.startDate);
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      return start >= today;
    },
    {
      message: "Thời gian bắt đầu không được sớm hơn hôm nay",
      path: ["startDate"],
    }
  );

export type OrganizerEventCreateInput = z.infer<typeof organizerEventCreateSchema>;

// Event update (edit page)
export const organizerEventUpdateSchema = z.object({
  title: z.string().min(3).optional(),
  shortDescription: z.string().min(10).optional(),
  description: z.string().min(20).optional(),
  categoryId: z.string().optional(),
  locationId: z.string().optional(),
  address: z.string().optional(),
  city: z.string().optional(),
  latitude: z.coerce.number().optional(),
  longitude: z.coerce.number().optional(),
  maxAttendees: z.coerce.number().optional(),
  startDate: z.string().optional(),
  endDate: z.string().optional(),
});

export type OrganizerEventUpdateInput = z.infer<typeof organizerEventUpdateSchema>;

// Ticket type (used in new wizard and manage ticket types)
export const organizerTicketTypeSchema = z
  .object({
    name: z.string().min(2, "Tên loại vé phải có ít nhất 2 ký tự").max(100, "Tên loại vé không được quá 100 ký tự"),
    description: z.string().max(500, "Mô tả không được quá 500 ký tự").optional(),
    price: z.coerce.number().min(0, "Giá không được âm"),
    quantity: z.coerce.number().min(1, "Số lượng phải lớn hơn 0"),
    minTicketsPerOrder: z.coerce.number().min(1, "Số vé tối thiểu/đơn hàng phải lớn hơn 0").optional(),
    maxTicketsPerCustomer: z.coerce.number().min(1, "Số vé tối đa/khách hàng phải lớn hơn 0").optional(),
    isVIP: z.boolean().default(false),
    isEarlyBird: z.boolean().default(false),
    isActive: z.boolean().default(true),
    salesStartDate: z.string().optional(),
    salesEndDate: z.string().optional(),
  })
  .refine(
    (data) => {
      if (data.salesStartDate && data.salesEndDate) {
        const start = new Date(data.salesStartDate);
        const end = new Date(data.salesEndDate);
        return start <= end;
      }
      return true;
    },
    {
      message: "Thời gian bán từ phải trước hoặc bằng thời gian bán đến",
      path: ["salesEndDate"],
    }
  );

export type OrganizerTicketTypeInput = z.infer<typeof organizerTicketTypeSchema>;

// Report request (reports page)
export const organizerReportSchema = z
  .object({
    eventId: z.string().min(1, "Vui lòng chọn sự kiện"),
    fromDate: z.string().min(1, "Vui lòng chọn ngày bắt đầu"),
    toDate: z.string().min(1, "Vui lòng chọn ngày kết thúc"),
  })
  .refine(
    (data) => {
      const from = new Date(data.fromDate);
      const to = new Date(data.toDate);
      return from <= to;
    },
    { message: "Từ ngày phải trước hoặc bằng Đến ngày", path: ["toDate"] }
  );

export type OrganizerReportInput = z.infer<typeof organizerReportSchema>;



