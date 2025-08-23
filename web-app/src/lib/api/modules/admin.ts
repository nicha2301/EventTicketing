import { http } from "../http";
import {
  getUserById,
  createUser,
  updateUser,
  deleteUser,
  updateUserRole,
  activateUser,
  deactivateUser,
  createCategory,
  updateCategory,
  deleteCategory,
  activateCategory,
  deactivateCategory,
  createLocation,
  updateLocation,
  deleteLocation,
  publishEvent,
  cancelEvent,
  deleteEvent,
} from "../generated/client";

// Temporary interfaces for Admin types
interface UserSummaryDto {
  id: string;
  fullName: string;
  email: string;
  phoneNumber?: string;
  role: 'USER' | 'ORGANIZER' | 'ADMIN';
  enabled: boolean;
  createdAt: string;
  lastLoginAt?: string;
  profilePictureUrl?: string;
}

interface CategorySummaryDto {
  id: string;
  name: string;
  description?: string;
  iconUrl?: string;
  isActive: boolean;
  createdAt: string;
  eventCount?: number;
}

interface LocationSummaryDto {
  id: string;
  name: string;
  address: string;
  city: string;
  country: string;
  latitude?: number;
  longitude?: number;
  capacity?: number;
  createdAt: string;
  eventCount?: number;
}

interface EventSummaryDto {
  id: string;
  title: string;
  description?: string;
  startDate: string;
  endDate: string;
  status: 'DRAFT' | 'PUBLISHED' | 'CANCELLED' | 'COMPLETED';
  organizerId: string;
  organizerName: string;
  categoryId: string;
  categoryName: string;
  locationId: string;
  locationName: string;
  ticketPrice?: number;
  totalRevenue?: number;
  createdAt: string;
}

interface ApiResponsePageUserSummaryDto {
  data: {
    content: UserSummaryDto[];
    totalPages: number;
    totalElements: number;
    number: number;
    size: number;
  };
  message: string;
  success: boolean;
}

interface ApiResponsePageCategorySummaryDto {
  data: {
    content: CategorySummaryDto[];
    totalPages: number;
    totalElements: number;
    number: number;
    size: number;
  };
  message: string;
  success: boolean;
}

interface ApiResponsePageLocationSummaryDto {
  data: {
    content: LocationSummaryDto[];
    totalPages: number;
    totalElements: number;
    number: number;
    size: number;
  };
  message: string;
  success: boolean;
}

interface ApiResponsePageEventSummaryDto {
  data: {
    content: EventSummaryDto[];
    totalPages: number;
    totalElements: number;
    number: number;
    size: number;
  };
  message: string;
  success: boolean;
}

// Pageable APIs - Custom wrappers
export const getAllUsers = async (
  page = 0,
  size = 10,
  role?: string,
  enabled?: boolean,
  search?: string,
  signal?: AbortSignal
) => {
  const params = new URLSearchParams();
  params.set('page', String(page));
  params.set('size', String(size));
  if (role) params.set('role', role);
  if (enabled !== undefined) params.set('enabled', String(enabled));
  if (search) params.set('search', search);

  const response = await http<ApiResponsePageUserSummaryDto>({
    url: `/api/users?${params.toString()}`,
    method: 'GET',
    signal,
  });
  return response.data;
};

export const getAllCategories = async (
  page = 0,
  size = 10,
  isActive?: boolean,
  search?: string,
  signal?: AbortSignal
) => {
  const params = new URLSearchParams();
  params.set('page', String(page));
  params.set('size', String(size));
  if (isActive !== undefined) params.set('isActive', String(isActive));
  if (search) params.set('search', search);

  const response = await http<ApiResponsePageCategorySummaryDto>({
    url: `/api/categories?${params.toString()}`,
    method: 'GET',
    signal,
  });
  return response.data;
};

export const getAllLocations = async (
  page = 0,
  size = 10,
  city?: string,
  search?: string,
  signal?: AbortSignal
) => {
  const params = new URLSearchParams();
  params.set('page', String(page));
  params.set('size', String(size));
  if (city) params.set('city', city);
  if (search) params.set('search', search);

  const response = await http<ApiResponsePageLocationSummaryDto>({
    url: `/api/locations?${params.toString()}`,
    method: 'GET',
    signal,
  });
  return response.data;
};

export const getAllEvents = async (
  page = 0,
  size = 10,
  status?: string,
  categoryId?: string,
  locationId?: string,
  organizerId?: string,
  search?: string,
  signal?: AbortSignal
) => {
  const params = new URLSearchParams();
  params.set('page', String(page));
  params.set('size', String(size));
  if (status) params.set('status', status);
  if (categoryId) params.set('categoryId', categoryId);
  if (locationId) params.set('locationId', locationId);
  if (organizerId) params.set('organizerId', organizerId);
  if (search) params.set('search', search);

  const response = await http<ApiResponsePageEventSummaryDto>({
    url: `/api/events?${params.toString()}`,
    method: 'GET',
    signal,
  });
  return response.data;
};

export {
  getUserById,
  createUser,
  updateUser,
  deleteUser,
  updateUserRole,
  activateUser,
  deactivateUser,
  createCategory,
  updateCategory,
  deleteCategory,
  activateCategory,
  deactivateCategory,
  createLocation,
  updateLocation,
  deleteLocation,
  publishEvent,
  cancelEvent,
  deleteEvent,
};

export type {
  UserSummaryDto,
  CategorySummaryDto,
  LocationSummaryDto,
  EventSummaryDto,
  ApiResponsePageUserSummaryDto,
  ApiResponsePageCategorySummaryDto,
  ApiResponsePageLocationSummaryDto,
  ApiResponsePageEventSummaryDto,
};
