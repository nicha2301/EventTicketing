import { z } from "zod";

// Schema cho cập nhật thông tin cá nhân
export const profileUpdateSchema = z.object({
  fullName: z
    .string()
    .min(2, "Họ và tên phải có ít nhất 2 ký tự")
    .max(100, "Họ và tên không được quá 100 ký tự")
    .optional(),
  
  phoneNumber: z
    .string()
    .regex(/^(\+84|84|0)[3-9]\d{8}$/, "Số điện thoại không hợp lệ")
    .optional()
    .or(z.literal("")),
    
  profilePictureUrl: z
    .string()
    .url("URL ảnh không hợp lệ")
    .optional()
    .or(z.literal("")),
});

// Schema cho đổi mật khẩu
export const passwordChangeSchema = z.object({
  currentPassword: z
    .string()
    .min(1, "Vui lòng nhập mật khẩu hiện tại"),
    
  newPassword: z
    .string()
    .min(8, "Mật khẩu mới phải có ít nhất 8 ký tự")
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/, "Mật khẩu phải chứa ít nhất 1 chữ thường, 1 chữ hoa và 1 số"),
    
  confirmPassword: z
    .string()
    .min(1, "Vui lòng xác nhận mật khẩu"),
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: "Mật khẩu xác nhận không khớp",
  path: ["confirmPassword"],
});

export type ProfileUpdateFormData = z.infer<typeof profileUpdateSchema>;
export type PasswordChangeFormData = z.infer<typeof passwordChangeSchema>;
