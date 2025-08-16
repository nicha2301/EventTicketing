"use client";

import { useGetUpcomingEvents } from "@/lib/api/generated/client";

export function useUpcomingEvents(limit = 6) {
  return useGetUpcomingEvents({ limit }, {
    query: {
      staleTime: 5 * 60 * 1000,
    }
  });
}


