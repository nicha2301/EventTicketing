import { api } from "./http";
import { EventDto } from "./generated/client";

type ApiResponse<T> = {
  success: boolean;
  message?: string;
  data: T;
};

export async function fetchFeaturedEvents(limit = 6) {
  const res = await api.get<ApiResponse<EventDto[]>>(`/api/events/featured`, {
    params: { limit },
  });
  return res.data.data;
}

export async function fetchUpcomingEvents(limit = 6) {
  const res = await api.get<ApiResponse<EventDto[]>>(`/api/events/upcoming`, {
    params: { limit },
  });
  return res.data.data;
}

export async function fetchEventById(id: string) {
  const res = await api.get<ApiResponse<EventDto>>(`/api/events/${id}`);
  return res.data.data;
}

export type Page<T> = {
  content: T[];
  totalElements?: number;
  totalPages?: number;
  size?: number;
  number?: number;
};

export async function searchEvents(params: { q: string; page?: number; size?: number }) {
  const res = await api.get<ApiResponse<Page<EventDto>>>(`/api/events/search`, {
    params: { query: params.q, page: params.page ?? 0, size: params.size ?? 12 },
  });
  return res.data.data;
}


