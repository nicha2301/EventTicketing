import { http } from "../http";
import {
  getRatingById,
  updateRatingStatus,
  deleteRating,
} from "../generated/client";

interface RatingSummaryDto {
  id: string;
  rating: number;
  comment?: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'REPORTED';
  reportReason?: string;
  reportedAt?: string;
  userId: string;
  userName: string;
  userEmail: string;
  eventId: string;
  eventTitle: string;
  createdAt: string;
  updatedAt: string;
}

interface ApiResponsePageRatingSummaryDto {
  data: {
    content: RatingSummaryDto[];
    totalPages: number;
    totalElements: number;
    number: number;
    size: number;
  };
  message: string;
  success: boolean;
}

export const getReportedRatings = async (
  page = 0,
  size = 10,
  signal?: AbortSignal
) => {
  const params = new URLSearchParams();
  params.set('page', String(page));
  params.set('size', String(size));

  const response = await http<ApiResponsePageRatingSummaryDto>({
    url: `/api/ratings/admin/reported?${params.toString()}`,
    method: 'GET',
    signal,
  });
  return response.data;
};

export const getRatingsByStatus = async (
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'REPORTED',
  page = 0,
  size = 10,
  signal?: AbortSignal
) => {
  const params = new URLSearchParams();
  params.set('page', String(page));
  params.set('size', String(size));

  const response = await http<ApiResponsePageRatingSummaryDto>({
    url: `/api/ratings/admin/status/${status}?${params.toString()}`,
    method: 'GET',
    signal,
  });
  return response.data;
};

export {
  getRatingById,
  updateRatingStatus,
  deleteRating,
};

export type {
  RatingSummaryDto,
  ApiResponsePageRatingSummaryDto,
};
