'use client'

import { useQuery } from '@tanstack/react-query'
import { api } from '@/lib/api/http'

interface UseEventRatingsOptions {
  eventId: string
  page?: number
  size?: number
}

export function useEventRatingsList({ eventId, page = 0, size = 10 }: UseEventRatingsOptions) {
  const ratingsQuery = useQuery({
    queryKey: ['event-ratings', eventId, page, size],
    queryFn: async () => {
      const response = await api.get(`/api/ratings/events/${eventId}?page=${page}&size=${size}`)
      return response.data
    },
    enabled: !!eventId,
    staleTime: 2 * 60 * 1000,
  })

  const statisticsQuery = useQuery({
    queryKey: ['rating-stats', eventId],
    queryFn: async () => {
      const response = await api.get(`/api/ratings/events/${eventId}/statistics`)
      return response.data
    },
    enabled: !!eventId,
    staleTime: 2 * 60 * 1000,
  })

  return {
    ratings: ratingsQuery.data?.ratings || [],
    statistics: statisticsQuery.data,
    isLoading: ratingsQuery.isLoading || statisticsQuery.isLoading,
    error: ratingsQuery.error || statisticsQuery.error,
    hasNextPage: (ratingsQuery.data?.currentPage || 0) < (ratingsQuery.data?.totalPages || 0) - 1,
    totalElements: ratingsQuery.data?.totalItems || 0,
    currentPage: ratingsQuery.data?.currentPage || 0,
    totalPages: ratingsQuery.data?.totalPages || 0,
    refetch: () => {
      ratingsQuery.refetch()
      statisticsQuery.refetch()
    }
  }
}