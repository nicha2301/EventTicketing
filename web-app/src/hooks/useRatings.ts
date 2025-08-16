'use client'

import { 
  useGetRatingsByEventId,
  useGetRatingsByCurrentUser,
  useGetRatingById,
  useCreateOrUpdateRating,
  useUpdateRating,
  useDeleteRating,
  useGetRatingStatistics,
  useGetUserRatingForEvent,
  RatingRequest,
  RatingUpdateRequest
} from '@/lib/api/generated/client'
import { useAuthStore } from '@/store/auth'
import { toast } from 'sonner'
import { useQueryClient } from '@tanstack/react-query'

interface UseEventRatingsOptions {
  eventId: string
  page?: number
  size?: number
}

interface UseUserRatingsOptions {
  page?: number
  size?: number
}

export function useEventRatings({ eventId, page = 0, size = 10 }: UseEventRatingsOptions) {
  const { isAuthenticated } = useAuthStore()
  
  // Get ratings for a specific event
  const ratingsQuery = useGetRatingsByEventId(
    eventId,
    { page, size },
    {
      query: {
        enabled: !!eventId,
        staleTime: 5 * 60 * 1000,
        gcTime: 10 * 60 * 1000,
      }
    }
  )

  // Get rating statistics for event
  const statisticsQuery = useGetRatingStatistics(
    eventId,
    {
      query: {
        enabled: !!eventId,
        staleTime: 5 * 60 * 1000,
      }
    }
  )

  return {
    ratings: ratingsQuery.data?.data?.ratings || [],
    statistics: statisticsQuery.data?.data,
    isLoading: ratingsQuery.isLoading || statisticsQuery.isLoading,
    error: ratingsQuery.error || statisticsQuery.error,
    hasNextPage: (ratingsQuery.data?.data?.currentPage || 0) < (ratingsQuery.data?.data?.totalPages || 0) - 1,
    totalElements: ratingsQuery.data?.data?.totalItems || 0,
    currentPage: ratingsQuery.data?.data?.currentPage || 0,
    totalPages: ratingsQuery.data?.data?.totalPages || 0,
    refetch: () => {
      ratingsQuery.refetch()
      statisticsQuery.refetch()
    }
  }
}

export function useUserRatings({ page = 0, size = 10 }: UseUserRatingsOptions) {
  const { isAuthenticated } = useAuthStore()
  
  const ratingsQuery = useGetRatingsByCurrentUser(
    { page, size },
    {
      query: {
        enabled: isAuthenticated,
        staleTime: 5 * 60 * 1000,
      }
    }
  )

  return {
    ratings: ratingsQuery.data?.data?.ratings || [],
    isLoading: ratingsQuery.isLoading,
    error: ratingsQuery.error,
    hasNextPage: (ratingsQuery.data?.data?.currentPage || 0) < (ratingsQuery.data?.data?.totalPages || 0) - 1,
    totalElements: ratingsQuery.data?.data?.totalItems || 0,
    currentPage: ratingsQuery.data?.data?.currentPage || 0,
    totalPages: ratingsQuery.data?.data?.totalPages || 0,
    refetch: ratingsQuery.refetch
  }
}

export function useUserRatingForEvent(eventId: string) {
  const { isAuthenticated, currentUser } = useAuthStore()
  
  const ratingQuery = useGetUserRatingForEvent(
    eventId,
    currentUser?.id || '',
    {
      query: {
        enabled: isAuthenticated && !!eventId && !!currentUser?.id,
        staleTime: 5 * 60 * 1000,
      }
    }
  )

  return {
    rating: ratingQuery.data?.data,
    isLoading: ratingQuery.isLoading,
    error: ratingQuery.error,
    refetch: ratingQuery.refetch
  }
}

export function useRatingActions() {
  const queryClient = useQueryClient()
  const { currentUser } = useAuthStore()
  
  const invalidateRatingQueries = (eventId?: string, userId?: string) => {
    if (eventId) {
      queryClient.invalidateQueries({ 
        queryKey: [`/api/ratings/events/${eventId}`] 
      })
      queryClient.invalidateQueries({ 
        queryKey: [`/api/ratings/events/${eventId}/statistics`] 
      })
      if (userId) {
        queryClient.invalidateQueries({ 
          queryKey: [`/api/ratings/events/${eventId}/user/${userId}`] 
        })
      } else {
        queryClient.invalidateQueries({ 
          predicate: (query) => {
            const queryKey = query.queryKey[0] as string
            return queryKey?.includes(`/api/ratings/events/${eventId}/user/`)
          }
        })
      }
    } else {
      queryClient.invalidateQueries({ 
        predicate: (query) => {
          const queryKey = query.queryKey[0] as string
          return queryKey?.includes('/api/ratings')
        }
      })
    }
  }

  const createMutation = useCreateOrUpdateRating({
    mutation: {
      onSuccess: (data, variables) => {
        toast.success('Đánh giá đã được gửi thành công!')
        invalidateRatingQueries(variables.data.eventId, currentUser?.id)
      },
      onError: (error: any) => {
        toast.error(error.response?.data?.message || 'Có lỗi xảy ra khi gửi đánh giá')
      }
    }
  })

  const updateMutation = useUpdateRating({
    mutation: {
      onSuccess: () => {
        toast.success('Đánh giá đã được cập nhật!')
        invalidateRatingQueries()
      },
      onError: (error: any) => {
        toast.error(error.response?.data?.message || 'Có lỗi xảy ra khi cập nhật đánh giá')
      }
    }
  })

  const deleteMutation = useDeleteRating({
    mutation: {
      onSuccess: () => {
        toast.success('Đánh giá đã được xóa!')
        invalidateRatingQueries()
      },
      onError: (error: any) => {
        toast.error(error.response?.data?.message || 'Có lỗi xảy ra khi xóa đánh giá')
      }
    }
  })

  const createOrUpdateRating = async (data: RatingRequest) => {
    return createMutation.mutateAsync({ data })
  }

  const updateRating = async (ratingId: string, data: RatingUpdateRequest) => {
    return updateMutation.mutateAsync({ ratingId, data })
  }

  const deleteRating = async (ratingId: string) => {
    return deleteMutation.mutateAsync({ ratingId })
  }

  return {
    createOrUpdateRating,
    updateRating,
    deleteRating,
    isCreating: createMutation.isPending,
    isUpdating: updateMutation.isPending,
    isDeleting: deleteMutation.isPending,
    isLoading: createMutation.isPending || updateMutation.isPending || deleteMutation.isPending
  }
}
