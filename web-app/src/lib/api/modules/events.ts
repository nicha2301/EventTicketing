import { http } from "../http";
import type { EventDto, ApiResponsePageEventDto } from "../generated/client";

export interface EventsListResponse {
  events: EventDto[];
  totalPages: number;
  totalElements: number;
  currentPage: number;
}

export const listEvents = async (params?: {
  page?: number;
  size?: number;
  categoryId?: string;
  search?: string;
}): Promise<EventsListResponse> => {
  const response = await http<ApiResponsePageEventDto>({
    url: "/api/events",
    method: "GET",
    params: {
      page: params?.page || 0,
      size: params?.size || 10,
      categoryId: params?.categoryId,
      search: params?.search,
    },
  });

  const { data } = response.data;
  if (!data) {
    throw new Error("Failed to fetch events");
  }

  return {
    events: data.content || [],
    totalPages: data.totalPages || 0,
    totalElements: data.totalElements || 0,
    currentPage: data.number || 0,
  };
};

export const getEventById = async (id: string): Promise<EventDto> => {
  const response = await http<{ data: EventDto }>({
    url: `/api/events/${id}`,
    method: "GET",
  });

  const { data } = response.data;
  if (!data) {
    throw new Error("Event not found");
  }

  return data;
};

export const getFeaturedEvents = async (limit: number = 6): Promise<EventDto[]> => {
  const response = await http<{ data: EventDto[] }>({
    url: "/api/events/featured",
    method: "GET",
    params: { limit },
  });

  const { data } = response.data;
  return data || [];
};

export const getUpcomingEvents = async (limit: number = 6): Promise<EventDto[]> => {
  const response = await http<{ data: EventDto[] }>({
    url: "/api/events/upcoming",
    method: "GET",
    params: { limit },
  });

  const { data } = response.data;
  return data || [];
};
