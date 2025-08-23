"use client";

import { useQuery } from "@tanstack/react-query";
import {
  getAllUsers,
  getAllCategories,
  getAllLocations,
  getAllEvents,
} from "@/lib/api/modules/admin";

export function useSystemOverview() {
  const { data: usersData } = useQuery({
    queryKey: ["admin", "analytics", "users", "overview"],
    queryFn: async ({ signal }) => {
      const res = await getAllUsers(0, 1, undefined, undefined, undefined, signal);
      return res || {};
    },
    staleTime: 5 * 60 * 1000,
  });

  const { data: categoriesData } = useQuery({
    queryKey: ["admin", "analytics", "categories", "overview"],
    queryFn: async ({ signal }) => {
      const res = await getAllCategories(0, 1, undefined, undefined, signal);
      return res || {};
    },
    staleTime: 5 * 60 * 1000,
  });

  const { data: locationsData } = useQuery({
    queryKey: ["admin", "analytics", "locations", "overview"],
    queryFn: async ({ signal }) => {
      const res = await getAllLocations(0, 1, undefined, undefined, signal);
      return res || {};
    },
    staleTime: 5 * 60 * 1000,
  });

  const { data: eventsData } = useQuery({
    queryKey: ["admin", "analytics", "events", "overview"],
    queryFn: async ({ signal }) => {
      const res = await getAllEvents(0, 1, undefined, undefined, undefined, undefined, undefined, signal);
      return res || {};
    },
    staleTime: 5 * 60 * 1000,
  });

  return {
    totalUsers: usersData?.data?.totalElements || 0,
    totalCategories: categoriesData?.data?.totalElements || 0,
    totalLocations: locationsData?.data?.totalElements || 0,
    totalEvents: eventsData?.data?.totalElements || 0,
    isLoading: false, 
  };
}

export function useUserAnalytics(page = 0, size = 10) {
  return useQuery({
    queryKey: ["admin", "analytics", "users", page, size],
    queryFn: async ({ signal }) => {
      const res = await getAllUsers(page, size, undefined, undefined, undefined, signal);
      return res || {};
    },
    staleTime: 2 * 60 * 1000,
  });
}

export function useCategoryAnalytics(page = 0, size = 10) {
  return useQuery({
    queryKey: ["admin", "analytics", "categories", page, size],
    queryFn: async ({ signal }) => {
      const res = await getAllCategories(page, size, undefined, undefined, signal);
      return res || {};
    },
    staleTime: 2 * 60 * 1000,
  });
}

export function useLocationAnalytics(page = 0, size = 10) {
  return useQuery({
    queryKey: ["admin", "analytics", "locations", page, size],
    queryFn: async ({ signal }) => {
      const res = await getAllLocations(page, size, undefined, undefined, signal);
      return res || {};
    },
    staleTime: 2 * 60 * 1000, 
  });
}

export function useEventAnalytics(
  page = 0,
  size = 10,
  status?: string,
  categoryId?: string,
  locationId?: string
) {
  return useQuery({
    queryKey: ["admin", "analytics", "events", page, size, status, categoryId, locationId],
    queryFn: async ({ signal }) => {
      const res = await getAllEvents(page, size, status, categoryId, locationId, undefined, undefined, signal);
      return res || {};
    },
    staleTime: 2 * 60 * 1000, 
  });
}


