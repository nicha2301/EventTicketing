'use client'

import { Star, ThumbsUp, Flag, MoreHorizontal, Edit, Trash2 } from 'lucide-react'
import { format } from 'date-fns'
import { vi } from 'date-fns/locale'
import { Button } from '@/components/ui/button'
import { 
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { SimpleAvatar } from '@/components/ui/simple-avatar'
import { RatingResponse } from '@/lib/api/generated/client'
import { useAuthStore } from '@/store/auth'
import { cn } from '@/lib/utils/cn'

interface RatingCardProps {
  rating: RatingResponse
  onEdit?: (rating: RatingResponse) => void
  onDelete?: (ratingId: string) => void
  onReport?: (ratingId: string) => void
  className?: string
}

export function RatingCard({ 
  rating, 
  onEdit, 
  onDelete, 
  onReport,
  className 
}: RatingCardProps) {
  const { currentUser } = useAuthStore()
  const isOwnRating = currentUser?.id === rating.userId

  const renderStars = (score: number) => {
    return (
      <div className="flex items-center gap-1">
        {[1, 2, 3, 4, 5].map((star) => (
          <Star
            key={star}
            className={cn(
              "w-4 h-4",
              star <= score
                ? "fill-yellow-400 text-yellow-400"
                : "text-gray-300"
            )}
          />
        ))}
      </div>
    )
  }

  const getInitials = (name: string) => {
    return name
      .split(' ')
      .map(word => word[0])
      .join('')
      .toUpperCase()
      .substring(0, 2)
  }

  return (
    <div className={cn("bg-white border border-gray-200 rounded-lg p-4", className)}>
      <div className="flex items-start justify-between">
        <div className="flex items-start gap-3 flex-1">
          {/* Avatar */}
          <SimpleAvatar
            src={rating.userAvatar}
            fallback={getInitials(rating.username || 'Anonymous')}
            className="w-10 h-10"
          />

          <div className="flex-1 min-w-0">
            {/* User Info & Rating */}
            <div className="flex items-center gap-2 mb-2">
              <h4 className="font-medium text-gray-900 truncate">
                {rating.username || 'Người dùng ẩn danh'}
              </h4>
              {renderStars(rating.score)}
              <span className="text-sm text-gray-500">
                {rating.score}/5
              </span>
            </div>

            {/* Review Text */}
            {rating.review && (
              <p className="text-gray-700 text-sm mb-3 leading-relaxed">
                {rating.review}
              </p>
            )}

            {/* Timestamp */}
            <div className="flex items-center gap-4 text-xs text-gray-500">
              <span>
                {format(new Date(rating.createdAt), 'dd/MM/yyyy HH:mm', { locale: vi })}
              </span>
              {rating.updatedAt && rating.updatedAt !== rating.createdAt && (
                <span className="text-amber-600">
                  • Đã chỉnh sửa
                </span>
              )}
            </div>
          </div>
        </div>

        {/* Actions Menu */}
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="sm" className="h-8 w-8 p-0">
              <MoreHorizontal className="w-4 h-4" />
              <span className="sr-only">Mở menu</span>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            {isOwnRating ? (
              <>
                <DropdownMenuItem onClick={() => onEdit?.(rating)}>
                  <Edit className="w-4 h-4 mr-2" />
                  Chỉnh sửa
                </DropdownMenuItem>
                <DropdownMenuItem 
                  onClick={() => rating.id && onDelete?.(rating.id)}
                  className="text-red-600"
                >
                  <Trash2 className="w-4 h-4 mr-2" />
                  Xóa
                </DropdownMenuItem>
              </>
            ) : (
              <DropdownMenuItem onClick={() => rating.id && onReport?.(rating.id)}>
                <Flag className="w-4 h-4 mr-2" />
                Báo cáo
              </DropdownMenuItem>
            )}
          </DropdownMenuContent>
        </DropdownMenu>
      </div>

      {/* Helpful Actions (Future feature) */}
      {!isOwnRating && (
        <div className="flex items-center gap-4 mt-3 pt-3 border-t border-gray-100">
          <button className="flex items-center gap-1 text-sm text-gray-500 hover:text-blue-600 transition-colors">
            <ThumbsUp className="w-4 h-4" />
            <span>Hữu ích (0)</span>
          </button>
        </div>
      )}
    </div>
  )
}
