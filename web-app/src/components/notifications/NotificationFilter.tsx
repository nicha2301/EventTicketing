'use client'

import { NotificationResponseNotificationType } from '@/lib/api/generated/client'
import { cn } from '@/lib/utils/cn'
import { 
  Bell, 
  User, 
  Shield, 
  CheckCircle, 
  Calendar, 
  MessageCircle, 
  Star,
  CreditCard,
  Settings
} from 'lucide-react'

interface NotificationFilterProps {
  selectedType: NotificationResponseNotificationType | 'ALL'
  onTypeChange: (type: NotificationResponseNotificationType | 'ALL') => void
  counts: Record<NotificationResponseNotificationType | 'ALL', number>
}

const filterOptions = [
  {
    key: 'ALL' as const,
    label: 'Tất cả',
    icon: Bell,
    color: 'text-gray-600'
  },
  {
    key: 'ACCOUNT_ACTIVATION' as const,
    label: 'Tài khoản',
    icon: User,
    color: 'text-green-600'
  },
  {
    key: 'PASSWORD_RESET' as const,
    label: 'Bảo mật',
    icon: Shield,
    color: 'text-red-600'
  },
  {
    key: 'TICKET_CONFIRMATION' as const,
    label: 'Xác nhận vé',
    icon: CheckCircle,
    color: 'text-blue-600'
  },
  {
    key: 'EVENT_REMINDER' as const,
    label: 'Nhắc nhở',
    icon: Calendar,
    color: 'text-purple-600'
  },
  {
    key: 'NEW_COMMENT' as const,
    label: 'Bình luận',
    icon: MessageCircle,
    color: 'text-orange-600'
  },
  {
    key: 'NEW_RATING' as const,
    label: 'Đánh giá',
    icon: Star,
    color: 'text-yellow-600'
  },
  {
    key: 'TICKET_PURCHASE' as const,
    label: 'Mua vé',
    icon: CreditCard,
    color: 'text-green-600'
  },
  {
    key: 'SYSTEM' as const,
    label: 'Hệ thống',
    icon: Settings,
    color: 'text-gray-600'
  },
]

export function NotificationFilter({ selectedType, onTypeChange, counts }: NotificationFilterProps) {
  return (
    <div className="bg-white border border-gray-200 rounded-lg p-4">
      <h3 className="text-sm font-semibold text-gray-900 mb-3">Lọc theo loại</h3>
      
      <div className="flex flex-wrap gap-2">
        {filterOptions.map((option) => {
          const Icon = option.icon
          const count = counts[option.key] || 0
          const isSelected = selectedType === option.key
          
          return (
            <button
              key={option.key}
              onClick={() => onTypeChange(option.key)}
              className={cn(
                'flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition-colors',
                isSelected
                  ? 'bg-blue-100 text-blue-900 border border-blue-200'
                  : 'bg-gray-50 text-gray-700 hover:bg-gray-100 border border-transparent'
              )}
            >
              <Icon className={cn('w-4 h-4', isSelected ? 'text-blue-600' : option.color)} />
              
              <span>{option.label}</span>
              
              {count > 0 && (
                <span className={cn(
                  'px-2 py-0.5 text-xs rounded-full',
                  isSelected
                    ? 'bg-blue-200 text-blue-800'
                    : 'bg-gray-200 text-gray-600'
                )}>
                  {count}
                </span>
              )}
            </button>
          )
        })}
      </div>
      
      {/* Mobile friendly */}
      <div className="mt-3 md:hidden">
        <select
          value={selectedType}
          onChange={(e) => onTypeChange(e.target.value as NotificationResponseNotificationType | 'ALL')}
          className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm"
        >
          {filterOptions.map((option) => (
            <option key={option.key} value={option.key}>
              {option.label} {counts[option.key] ? `(${counts[option.key]})` : ''}
            </option>
          ))}
        </select>
      </div>
    </div>
  )
}
