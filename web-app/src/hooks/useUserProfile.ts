"use client";

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { getCurrentUser, updateCurrentUser, changePassword } from "@/lib/api/generated/client";
import type { UserUpdateDto, PasswordChangeDto } from "@/lib/api/generated/client";
import { useInitialization } from "@/hooks/useInitialization";

// Hook lấy thông tin user hiện tại
export function useUserProfile() {
  const { isAuthenticated } = useInitialization();
  
  return useQuery({
    queryKey: ['user-profile'],
    queryFn: async () => {
      const response = await getCurrentUser();
      return response.data.data;
    },
    enabled: isAuthenticated,
    staleTime: 5 * 60 * 1000, 
  });
}

// Hook cập nhật profile
export function useUpdateProfile() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: UserUpdateDto) => {
      const response = await updateCurrentUser(data);
      return response.data;
    },
    onSuccess: (data) => {
      // Cập nhật cache
      queryClient.setQueryData(['user-profile'], data.data);
      // Update auth store nếu cần
      queryClient.invalidateQueries({ queryKey: ['user-profile'] });
      toast.success("Cập nhật thông tin thành công!");
    },
    onError: (error: any) => {
      const message = error?.response?.data?.message || "Có lỗi xảy ra khi cập nhật thông tin";
      toast.error(message);
    },
  });
}

// Hook đổi mật khẩu  
export function useChangePassword() {
  return useMutation({
    mutationFn: async (data: PasswordChangeDto) => {
      const response = await changePassword(data);
      return response.data;
    },
    onSuccess: () => {
      toast.success("Đổi mật khẩu thành công!");
    },
    onError: (error: any) => {
      const message = error?.response?.data?.message || "Có lỗi xảy ra khi đổi mật khẩu";
      toast.error(message);
    },
  });
}
