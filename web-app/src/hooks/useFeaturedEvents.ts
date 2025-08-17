"use client";

import { useQuery } from "@tanstack/react-query";
import { getFeaturedEvents } from "@/lib/api/modules/events";

export function useFeaturedEvents(limit = 6) {
  return useQuery({
    queryKey: ['featured-events', limit],
    queryFn: () => getFeaturedEvents(limit),
    staleTime: 5 * 60 * 1000,
  });
}


