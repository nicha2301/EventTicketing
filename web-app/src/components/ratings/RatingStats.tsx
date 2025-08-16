'use client'

import { Star } from 'lucide-react'
import { RatingStatisticsResponse } from '@/lib/api/generated/client'
import { cn } from '@/lib/utils/cn'

interface RatingStatsProps {
  statistics: RatingStatisticsResponse
  className?: string
}

export function RatingStats({ statistics, className }: RatingStatsProps) {
  const { averageRating, totalRatings, ratingCounts } = statistics
  
  const renderStars = (rating: number) => {
    return (
      <div className="flex items-center gap-1">
        {[1, 2, 3, 4, 5].map((star) => (
          <Star
            key={star}
            className={cn(
              "w-5 h-5",
              star <= Math.round(rating)
                ? "fill-yellow-400 text-yellow-400"
                : "text-gray-300"
            )}
          />
        ))}
      </div>
    )
  }

  const getRatingPercentage = (count: number) => {
    return totalRatings > 0 ? (count / totalRatings) * 100 : 0
  }

  return (
    <div className={cn("bg-white border border-gray-200 rounded-lg p-6", className)}>
      <h3 className="text-lg font-semibold text-gray-900 mb-4">
        Đánh giá từ khách hàng
      </h3>
      
      {totalRatings === 0 ? (
        <div className="text-center py-8">
          <div className="flex justify-center mb-3">
            {renderStars(0)}
          </div>
          <p className="text-gray-500 text-sm">
            Chưa có đánh giá nào cho sự kiện này
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          {/* Overall Rating */}
          <div className="text-center pb-4 border-b border-gray-100">
            <div className="flex justify-center mb-2">
              {renderStars(averageRating)}
            </div>
            <div className="text-3xl font-bold text-gray-900 mb-1">
              {averageRating.toFixed(1)}
            </div>
            <p className="text-sm text-gray-600">
              Dựa trên {totalRatings} {totalRatings === 1 ? 'đánh giá' : 'đánh giá'}
            </p>
          </div>

          {/* Rating Breakdown */}
          <div className="space-y-3">
            {[5, 4, 3, 2, 1].map((star) => {
              const count = ratingCounts[star.toString()] || 0
              const percentage = getRatingPercentage(count)
              
              return (
                <div key={star} className="flex items-center gap-3">
                  <div className="flex items-center gap-1 min-w-[60px]">
                    <span className="text-sm font-medium text-gray-700">
                      {star}
                    </span>
                    <Star className="w-3 h-3 fill-yellow-400 text-yellow-400" />
                  </div>
                  
                  <div className="flex-1 bg-gray-200 rounded-full h-2">
                    <div
                      className="bg-yellow-400 h-2 rounded-full transition-all duration-300"
                      style={{ width: `${percentage}%` }}
                    />
                  </div>
                  
                  <div className="min-w-[40px] text-right">
                    <span className="text-sm text-gray-600">
                      {count}
                    </span>
                  </div>
                </div>
              )
            })}
          </div>
        </div>
      )}
    </div>
  )
}
