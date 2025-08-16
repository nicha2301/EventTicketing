"use client";

import { useGetFeaturedEvents } from "@/lib/api/generated/client";

export function useFeaturedEvents(limit = 6) {
  return useGetFeaturedEvents({ limit }, {
    query: {
      staleTime: 5 * 60 * 1000,
    }
  });
}


