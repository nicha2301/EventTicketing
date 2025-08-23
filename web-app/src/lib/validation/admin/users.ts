import { z } from "zod";

export const adminUserCreateSchema = z.object({
  fullName: z.string().min(2, "Họ tên phải có ít nhất 2 ký tự").max(100, "Họ tên không được quá 100 ký tự"),
  email: z.string().email("Email không hợp lệ"),
  password: z.string().min(6, "Mật khẩu phải có ít nhất 6 ký tự"),
  phoneNumber: z.string().regex(/^\+?[0-9]{10,15}$/, "Số điện thoại không hợp lệ").optional(),
  role: z.enum(["USER", "ORGANIZER", "ADMIN"], {
    errorMap: () => ({ message: "Vai trò không hợp lệ" }),
  }),
  enabled: z.boolean().default(true),
});

export const adminUserUpdateSchema = z.object({
  fullName: z.string().min(2, "Họ tên phải có ít nhất 2 ký tự").max(100, "Họ tên không được quá 100 ký tự").optional(),
  phoneNumber: z.string().regex(/^\+?[0-9]{10,15}$/, "Số điện thoại không hợp lệ").optional(),
  profilePictureUrl: z.string().url("URL ảnh đại diện không hợp lệ").optional(),
});

export const adminRoleUpdateSchema = z.object({
  role: z.enum(["USER", "ORGANIZER", "ADMIN"], {
    errorMap: () => ({ message: "Vai trò không hợp lệ" }),
  }),
});

export const adminUserFiltersSchema = z.object({
  role: z.enum(["USER", "ORGANIZER", "ADMIN"]).optional(),
  enabled: z.boolean().optional(),
  search: z.string().optional(),
  page: z.number().min(0).default(0),
  size: z.number().min(1).max(100).default(10),
});

export type AdminUserCreateInput = z.infer<typeof adminUserCreateSchema>;
export type AdminUserUpdateInput = z.infer<typeof adminUserUpdateSchema>;
export type AdminRoleUpdateInput = z.infer<typeof adminRoleUpdateSchema>;
export type AdminUserFiltersInput = z.infer<typeof adminUserFiltersSchema>;


