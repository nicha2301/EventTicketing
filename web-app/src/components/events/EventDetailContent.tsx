'use client'

import Image from 'next/image'
import Link from 'next/link'
import { ChevronRight, MapPin, Calendar, Clock, Users, Star, Heart, Share2, Bookmark, ShoppingCart } from 'lucide-react'
import { EventDto, cancelEvent, publishEvent } from '@/lib/api/generated/client'
import { Button } from '@/components/ui/button'
import { EventRatings } from '@/components/ratings/EventRatings'
import { sanitizeEventImageUrl } from '@/lib/utils/image'
import { useEventRatingStats } from '@/hooks/useEventRatingStats'
import { useAuthStore } from '@/store/auth'
import { useMutation } from '@tanstack/react-query'
import { useState } from 'react'
import { toast } from 'sonner'

interface EventDetailContentProps {
  event: EventDto
}

export function EventDetailContent({ event }: EventDetailContentProps) {
  const { averageRating, totalRatings, isLoading: ratingsLoading } = useEventRatingStats(event.id)
  const { currentUser } = useAuthStore()
  const isOrganizer = !!currentUser && (currentUser.role === 'ADMIN' || currentUser.id === event.organizerId)
  const [currentStatus, setCurrentStatus] = useState(event.status)
  const publishMut = useMutation({
    mutationFn: async () => publishEvent(event.id as string),
    onSuccess: () => { setCurrentStatus('PUBLISHED' as any); toast.success('Đã xuất bản sự kiện'); },
    onError: (e: any) => toast.error(e?.response?.data?.message || 'Xuất bản thất bại')
  })
  const cancelMut = useMutation({
    mutationFn: async (reason: string) => cancelEvent(event.id as string, { reason }),
    onSuccess: () => { setCurrentStatus('CANCELLED' as any); toast.success('Đã huỷ sự kiện'); },
    onError: (e: any) => toast.error(e?.response?.data?.message || 'Huỷ sự kiện thất bại')
  })
  
  const formatPrice = (minPrice?: number, maxPrice?: number, isFree?: boolean) => {
    if (isFree) return 'Miễn phí'
    if (!minPrice && !maxPrice) return 'Liên hệ'
    if (minPrice === maxPrice) {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(minPrice || 0)
    }
    return `${new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(minPrice || 0)} - ${new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(maxPrice || 0)}`
  }

  const formatDate = (dateString: string) => {
    const date = new Date(dateString)
    return date.toLocaleDateString('vi-VN', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    })
  }

  const formatTime = (dateString: string) => {
    const date = new Date(dateString)
    return date.toLocaleTimeString('vi-VN', {
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Breadcrumb */}
      <div className="bg-white border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <nav className="flex items-center space-x-2 text-sm text-gray-500">
            <Link href="/" className="hover:text-blue-600">Trang chủ</Link>
            <ChevronRight className="w-4 h-4" />
            <Link href="/search" className="hover:text-blue-600">Sự kiện</Link>
            <ChevronRight className="w-4 h-4" />
            <span className="text-gray-900 font-medium">{event.title}</span>
          </nav>
        </div>
      </div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main Content */}
          <div className="lg:col-span-2 space-y-8">
            {/* Hero Section */}
            <div className="bg-white rounded-xl shadow-sm overflow-hidden">
              {/* Event Image */}
              <div className="relative h-64 sm:h-80">
                <Image
                  src={sanitizeEventImageUrl(event.featuredImageUrl, event.imageUrls)}
                  alt={event.title}
                  fill
                  className="object-cover"
                />
              </div>
              
              <div className="p-6">
                <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4 mb-6">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-2">
                      {event.categoryName && (
                        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                          {event.categoryName}
                        </span>
                      )}
                      <div className="flex items-center gap-1">
                        <Star className="w-4 h-4 fill-yellow-400 text-yellow-400" />
                        <span className="text-sm font-medium">
                          {ratingsLoading ? '...' : totalRatings > 0 ? averageRating.toFixed(1) : 'Chưa có'}
                        </span>
                        <span className="text-sm text-gray-500">
                          ({ratingsLoading ? '...' : totalRatings} đánh giá)
                        </span>
                      </div>
                    </div>
                    <h1 className="text-3xl font-bold text-gray-900 mb-2">{event.title}</h1>
                    <div className="flex items-center gap-4 text-gray-600">
                      <div className="flex items-center gap-1">
                        <MapPin className="w-4 h-4" />
                        <span className="text-sm">{event.city || event.address}</span>
                      </div>
                      <div className="flex items-center gap-1">
                        <Calendar className="w-4 h-4" />
                        <span className="text-sm">
                          {formatDate(event.startDate)}
                        </span>
                      </div>
                    </div>
                  </div>
                  
                  <div className="flex flex-col items-end gap-3">
                    <div className="text-right">
                      <div className="text-3xl font-bold text-blue-600">
                        {formatPrice(event.minTicketPrice, event.maxTicketPrice, event.isFree)}
                      </div>
                      {!event.isFree && event.minTicketPrice && (
                        <div className="text-sm text-gray-500">/ người</div>
                      )}
                    </div>
                    
                    {/* Action Buttons */}
                    <div className="flex items-center gap-2">
                      <Link href={`/events/${event.id}/purchase`}>
                        <Button className="px-6">
                          <ShoppingCart className="w-4 h-4 mr-2" />
                          Mua vé
                        </Button>
                      </Link>
                      <Button variant="outline" size="sm">
                        <Heart className="w-4 h-4 mr-1" />
                        Yêu thích
                      </Button>
                      <Button variant="outline" size="sm">
                        <Bookmark className="w-4 h-4 mr-1" />
                        Lưu
                      </Button>
                      <Button variant="outline" size="sm">
                        <Share2 className="w-4 h-4 mr-1" />
                        Chia sẻ
                      </Button>
                    </div>

                    {isOrganizer && (
                      <div className="flex flex-wrap items-center gap-2 pt-2">
                        <Link href={`/organizer/events/${event.id}/edit`} className="text-sm text-blue-600 hover:text-blue-800">Sửa</Link>
                        <Link href={`/organizer/events/${event.id}/tickets`} className="text-sm text-gray-700 hover:text-gray-900">Vé</Link>
                        <Link href={`/organizer/events/${event.id}/check-in`} className="text-sm text-gray-700 hover:text-gray-900">Check-in</Link>
                        <Link href={`/organizer/reports`} className="text-sm text-gray-700 hover:text-gray-900">Báo cáo</Link>
                        <Link href={`/organizer/analytics`} className="text-sm text-gray-700 hover:text-gray-900">Analytics</Link>
                        {currentStatus === 'DRAFT' && (
                          <button
                            onClick={() => publishMut.mutate()}
                            className="text-sm text-green-700 hover:text-green-900"
                            disabled={publishMut.isPending}
                          >Xuất bản</button>
                        )}
                        {currentStatus !== 'CANCELLED' && (
                          <button
                            onClick={() => { const r = prompt('Lý do huỷ?'); if (r) cancelMut.mutate(r) }}
                            className="text-sm text-red-700 hover:text-red-900"
                            disabled={cancelMut.isPending}
                          >Huỷ</button>
                        )}
                      </div>
                    )}
                  </div>
                </div>

                {/* Quick Info */}
                <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 p-4 bg-gray-50 rounded-lg">
                  <div className="text-center">
                    <div className="text-2xl font-bold text-gray-900">
                      {ratingsLoading ? '...' : totalRatings > 0 ? averageRating.toFixed(1) : '0'}
                    </div>
                    <div className="text-sm text-gray-500">Đánh giá</div>
                  </div>
                  <div className="text-center">
                    <div className="text-2xl font-bold text-gray-900">{event.currentAttendees || 0}</div>
                    <div className="text-sm text-gray-500">Đã tham gia</div>
                  </div>
                  <div className="text-center">
                    <div className="text-2xl font-bold text-gray-900">{event.maxAttendees || '∞'}</div>
                    <div className="text-sm text-gray-500">Giới hạn</div>
                  </div>
                  <div className="text-center">
                    <div className="text-2xl font-bold text-gray-900">
                      {event.maxAttendees ? Math.max(0, event.maxAttendees - (event.currentAttendees || 0)) : '∞'}
                    </div>
                    <div className="text-sm text-gray-500">Còn lại</div>
                  </div>
                </div>
              </div>
            </div>

            {/* Event Description */}
            <div className="bg-white rounded-xl shadow-sm p-6">
              <h2 className="text-xl font-bold text-gray-900 mb-4">Mô tả sự kiện</h2>
              <div className="prose prose-gray max-w-none">
                <p>{event.description || event.shortDescription || 'Mô tả sự kiện sẽ được cập nhật sớm.'}</p>
              </div>
            </div>

            {/* Event Ratings & Reviews */}
            {event.id && <EventRatings eventId={event.id} />}

            {/* Event Schedule */}
            <div className="bg-white rounded-xl shadow-sm p-6">
              <h2 className="text-xl font-bold text-gray-900 mb-4">Lịch trình</h2>
              <div className="space-y-4">
                <div className="flex items-center gap-3">
                  <Calendar className="w-5 h-5 text-blue-600" />
                  <div>
                    <div className="font-medium">{formatDate(event.startDate)}</div>
                    <div className="text-sm text-gray-500">
                      {formatTime(event.startDate)} 
                      {event.endDate && ` - ${formatTime(event.endDate)}`}
                    </div>
                  </div>
                </div>
                <div className="flex items-center gap-3">
                  <MapPin className="w-5 h-5 text-blue-600" />
                  <div>
                    <div className="font-medium">
                      {event.locationName && event.address 
                        ? `${event.locationName} - ${event.address}`
                        : event.locationName || event.address || 'Địa điểm sẽ được thông báo'
                      }
                    </div>
                    {event.city && (
                      <div className="text-sm text-gray-500">{event.city}</div>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* Event Info Card */}
            <div className="bg-white rounded-xl shadow-sm p-6">
              <h3 className="text-lg font-bold text-gray-900 mb-4">Thông tin sự kiện</h3>
              <div className="space-y-3">
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">Ngày bắt đầu</span>
                  <span className="font-medium">{formatDate(event.startDate)}</span>
                </div>
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">Thời gian</span>
                  <span className="font-medium">{formatTime(event.startDate)}</span>
                </div>
                {event.endDate && (
                  <div className="flex items-center justify-between">
                    <span className="text-gray-600">Kết thúc</span>
                    <span className="font-medium">{formatTime(event.endDate)}</span>
                  </div>
                )}
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">Giá vé</span>
                  <span className="font-medium">{formatPrice(event.minTicketPrice, event.maxTicketPrice, event.isFree)}</span>
                </div>
                {event.categoryName && (
                  <div className="flex items-center justify-between">
                    <span className="text-gray-600">Thể loại</span>
                    <span className="font-medium">{event.categoryName}</span>
                  </div>
                )}
                <div className="flex items-center justify-between">
                  <span className="text-gray-600">Trạng thái</span>
                  <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                    currentStatus === 'PUBLISHED' ? 'bg-green-100 text-green-800' :
                    currentStatus === 'DRAFT' ? 'bg-yellow-100 text-yellow-800' :
                    'bg-gray-100 text-gray-800'
                  }`}>
                    {currentStatus === 'PUBLISHED' ? 'Đang mở bán' :
                     currentStatus === 'DRAFT' ? 'Bản nháp' :
                     currentStatus}
                  </span>
                </div>
                {event.maxAttendees && (
                  <div className="flex items-center justify-between">
                    <span className="text-gray-600">Sức chứa</span>
                    <span className="font-medium">{event.currentAttendees || 0}/{event.maxAttendees}</span>
                  </div>
                )}
              </div>
              
              <Link href={`/events/${event.id}/purchase`}>
                <Button className="w-full mt-6">
                  <ShoppingCart className="w-4 h-4 mr-2" />
                  Mua vé ngay
                </Button>
              </Link>
            </div>

            {/* Organizer Card */}
            <div className="bg-white rounded-xl shadow-sm p-6">
              <h3 className="text-lg font-bold text-gray-900 mb-4">Ban tổ chức</h3>
              <div className="flex items-center gap-3 mb-4">
                <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
                  <Users className="w-6 h-6 text-blue-600" />
                </div>
                <div>
                  <div className="font-medium">{event.organizerName || 'Đang cập nhật'}</div>
                  <div className="text-sm text-gray-500">Tổ chức sự kiện chuyên nghiệp</div>
                </div>
              </div>
              <Button variant="outline" className="w-full">
                Xem thông tin
              </Button>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
