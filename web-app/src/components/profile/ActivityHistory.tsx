'use client'

import { useState } from 'react'
import { 
  Activity, 
  Calendar, 
  CreditCard, 
  Heart, 
  MessageSquare, 
  User, 
  MapPin, 
  Clock,
  Filter,
  ChevronDown
} from 'lucide-react'
import { Button } from '@/components/ui/button'

interface ActivityItem {
  id: string
  type: 'booking' | 'event' | 'review' | 'profile' | 'social'
  title: string
  description: string
  timestamp: string
  metadata?: {
    eventName?: string
    amount?: number
    rating?: number
    location?: string
  }
}

// Mock activity data
const activities: ActivityItem[] = [
  {
    id: '1',
    type: 'booking',
    title: 'Đặt vé thành công',
    description: 'Bạn đã đặt 2 vé cho sự kiện "Tech Conference 2025"',
    timestamp: '2025-08-16T14:30:00Z',
    metadata: {
      eventName: 'Tech Conference 2025',
      amount: 500000
    }
  },
  {
    id: '2', 
    type: 'social',
    title: 'Đánh giá sự kiện',
    description: 'Bạn đã đánh giá 5 sao cho "Workshop UI/UX Design"',
    timestamp: '2025-08-15T10:15:00Z',
    metadata: {
      eventName: 'Workshop UI/UX Design',
      rating: 5
    }
  },
  {
    id: '3',
    type: 'event',
    title: 'Tham gia sự kiện',
    description: 'Bạn đã check-in tại "Art Exhibition 2025"',
    timestamp: '2025-08-14T18:00:00Z',
    metadata: {
      eventName: 'Art Exhibition 2025',
      location: 'Museum of Fine Arts, HCMC'
    }
  },
  {
    id: '4',
    type: 'social',
    title: 'Yêu thích sự kiện',
    description: 'Bạn đã thêm "Music Festival 2025" vào danh sách yêu thích',
    timestamp: '2025-08-13T16:20:00Z',
    metadata: {
      eventName: 'Music Festival 2025'
    }
  },
  {
    id: '5',
    type: 'profile',
    title: 'Cập nhật hồ sơ',
    description: 'Bạn đã cập nhật ảnh đại diện và thông tin liên hệ',
    timestamp: '2025-08-12T09:45:00Z'
  },
  {
    id: '6',
    type: 'booking',
    title: 'Hủy đặt vé',
    description: 'Bạn đã hủy vé cho "Cooking Class Advanced"',
    timestamp: '2025-08-11T11:30:00Z',
    metadata: {
      eventName: 'Cooking Class Advanced',
      amount: 200000
    }
  },
  {
    id: '7',
    type: 'social',
    title: 'Bình luận sự kiện',
    description: 'Bạn đã bình luận về "Photography Workshop"',
    timestamp: '2025-08-10T14:15:00Z',
    metadata: {
      eventName: 'Photography Workshop'
    }
  },
  {
    id: '8',
    type: 'event',
    title: 'Đăng ký sự kiện',
    description: 'Bạn đã đăng ký tham gia "Startup Meetup HCMC"',
    timestamp: '2025-08-09T20:00:00Z',
    metadata: {
      eventName: 'Startup Meetup HCMC'
    }
  }
]

const activityTypes = [
  { value: 'all', label: 'Tất cả hoạt động', icon: Activity },
  { value: 'booking', label: 'Đặt vé', icon: CreditCard },
  { value: 'event', label: 'Sự kiện', icon: Calendar },
  { value: 'social', label: 'Hoạt động xã hội', icon: Heart },
  { value: 'profile', label: 'Hồ sơ', icon: User },
]

export function ActivityHistory() {
  const [selectedType, setSelectedType] = useState('all')
  const [showFilters, setShowFilters] = useState(false)

  const filteredActivities = selectedType === 'all' 
    ? activities 
    : activities.filter(activity => activity.type === selectedType)

  const getActivityIcon = (type: string) => {
    switch (type) {
      case 'booking': return CreditCard
      case 'event': return Calendar
      case 'review': 
      case 'social': return Heart
      case 'profile': return User
      default: return Activity
    }
  }

  const getActivityColor = (type: string) => {
    switch (type) {
      case 'booking': return 'bg-green-100 text-green-600'
      case 'event': return 'bg-blue-100 text-blue-600'
      case 'social': return 'bg-pink-100 text-pink-600'
      case 'profile': return 'bg-purple-100 text-purple-600'
      default: return 'bg-gray-100 text-gray-600'
    }
  }

  const formatTimestamp = (timestamp: string) => {
    const date = new Date(timestamp)
    const now = new Date()
    const diffInHours = (now.getTime() - date.getTime()) / (1000 * 60 * 60)

    if (diffInHours < 1) {
      const minutes = Math.floor(diffInHours * 60)
      return `${minutes} phút trước`
    } else if (diffInHours < 24) {
      return `${Math.floor(diffInHours)} giờ trước`
    } else if (diffInHours < 24 * 7) {
      return `${Math.floor(diffInHours / 24)} ngày trước`
    } else {
      return date.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
      })
    }
  }

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount)
  }

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-semibold text-gray-900 flex items-center gap-2">
            <Activity className="w-5 h-5" />
            Lịch sử hoạt động
          </h2>
          <p className="text-gray-600 text-sm mt-1">
            Theo dõi tất cả hoạt động của bạn trên EventTicketing
          </p>
        </div>
        
        {/* Filter Button */}
        <Button
          variant="outline"
          onClick={() => setShowFilters(!showFilters)}
          className="flex items-center gap-2"
        >
          <Filter className="w-4 h-4" />
          Lọc
          <ChevronDown className={`w-4 h-4 transition-transform ${showFilters ? 'rotate-180' : ''}`} />
        </Button>
      </div>

      {/* Filter Options */}
      {showFilters && (
        <div className="bg-gray-50 rounded-lg p-4">
          <h3 className="font-medium text-gray-900 mb-3">Lọc theo loại hoạt động</h3>
          <div className="flex flex-wrap gap-2">
            {activityTypes.map((type) => {
              const Icon = type.icon
              return (
                <button
                  key={type.value}
                  onClick={() => setSelectedType(type.value)}
                  className={`flex items-center gap-2 px-3 py-2 rounded-lg text-sm font-medium transition-colors ${
                    selectedType === type.value
                      ? 'bg-blue-600 text-white'
                      : 'bg-white text-gray-700 hover:bg-gray-100'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  {type.label}
                </button>
              )
            })}
          </div>
        </div>
      )}

      {/* Activity Stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="bg-blue-50 rounded-lg p-4">
          <div className="text-2xl font-bold text-blue-600">12</div>
          <div className="text-sm text-blue-700">Sự kiện đã tham gia</div>
        </div>
        <div className="bg-green-50 rounded-lg p-4">
          <div className="text-2xl font-bold text-green-600">8</div>
          <div className="text-sm text-green-700">Vé đã đặt</div>
        </div>
        <div className="bg-pink-50 rounded-lg p-4">
          <div className="text-2xl font-bold text-pink-600">15</div>
          <div className="text-sm text-pink-700">Đánh giá đã viết</div>
        </div>
        <div className="bg-purple-50 rounded-lg p-4">
          <div className="text-2xl font-bold text-purple-600">23</div>
          <div className="text-sm text-purple-700">Sự kiện yêu thích</div>
        </div>
      </div>

      {/* Activity Timeline */}
      <div className="space-y-4">
        <h3 className="font-medium text-gray-900">
          Hoạt động gần đây 
          <span className="font-normal text-gray-500">({filteredActivities.length} hoạt động)</span>
        </h3>
        
        <div className="space-y-3">
          {filteredActivities.map((activity, index) => {
            const Icon = getActivityIcon(activity.type)
            return (
              <div key={activity.id} className="bg-white border border-gray-200 rounded-lg p-4">
                <div className="flex items-start gap-4">
                  {/* Icon */}
                  <div className={`w-10 h-10 rounded-full flex items-center justify-center ${getActivityColor(activity.type)}`}>
                    <Icon className="w-5 h-5" />
                  </div>

                  {/* Content */}
                  <div className="flex-1 min-w-0">
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <h4 className="font-medium text-gray-900">{activity.title}</h4>
                        <p className="text-gray-600 text-sm mt-1">{activity.description}</p>
                        
                        {/* Metadata */}
                        {activity.metadata && (
                          <div className="flex items-center gap-4 mt-2 text-xs text-gray-500">
                            {activity.metadata.eventName && (
                              <span className="flex items-center gap-1">
                                <Calendar className="w-3 h-3" />
                                {activity.metadata.eventName}
                              </span>
                            )}
                            {activity.metadata.amount && (
                              <span className="flex items-center gap-1">
                                <CreditCard className="w-3 h-3" />
                                {formatCurrency(activity.metadata.amount)}
                              </span>
                            )}
                            {activity.metadata.rating && (
                              <span className="flex items-center gap-1">
                                <Heart className="w-3 h-3" />
                                {activity.metadata.rating} sao
                              </span>
                            )}
                            {activity.metadata.location && (
                              <span className="flex items-center gap-1">
                                <MapPin className="w-3 h-3" />
                                {activity.metadata.location}
                              </span>
                            )}
                          </div>
                        )}
                      </div>
                      
                      {/* Timestamp */}
                      <div className="flex items-center gap-1 text-xs text-gray-500 ml-4">
                        <Clock className="w-3 h-3" />
                        {formatTimestamp(activity.timestamp)}
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )
          })}
        </div>

        {/* Load More */}
        <div className="text-center pt-4">
          <Button variant="outline">
            Xem thêm hoạt động
          </Button>
        </div>
      </div>

      {/* Export Data */}
      <div className="border-t pt-6">
        <div className="bg-gray-50 rounded-lg p-4">
          <h3 className="font-medium text-gray-900 mb-2">Xuất dữ liệu hoạt động</h3>
          <p className="text-gray-600 text-sm mb-4">
            Tải xuống toàn bộ lịch sử hoạt động của bạn dưới dạng file CSV hoặc JSON
          </p>
          <div className="flex gap-2">
            <Button variant="outline" size="sm">
              Xuất CSV
            </Button>
            <Button variant="outline" size="sm">
              Xuất JSON
            </Button>
          </div>
        </div>
      </div>
    </div>
  )
}
