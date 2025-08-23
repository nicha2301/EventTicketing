import { z } from "zod";


export const adminCategoryCreateSchema = z.object({
  name: z.string().min(2, "Tên danh mục phải có ít nhất 2 ký tự").max(50, "Tên danh mục không được quá 50 ký tự"),
  description: z.string().max(255, "Mô tả không được quá 255 ký tự").optional(),
  iconUrl: z.string().url("URL icon không hợp lệ").optional(),
});

export const adminCategoryUpdateSchema = z.object({
  name: z.string().min(2, "Tên danh mục phải có ít nhất 2 ký tự").max(50, "Tên danh mục không được quá 50 ký tự").optional(),
  description: z.string().max(255, "Mô tả không được quá 255 ký tự").optional(),
  iconUrl: z.string().url("URL icon không hợp lệ").optional(),
});

export const adminCategoryFiltersSchema = z.object({
  isActive: z.boolean().optional(),
  search: z.string().optional(),
  page: z.number().min(0).default(0),
  size: z.number().min(1).max(100).default(10),
});

export const adminLocationCreateSchema = z.object({
  name: z.string().min(2, "Tên địa điểm phải có ít nhất 2 ký tự").max(100, "Tên địa điểm không được quá 100 ký tự"),
  address: z.string().min(5, "Địa chỉ phải có ít nhất 5 ký tự").max(255, "Địa chỉ không được quá 255 ký tự"),
  city: z.string().min(2, "Thành phố phải có ít nhất 2 ký tự").max(100, "Thành phố không được quá 100 ký tự"),
  country: z.string().min(2, "Quốc gia phải có ít nhất 2 ký tự").max(100, "Quốc gia không được quá 100 ký tự"),
  latitude: z.number().min(-90).max(90).optional(),
  longitude: z.number().min(-180).max(180).optional(),
  capacity: z.number().min(1, "Sức chứa phải lớn hơn 0").optional(),
});

export const adminLocationUpdateSchema = z.object({
  name: z.string().min(2, "Tên địa điểm phải có ít nhất 2 ký tự").max(100, "Tên địa điểm không được quá 100 ký tự").optional(),
  address: z.string().min(5, "Địa chỉ phải có ít nhất 5 ký tự").max(255, "Địa chỉ không được quá 255 ký tự").optional(),
  city: z.string().min(2, "Thành phố phải có ít nhất 2 ký tự").max(100, "Thành phố không được quá 100 ký tự").optional(),
  country: z.string().min(2, "Quốc gia phải có ít nhất 2 ký tự").max(100, "Quốc gia không được quá 100 ký tự").optional(),
  latitude: z.number().min(-90).max(90).optional(),
  longitude: z.number().min(-180).max(180).optional(),
  capacity: z.number().min(1, "Sức chứa phải lớn hơn 0").optional(),
});

export const adminLocationFiltersSchema = z.object({
  city: z.string().optional(),
  search: z.string().optional(),
  page: z.number().min(0).default(0),
  size: z.number().min(1).max(100).default(10),
});

export type AdminCategoryCreateInput = z.infer<typeof adminCategoryCreateSchema>;
export type AdminCategoryUpdateInput = z.infer<typeof adminCategoryUpdateSchema>;
export type AdminCategoryFiltersInput = z.infer<typeof adminCategoryFiltersSchema>;

export type AdminLocationCreateInput = z.infer<typeof adminLocationCreateSchema>;
export type AdminLocationUpdateInput = z.infer<typeof adminLocationUpdateSchema>;
export type AdminLocationFiltersInput = z.infer<typeof adminLocationFiltersSchema>;


