import { http } from "../http";
import type { LocationDto, ApiResponsePageLocationDto } from "../generated/client";

export interface LocationsListResponse {
  locations: LocationDto[];
  totalPages: number;
  totalElements: number;
  currentPage: number;
}

export const getAllLocations = async (page = 0, size = 100): Promise<LocationsListResponse> => {
  const response = await http<ApiResponsePageLocationDto>({
    url: "/api/locations",
    method: "GET",
    params: {
      page,
      size,
    },
  });

  const { data } = response.data;
  if (!data) {
    throw new Error("Failed to fetch locations");
  }

  return {
    locations: data.content || [],
    totalPages: data.totalPages || 0,
    totalElements: data.totalElements || 0,
    currentPage: data.number || 0,
  };
};

export const getLocationById = async (id: string): Promise<LocationDto> => {
  const response = await http<{ data: LocationDto }>({
    url: `/api/locations/${id}`,
    method: "GET",
  });

  const { data } = response.data;
  if (!data) {
    throw new Error("Location not found");
  }

  return data;
};

export const getPopularLocations = async (limit = 10): Promise<LocationDto[]> => {
  const response = await http<{ data: LocationDto[] }>({
    url: "/api/locations/popular",
    method: "GET",
    params: { limit },
  });

  const { data } = response.data;
  return data || [];
};
