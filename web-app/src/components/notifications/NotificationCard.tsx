'use client'

import { formatDistanceToNow } from 'date-fns'
import { vi } from 'date-fns/locale'
import { 
  Bell, 
  CheckCircle, 
  Trash2, 
  User, 
  CreditCard, 
  Calendar, 
  MessageCircle, 
  Star,
  Shield,
  Settings
} from 'lucide-react'
import { NotificationResponse, NotificationResponseNotificationType } from '@/lib/api/generated/client'
import { cn } from '@/lib/utils/cn'
import { Button } from '@/components/ui/button'

interface NotificationCardProps {
  notification: NotificationResponse
  isSelected: boolean
  onSelect: (id: string) => void
  onMarkAsRead: (id: string) => void
  onDelete: (id: string) => void
}

const getNotificationIcon = (type: NotificationResponseNotificationType) => {
  const iconMap = {
    ACCOUNT_ACTIVATION: User,
    PASSWORD_RESET: Shield,
    TICKET_CONFIRMATION: CheckCircle,
    EVENT_REMINDER: Calendar,
    NEW_COMMENT: MessageCircle,
    NEW_RATING: Star,
    TICKET_PURCHASE: CreditCard,
    SYSTEM: Settings,
  }
  
  return iconMap[type] || Bell
}

const getNotificationColor = (type: NotificationResponseNotificationType) => {
  const colorMap = {
    ACCOUNT_ACTIVATION: 'text-green-600 bg-green-50',
    PASSWORD_RESET: 'text-red-600 bg-red-50',
    TICKET_CONFIRMATION: 'text-blue-600 bg-blue-50',
    EVENT_REMINDER: 'text-purple-600 bg-purple-50',
    NEW_COMMENT: 'text-orange-600 bg-orange-50',
    NEW_RATING: 'text-yellow-600 bg-yellow-50',
    TICKET_PURCHASE: 'text-green-600 bg-green-50',
    SYSTEM: 'text-gray-600 bg-gray-50',
  }
  
  return colorMap[type] || 'text-gray-600 bg-gray-50'
}

const getNotificationTypeLabel = (type: NotificationResponseNotificationType) => {
  const labelMap = {
    ACCOUNT_ACTIVATION: 'Kích hoạt tài khoản',
    PASSWORD_RESET: 'Đặt lại mật khẩu',
    TICKET_CONFIRMATION: 'Xác nhận vé',
    EVENT_REMINDER: 'Nhắc nhở sự kiện',
    NEW_COMMENT: 'Bình luận mới',
    NEW_RATING: 'Đánh giá mới',
    TICKET_PURCHASE: 'Mua vé',
    SYSTEM: 'Hệ thống',
  }
  
  return labelMap[type] || 'Thông báo'
}

export function NotificationCard({ 
  notification, 
  isSelected, 
  onSelect, 
  onMarkAsRead, 
  onDelete 
}: NotificationCardProps) {
  const Icon = getNotificationIcon(notification.notificationType)
  const colorClasses = getNotificationColor(notification.notificationType)
  const typeLabel = getNotificationTypeLabel(notification.notificationType)
  
  const formatDate = (dateString: string) => {
    try {
      return formatDistanceToNow(new Date(dateString), { 
        addSuffix: true, 
        locale: vi 
      })
    } catch {
      return 'Vừa xong'
    }
  }

  const handleCardClick = () => {
    if (!notification.isRead) {
      onMarkAsRead(notification.id)
    }
  }

  return (
    <div 
      className={cn(
        'border rounded-lg p-4 transition-all duration-200 hover:shadow-md cursor-pointer',
        notification.isRead 
          ? 'bg-white border-gray-200' 
          : 'bg-blue-50 border-blue-200',
        isSelected && 'ring-2 ring-blue-500'
      )}
      onClick={handleCardClick}
    >
      <div className="flex items-start gap-3">
        {/* Checkbox */}
        <input
          type="checkbox"
          checked={isSelected}
          onChange={() => onSelect(notification.id)}
          onClick={(e) => e.stopPropagation()}
          className="mt-1"
        />

        {/* Icon */}
        <div className={cn(
          'w-10 h-10 rounded-full flex items-center justify-center',
          colorClasses
        )}>
          <Icon className="w-5 h-5" />
        </div>

        {/* Content */}
        <div className="flex-1 min-w-0">
          <div className="flex items-start justify-between gap-2">
            <div className="flex-1">
              {/* Title với unread indicator */}
              <div className="flex items-center gap-2 mb-1">
                <h3 className={cn(
                  'font-semibold text-gray-900 text-sm',
                  !notification.isRead && 'font-bold'
                )}>
                  {notification.title}
                </h3>
                {!notification.isRead && (
                  <div className="w-2 h-2 bg-blue-600 rounded-full"></div>
                )}
              </div>

              {/* Type badge */}
              <span className={cn(
                'inline-block px-2 py-1 text-xs rounded-full mb-2',
                colorClasses
              )}>
                {typeLabel}
              </span>

              {/* Content */}
              <p className="text-gray-700 text-sm line-clamp-2 mb-2">
                {notification.content}
              </p>

              {/* Meta info */}
              <div className="flex items-center text-xs text-gray-500 gap-4">
                <span>{formatDate(notification.createdAt)}</span>
                {notification.readAt && (
                  <span>Đã đọc {formatDate(notification.readAt)}</span>
                )}
                {notification.referenceType && notification.referenceId && (
                  <span className="text-blue-600">
                    {notification.referenceType}: {notification.referenceId.slice(0, 8)}...
                  </span>
                )}
              </div>
            </div>

            {/* Actions */}
            <div className="flex items-center gap-1">
              {!notification.isRead && (
                <Button
                  size="sm"
                  variant="ghost"
                  onClick={(e) => {
                    e.stopPropagation()
                    onMarkAsRead(notification.id)
                  }}
                  className="text-blue-600 hover:text-blue-800"
                >
                  <CheckCircle className="w-4 h-4" />
                </Button>
              )}
              
              <Button
                size="sm"
                variant="ghost"
                onClick={(e) => {
                  e.stopPropagation()
                  onDelete(notification.id)
                }}
                className="text-red-600 hover:text-red-800"
              >
                <Trash2 className="w-4 h-4" />
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
