"use client";

import { getAllCategories, getAllLocations } from "@/lib/api";
import { getFeaturedEvents, getOrganizerEvents } from "@/lib/api/modules/events";
import { useAuthStore } from "@/store/auth";
import { useQuery } from "@tanstack/react-query";

export function useFeaturedEvents(limit = 6) {
  return useQuery({
    queryKey: ['featured-events', limit],
    queryFn: () => getFeaturedEvents(limit),
    staleTime: 5 * 60 * 1000,
  });
}

export function useOrganizerEvents(page: number, size: number) {
  const { currentUser } = useAuthStore();
  const organizerId = currentUser?.id ?? "";
  return useQuery({
    queryKey: ["organizer-events", organizerId, page, size],
    enabled: !!organizerId && (currentUser?.role === 'ORGANIZER' || currentUser?.role === 'ADMIN'),
    queryFn: async ({ signal }) => (await getOrganizerEvents(organizerId, page, size, signal)).data,
    placeholderData: (prev) => prev,
  });
}

export function useCategories() {
  return useQuery({
    queryKey: ["categories"],
    queryFn: async () => await getAllCategories(0, 100),
    staleTime: 5 * 60 * 1000, 
  });
}

export function useLocations(page = 0, size = 20) {
  return useQuery({
    queryKey: ["locations", page, size],
    queryFn: async () => await getAllLocations(page, size),
    placeholderData: (prev) => prev,
  });
}


