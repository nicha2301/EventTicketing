'use client'

import { Button } from '@/components/ui/button'
import { CheckCircle, Trash2, Square, CheckSquare } from 'lucide-react'

interface NotificationActionsProps {
  selectedCount: number
  totalCount: number
  onSelectAll: () => void
  onMarkAsRead: () => void
  onDelete: () => void
  onDeleteAll: () => void
  isMarkingAsRead: boolean
  isDeleting: boolean
  isDeletingAll: boolean
}

export function NotificationActions({
  selectedCount,
  totalCount,
  onSelectAll,
  onMarkAsRead,
  onDelete,
  onDeleteAll,
  isMarkingAsRead,
  isDeleting,
  isDeletingAll
}: NotificationActionsProps) {
  const isAllSelected = selectedCount === totalCount && totalCount > 0
  
  return (
    <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          {/* Select All Toggle */}
          <button
            onClick={onSelectAll}
            className="flex items-center gap-2 text-sm font-medium text-blue-900 hover:text-blue-700"
          >
            {isAllSelected ? (
              <CheckSquare className="w-4 h-4" />
            ) : (
              <Square className="w-4 h-4" />
            )}
            {isAllSelected ? 'Bỏ chọn tất cả' : 'Chọn tất cả'}
          </button>
          
          {/* Selected count */}
          <span className="text-sm text-blue-700">
            {selectedCount > 0 
              ? `Đã chọn ${selectedCount} thông báo`
              : 'Chưa chọn thông báo nào'
            }
          </span>
        </div>

        {/* Bulk Actions */}
        <div className="flex items-center gap-2">
          {selectedCount > 0 && (
            <>
              <Button
                size="sm"
                variant="outline"
                onClick={onMarkAsRead}
                disabled={isMarkingAsRead}
                className="text-blue-600 border-blue-200 hover:bg-blue-50"
              >
                {isMarkingAsRead ? (
                  <div className="w-4 h-4 border-2 border-blue-600 border-t-transparent rounded-full animate-spin" />
                ) : (
                  <CheckCircle className="w-4 h-4" />
                )}
                <span className="ml-2">Đánh dấu đã đọc</span>
              </Button>
              
              <Button
                size="sm"
                variant="outline"
                onClick={onDelete}
                disabled={isDeleting}
                className="text-red-600 border-red-200 hover:bg-red-50"
              >
                {isDeleting ? (
                  <div className="w-4 h-4 border-2 border-red-600 border-t-transparent rounded-full animate-spin" />
                ) : (
                  <Trash2 className="w-4 h-4" />
                )}
                <span className="ml-2">Xóa</span>
              </Button>
            </>
          )}
          
          {totalCount > 0 && (
            <Button
              size="sm"
              variant="outline"
              onClick={onDeleteAll}
              disabled={isDeletingAll}
              className="text-red-600 border-red-200 hover:bg-red-50"
            >
              {isDeletingAll ? (
                <div className="w-4 h-4 border-2 border-red-600 border-t-transparent rounded-full animate-spin" />
              ) : (
                <Trash2 className="w-4 h-4" />
              )}
              <span className="ml-2">Xóa tất cả</span>
            </Button>
          )}
        </div>
      </div>

      {/* Action description */}
      {selectedCount > 0 && (
        <div className="mt-2 text-xs text-blue-600">
          Bạn có thể thực hiện các hành động trên {selectedCount} thông báo đã chọn
        </div>
      )}
    </div>
  )
}
