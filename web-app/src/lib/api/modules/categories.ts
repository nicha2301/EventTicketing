import { http } from "../http";
import type { CategoryDto, ApiResponsePageCategoryDto } from "../generated/client";

export interface CategoriesListResponse {
  categories: CategoryDto[];
  totalPages: number;
  totalElements: number;
  currentPage: number;
}

export const getAllCategories = async (page = 0, size = 100): Promise<CategoriesListResponse> => {
  const response = await http<ApiResponsePageCategoryDto>({
    url: "/api/categories",
    method: "GET",
    params: {
      page,
      size,
    },
  });

  const { data } = response.data;
  if (!data) {
    throw new Error("Failed to fetch categories");
  }

  return {
    categories: data.content || [],
    totalPages: data.totalPages || 0,
    totalElements: data.totalElements || 0,
    currentPage: data.number || 0,
  };
};

export const getCategoryById = async (id: string): Promise<CategoryDto> => {
  const response = await http<{ data: CategoryDto }>({
    url: `/api/categories/${id}`,
    method: "GET",
  });

  const { data } = response.data;
  if (!data) {
    throw new Error("Category not found");
  }

  return data;
};
