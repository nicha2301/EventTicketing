"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  getReportedRatings,
  getRatingById,
  updateRatingStatus,
  deleteRating,
  getRatingsByStatus,
  type RatingSummaryDto,
} from "@/lib/api/modules/moderation";
import { toast } from "sonner";

export function useReportedRatings(page = 0, size = 10) {
  return useQuery({
    queryKey: ["admin", "moderation", "ratings", "reported", page, size],
    queryFn: async ({ signal }) => {
      const res = await getReportedRatings(page, size, signal);
      return res || {};
    },
    placeholderData: (prev) => prev,
  });
}

export function useRatingsByStatus(
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'REPORTED',
  page = 0,
  size = 10
) {
  return useQuery({
    queryKey: ["admin", "moderation", "ratings", "status", status, page, size],
    enabled: !!status,
    queryFn: async ({ signal }) => {
      const res = await getRatingsByStatus(status, page, size, signal);
      return res || {};
    },
    placeholderData: (prev) => prev,
  });
}

export function useRatingById(ratingId?: string) {
  return useQuery({
    queryKey: ["admin", "moderation", "ratings", "detail", ratingId],
    enabled: !!ratingId,
    queryFn: async ({ signal }) => {
      const res = await getRatingById(ratingId as string, signal);
      return res || {};
    },
    placeholderData: (prev) => prev,
  });
}

export function useUpdateRatingStatus() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationKey: ["admin", "moderation", "ratings", "status"],
    mutationFn: async ({
      ratingId,
      status,
    }: {
      ratingId: string;
      status: 'APPROVED' | 'REJECTED';
    }) => {
      return updateRatingStatus(ratingId, { status });
    },
    onSuccess: (_, { status }) => {
      queryClient.invalidateQueries({ queryKey: ["admin", "moderation", "ratings"] });
      toast.success(`Đánh giá đã được ${status === 'APPROVED' ? 'chấp nhận' : 'từ chối'}!`);
    },
    onError: (error: any) => {
      toast.error(error?.message || "Không thể cập nhật trạng thái đánh giá");
    },
  });
}

export function useDeleteRating() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationKey: ["admin", "moderation", "ratings", "delete"],
    mutationFn: async (ratingId: string) => {
      return deleteRating(ratingId);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin", "moderation", "ratings"] });
      toast.success("Đánh giá đã được xóa thành công!");
    },
    onError: (error: any) => {
      toast.error(error?.message || "Không thể xóa đánh giá");
    },
  });
}
