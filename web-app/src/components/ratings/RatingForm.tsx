'use client'

import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { Textarea } from '@/components/ui/textarea'
import { Star, Loader2 } from 'lucide-react'
import { useRatingActions } from '@/hooks/useRatings'
import { RatingRequest } from '@/lib/api/generated/client'
import { cn } from '@/lib/utils/cn'

interface RatingFormProps {
  eventId: string
  existingRating?: {
    id: string
    score: number
    review: string
  }
  onSuccess?: () => void
  onCancel?: () => void
  className?: string
}

export function RatingForm({ 
  eventId, 
  existingRating, 
  onSuccess, 
  onCancel,
  className 
}: RatingFormProps) {
  const [rating, setRating] = useState(existingRating?.score || 0)
  const [comment, setComment] = useState(existingRating?.review || '')
  const [hoveredRating, setHoveredRating] = useState(0)
  
  const { createOrUpdateRating, updateRating, isCreating, isUpdating } = useRatingActions()
  
  const isLoading = isCreating || isUpdating
  const isEditing = !!existingRating

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (rating === 0) {
      return
    }

    try {
      if (isEditing) {
        await updateRating(existingRating.id, {
          score: rating,
          review: comment.trim()
        })
      } else {
        const ratingData: RatingRequest = {
          eventId,
          score: rating,
          review: comment.trim()
        }
        await createOrUpdateRating(ratingData)
      }
      
      onSuccess?.()
    } catch (error) {
      // Error handled by the hook
    }
  }

  const handleStarClick = (value: number) => {
    setRating(value)
  }

  const handleStarHover = (value: number) => {
    setHoveredRating(value)
  }

  const handleStarLeave = () => {
    setHoveredRating(0)
  }

  const displayRating = hoveredRating || rating

  return (
    <div className={cn("bg-white border border-gray-200 rounded-lg p-6", className)}>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <h3 className="text-lg font-semibold text-gray-900 mb-3">
            {isEditing ? 'Chỉnh sửa đánh giá' : 'Đánh giá sự kiện'}
          </h3>
          
          {/* Star Rating */}
          <div className="flex items-center gap-1 mb-4">
            {[1, 2, 3, 4, 5].map((star) => (
              <button
                key={star}
                type="button"
                onClick={() => handleStarClick(star)}
                onMouseEnter={() => handleStarHover(star)}
                onMouseLeave={handleStarLeave}
                className="p-1 rounded-full hover:bg-yellow-50 transition-colors"
                disabled={isLoading}
              >
                <Star
                  className={cn(
                    "w-8 h-8 transition-colors",
                    star <= displayRating
                      ? "fill-yellow-400 text-yellow-400"
                      : "text-gray-300 hover:text-yellow-300"
                  )}
                />
              </button>
            ))}
            <span className="ml-2 text-sm text-gray-600">
              {rating > 0 && (
                <>
                  {rating}/5 {rating === 1 ? 'sao' : 'sao'}
                </>
              )}
            </span>
          </div>
        </div>

        {/* Comment */}
        <div>
          <label htmlFor="comment" className="block text-sm font-medium text-gray-700 mb-2">
            Nhận xét (tùy chọn)
          </label>
          <Textarea
            id="comment"
            value={comment}
            onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setComment(e.target.value)}
            placeholder="Chia sẻ trải nghiệm của bạn về sự kiện này..."
            rows={4}
            maxLength={500}
            disabled={isLoading}
            className="resize-none"
          />
          <div className="flex justify-between mt-1">
            <span className="text-xs text-gray-500">
              Tối đa 500 ký tự
            </span>
            <span className="text-xs text-gray-500">
              {comment.length}/500
            </span>
          </div>
        </div>

        {/* Buttons */}
        <div className="flex justify-end gap-3 pt-2">
          {onCancel && (
            <Button
              type="button"
              variant="outline"
              onClick={onCancel}
              disabled={isLoading}
            >
              Hủy
            </Button>
          )}
          <Button
            type="submit"
            disabled={rating === 0 || isLoading}
            className="min-w-[120px]"
          >
            {isLoading ? (
              <>
                <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                {isEditing ? 'Đang cập nhật...' : 'Đang gửi...'}
              </>
            ) : (
              isEditing ? 'Cập nhật' : 'Gửi đánh giá'
            )}
          </Button>
        </div>
      </form>
    </div>
  )
}
