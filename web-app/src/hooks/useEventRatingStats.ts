'use client'

import { useQuery } from '@tanstack/react-query'
import { api } from '@/lib/api/http'

export function useEventRatingStats(eventId?: string) {
  const query = useQuery({
    queryKey: ['rating-stats', eventId],
    queryFn: async () => {
      if (!eventId) throw new Error('Event ID is required')
      
      const response = await api.get(`/api/ratings/events/${eventId}/statistics`)
      return response.data
    },
    enabled: !!eventId,
    staleTime: 2 * 60 * 1000, // Cache 2 phút
    gcTime: 5 * 60 * 1000,
  })

  return {
    averageRating: query.data?.averageRating || 0,
    totalRatings: query.data?.totalRatings || 0,
    isLoading: query.isLoading,
    error: query.error
  }
}
