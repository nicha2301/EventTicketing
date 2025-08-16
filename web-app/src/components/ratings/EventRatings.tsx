'use client'

import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { useAuthStore } from '@/store/auth'
import { RatingResponse } from '@/lib/api/generated/client'
import { Star, Plus, MessageSquare } from 'lucide-react'
import { cn } from '@/lib/utils/cn'
import { useEventRatingsList } from '@/hooks/useEventRatingsList'
import { useRatingActions, useUserRatingForEvent } from '@/hooks/useRatings'
import { RatingStats } from './RatingStats'
import { RatingForm } from './RatingForm'
import { RatingCard } from './RatingCard'

interface EventRatingsProps {
  eventId: string
  className?: string
}

export function EventRatings({ eventId, className }: EventRatingsProps) {
  const { isAuthenticated } = useAuthStore()
  const [showForm, setShowForm] = useState(false)
  const [editingRating, setEditingRating] = useState<RatingResponse | null>(null)
  const [page, setPage] = useState(0)
  
  const { 
    ratings, 
    statistics, 
    isLoading, 
    error, 
    hasNextPage, 
    totalElements,
    refetch 
  } = useEventRatingsList({ eventId, page, size: 10 })
  
  const { rating: userRating, refetch: refetchUserRating } = useUserRatingForEvent(eventId)
  const { deleteRating, isDeleting } = useRatingActions()
  
  const hasUserRated = !!userRating

  const handleSuccess = () => {
    setShowForm(false)
    setEditingRating(null)
    refetch()
    refetchUserRating()
  }

  const handleEdit = (rating: RatingResponse) => {
    setEditingRating(rating)
    setShowForm(true)
  }

  const handleDelete = async (ratingId: string) => {
    if (confirm('Bạn có chắc chắn muốn xóa đánh giá này?')) {
      try {
        await deleteRating(ratingId)
        refetch()
        refetchUserRating()
      } catch (error) {
        // Error handled by hook
      }
    }
  }

  const handleReport = (ratingId: string) => {
    // TODO: Implement report functionality
    console.log('Report rating:', ratingId)
  }

  const handleLoadMore = () => {
    setPage(prev => prev + 1)
  }

  const canAddRating = isAuthenticated && !hasUserRated && !showForm

  if (error) {
    return (
      <div className={cn("bg-white border border-gray-200 rounded-lg p-6", className)}>
        <div className="text-center py-8">
          <MessageSquare className="w-12 h-12 text-gray-400 mx-auto mb-3" />
          <p className="text-gray-500">
            Không thể tải đánh giá. Vui lòng thử lại sau.
          </p>
        </div>
      </div>
    )
  }

  return (
    <div className={cn("space-y-6", className)}>
      {/* Statistics */}
      {statistics && (
        <RatingStats statistics={statistics} />
      )}

      {/* Add/Edit Rating Form */}
      {showForm && (
        <RatingForm
          eventId={eventId}
          existingRating={editingRating ? {
            id: editingRating.id!,
            score: editingRating.score,
            review: editingRating.review || ''
          } : undefined}
          onSuccess={handleSuccess}
          onCancel={() => {
            setShowForm(false)
            setEditingRating(null)
          }}
        />
      )}

      {/* Add Rating Button */}
      {canAddRating && (
        <div className="flex justify-center">
          <Button
            onClick={() => setShowForm(true)}
            className="flex items-center gap-2"
          >
            <Star className="w-4 h-4" />
            Viết đánh giá
          </Button>
        </div>
      )}

      {/* User's Rating (if exists and not editing) */}
      {userRating && !showForm && (
        <div className="space-y-4">
          <h4 className="font-medium text-gray-900">Đánh giá của bạn</h4>
          <RatingCard
            rating={userRating}
            onEdit={handleEdit}
            onDelete={handleDelete}
            className="border-blue-200 bg-blue-50"
          />
        </div>
      )}

      {/* All Ratings */}
      <div className="space-y-4">
        <div className="flex items-center justify-between">
          <h4 className="font-medium text-gray-900">
            Tất cả đánh giá ({totalElements})
          </h4>
        </div>

        {isLoading && page === 0 ? (
          <div className="space-y-4">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="bg-gray-100 rounded-lg p-4 animate-pulse">
                <div className="flex items-start gap-3">
                  <div className="w-10 h-10 bg-gray-200 rounded-full" />
                  <div className="flex-1 space-y-2">
                    <div className="h-4 bg-gray-200 rounded w-1/4" />
                    <div className="h-3 bg-gray-200 rounded w-full" />
                    <div className="h-3 bg-gray-200 rounded w-3/4" />
                  </div>
                </div>
              </div>
            ))}
          </div>
        ) : ratings.length === 0 ? (
          <div className="text-center py-8 bg-gray-50 rounded-lg">
            <MessageSquare className="w-12 h-12 text-gray-400 mx-auto mb-3" />
            <p className="text-gray-500">
              Chưa có đánh giá nào. Hãy là người đầu tiên đánh giá sự kiện này!
            </p>
          </div>
        ) : (
          <div className="space-y-4">
            {ratings
              .filter((rating: any) => rating.id !== userRating?.id) // Don't show user's rating in general list
              .map((rating: any) => (
                <RatingCard
                  key={rating.id}
                  rating={rating}
                  onEdit={handleEdit}
                  onDelete={handleDelete}
                  onReport={handleReport}
                />
              ))}

            {/* Load More Button */}
            {hasNextPage && (
              <div className="flex justify-center pt-4">
                <Button
                  variant="outline"
                  onClick={handleLoadMore}
                  disabled={isLoading}
                >
                  {isLoading ? 'Đang tải...' : 'Xem thêm'}
                </Button>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
