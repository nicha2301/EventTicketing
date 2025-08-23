"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  getAllUsers,
  getUserById,
  createUser,
  updateUser,
  deleteUser,
  updateUserRole,
  activateUser,
  deactivateUser,
  type UserSummaryDto,
} from "@/lib/api/modules/admin";
import { toast } from "sonner";

export function useAllUsers(
  page = 0,
  size = 10,
  role?: string,
  enabled?: boolean,
  search?: string
) {
  return useQuery({
    queryKey: ["admin", "users", page, size, role, enabled, search],
    queryFn: async ({ signal }) => {
      const res = await getAllUsers(page, size, role, enabled, search, signal);
      return res || {};
    },
    placeholderData: (prev) => prev,
  });
}

export function useUserById(userId?: string) {
  return useQuery({
    queryKey: ["admin", "users", "detail", userId],
    enabled: !!userId,
    queryFn: async ({ signal }) => {
      const res = await getUserById(userId as string, signal);
      return res || {};
    },
    placeholderData: (prev) => prev,
  });
}

export function useCreateUser() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationKey: ["admin", "users", "create"],
    mutationFn: async (userData: {
      fullName: string;
      email: string;
      password: string;
      phoneNumber?: string;
      role: string;
      enabled: boolean;
    }) => {
      return createUser(userData);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "users"] });
      toast.success("Người dùng đã được tạo thành công!");
    },
    onError: (error: any) => {
      toast.error(error?.message || "Không thể tạo người dùng");
    },
  });
}

export function useUpdateUser() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationKey: ["admin", "users", "update"],
    mutationFn: async ({
      id,
      userData,
    }: {
      id: string;
      userData: {
        fullName?: string;
        phoneNumber?: string;
        profilePictureUrl?: string;
      };
    }) => {
      return updateUser(id, userData);
    },
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: ["admin", "users"] });
      queryClient.invalidateQueries({ queryKey: ["admin", "users", "detail", id] });
      toast.success("Thông tin người dùng đã được cập nhật!");
    },
    onError: (error: any) => {
      toast.error(error?.message || "Không thể cập nhật người dùng");
    },
  });
}

export function useDeleteUser() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationKey: ["admin", "users", "delete"],
    mutationFn: async (id: string) => {
      return deleteUser(id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "users"] });
      toast.success("Người dùng đã được xóa thành công!");
    },
    onError: (error: any) => {
      toast.error(error?.message || "Không thể xóa người dùng");
    },
  });
}

export function useUpdateUserRole() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationKey: ["admin", "users", "role"],
    mutationFn: async ({ id, role }: { id: string; role: string }) => {
      return updateUserRole(id, { role });
    },
    onSuccess: (_, { id }) => {
      queryClient.invalidateQueries({ queryKey: ["admin", "users"] });
      queryClient.invalidateQueries({ queryKey: ["admin", "users", "detail", id] });
      toast.success("Vai trò người dùng đã được cập nhật!");
    },
    onError: (error: any) => {
      toast.error(error?.message || "Không thể cập nhật vai trò người dùng");
    },
  });
}

export function useActivateUser() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationKey: ["admin", "users", "activate"],
    mutationFn: async (id: string) => {
      return activateUser(id);
    },
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: ["admin", "users"] });
      queryClient.invalidateQueries({ queryKey: ["admin", "users", "detail", id] });
      toast.success("Tài khoản người dùng đã được kích hoạt!");
    },
    onError: (error: any) => {
      toast.error(error?.message || "Không thể kích hoạt tài khoản người dùng");
    },
  });
}

export function useDeactivateUser() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationKey: ["admin", "users", "deactivate"],
    mutationFn: async (id: string) => {
      return deactivateUser(id);
    },
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: ["admin", "users"] });
      queryClient.invalidateQueries({ queryKey: ["admin", "users", "detail", id] });
      toast.success("Tài khoản người dùng đã được vô hiệu hóa!");
    },
    onError: (error: any) => {
      toast.error(error?.message || "Không thể vô hiệu hóa tài khoản người dùng");
    },
  });
}
