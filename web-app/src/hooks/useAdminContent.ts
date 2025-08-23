"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  getAllCategories,
  createCategory,
  updateCategory,
  deleteCategory,
  activateCategory,
  deactivateCategory,
  getAllLocations,
  createLocation,
  updateLocation,
  deleteLocation,
  getAllEvents,
  type CategorySummaryDto,
  type LocationSummaryDto,
} from "@/lib/api/modules/admin";
import { toast } from "sonner";

export function useAllEvents(
  page = 0,
  size = 10,
  status?: string,
  categoryId?: string,
  locationId?: string,
  search?: string
) {
  return useQuery({
    queryKey: ["admin", "events", page, size, status, categoryId, locationId, search],
    queryFn: async ({ signal }) => {
      const res = await getAllEvents(page, size, status, categoryId, locationId, undefined, search, signal);
      return res || {};
    },
    placeholderData: (prev) => prev,
  });
}

export function useAllCategories(
  page = 0,
  size = 10,
  isActive?: boolean,
  search?: string
) {
  return useQuery({
    queryKey: ["admin", "categories", page, size, isActive, search],
    queryFn: async ({ signal }) => {
      const res = await getAllCategories(page, size, isActive, search, signal);
      return res || {};
    },
    placeholderData: (prev) => prev,
  });
}

export function useCreateCategory() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationKey: ["admin", "categories", "create"],
    mutationFn: async (categoryData: {
      name: string;
      description?: string;
      iconUrl?: string;
    }) => {
      return createCategory({
        ...categoryData,
        isActive: true, 
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "categories"] });
      toast.success("Danh mục đã được tạo thành công!");
    },
    onError: (error: any) => {
      toast.error(error?.message || "Không thể tạo danh mục");
    },
  });
}

export function useUpdateCategory() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationKey: ["admin", "categories", "update"],
    mutationFn: async ({
      id,
      categoryData,
    }: {
      id: string;
      categoryData: {
        name?: string;
        description?: string;
        iconUrl?: string;
        isActive?: boolean;
      };
    }) => {
      const updateData: any = {
        name: categoryData.name || "", 
        isActive: categoryData.isActive !== undefined ? categoryData.isActive : true, // Required field
      };
      
      if (categoryData.description !== undefined) updateData.description = categoryData.description;
      if (categoryData.iconUrl !== undefined) updateData.iconUrl = categoryData.iconUrl;
      
      return updateCategory(id, updateData);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "categories"] });
      toast.success("Danh mục đã được cập nhật thành công!");
    },
    onError: (error: any) => {
      toast.error(error?.message || "Không thể cập nhật danh mục");
    },
  });
}

export function useDeleteCategory() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationKey: ["admin", "categories", "delete"],
    mutationFn: async (id: string) => {
      return deleteCategory(id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "categories"] });
      toast.success("Danh mục đã được xóa thành công!");
    },
    onError: (error: any) => {
      toast.error(error?.message || "Không thể xóa danh mục");
    },
  });
}

export function useActivateCategory() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationKey: ["admin", "categories", "activate"],
    mutationFn: async (id: string) => {
      return activateCategory(id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "categories"] });
      toast.success("Danh mục đã được kích hoạt!");
    },
    onError: (error: any) => {
      toast.error(error?.message || "Không thể kích hoạt danh mục");
    },
  });
}

export function useDeactivateCategory() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationKey: ["admin", "categories", "deactivate"],
    mutationFn: async (id: string) => {
      return deactivateCategory(id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "categories"] });
      toast.success("Danh mục đã được vô hiệu hóa!");
    },
    onError: (error: any) => {
      toast.error(error?.message || "Không thể vô hiệu hóa danh mục");
    },
  });
}

export function useAllLocations(
  page = 0,
  size = 10,
  city?: string,
  search?: string
) {
  return useQuery({
    queryKey: ["admin", "locations", page, size, city, search],
    queryFn: async ({ signal }) => {
      const res = await getAllLocations(page, size, city, search, signal);
      return res || {};
    },
    placeholderData: (prev) => prev,
  });
}

export function useCreateLocation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationKey: ["admin", "locations", "create"],
    mutationFn: async (locationData: {
      name: string;
      address: string;
      city: string;
      country: string;
      latitude?: number;
      longitude?: number;
      capacity?: number;
    }) => {
      return createLocation({
        ...locationData,
        latitude: locationData.latitude || 0, 
        longitude: locationData.longitude || 0,
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "locations"] });
      toast.success("Địa điểm đã được tạo thành công!");
    },
    onError: (error: any) => {
      toast.error(error?.message || "Không thể tạo địa điểm");
    },
  });
}

export function useUpdateLocation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationKey: ["admin", "locations", "update"],
    mutationFn: async ({
      id,
      locationData,
    }: {
      id: string;
      locationData: {
        name?: string;
        address?: string;
        city?: string;
        country?: string;
        latitude?: number;
        longitude?: number;
        capacity?: number;
      };
    }) => {
      const updateData: any = {};
      if (locationData.name !== undefined) updateData.name = locationData.name;
      if (locationData.address !== undefined) updateData.address = locationData.address;
      if (locationData.city !== undefined) updateData.city = locationData.city;
      if (locationData.country !== undefined) updateData.country = locationData.country;
      if (locationData.latitude !== undefined) updateData.latitude = locationData.latitude;
      if (locationData.longitude !== undefined) updateData.longitude = locationData.longitude;
      if (locationData.capacity !== undefined) updateData.capacity = locationData.capacity;
      
      return updateLocation(id, updateData);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "locations"] });
      toast.success("Địa điểm đã được cập nhật thành công!");
    },
    onError: (error: any) => {
      toast.error(error?.message || "Không thể cập nhật địa điểm");
    },
  });
}

export function useDeleteLocation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationKey: ["admin", "locations", "delete"],
    mutationFn: async (id: string) => {
      return deleteLocation(id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "locations"] });
      toast.success("Địa điểm đã được xóa thành công!");
    },
    onError: (error: any) => {
      toast.error(error?.message || "Không thể xóa địa điểm");
    },
  });
}
