"use client";

import { useQuery } from "@tanstack/react-query";
import { getUpcomingEvents } from "@/lib/api/modules/events";

export function useUpcomingEvents(limit = 6) {
  return useQuery({
    queryKey: ['upcoming-events', limit],
    queryFn: () => getUpcomingEvents(limit),
    staleTime: 5 * 60 * 1000,
  });
}


