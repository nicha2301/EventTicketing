'use client'

import { 
  useCountUnreadNotifications,
  useMarkAsRead,
  useMarkAllAsRead,
  useDeleteNotification,
  useDeleteAllNotifications,
  useGetNotificationPreferences,
  useUpdateNotificationPreferences,
  useGetUserNotifications,
  NotificationResponseNotificationType,
  NotificationResponse
} from '@/lib/api/generated/client'
import { toast } from 'sonner'

interface UseNotificationsOptions {
  type?: NotificationResponseNotificationType
  unreadOnly?: boolean
}

export function useNotifications(options: UseNotificationsOptions = {}) {
  const { data: notificationsData, isLoading, refetch } = useGetUserNotifications({
    pageable: { page: 0, size: 50 }
  })

  const { data: unreadCountData } = useCountUnreadNotifications()
  
  const markAsReadMutation = useMarkAsRead({
    mutation: {
      onSuccess: () => {
        toast.success('Đã đánh dấu thông báo đã đọc')
        refetch()
      },
      onError: (error: any) => {
        toast.error('Có lỗi xảy ra khi đánh dấu đã đọc')
        console.error('Mark as read error:', error)
      }
    }
  })

  const markAllAsReadMutation = useMarkAllAsRead({
    mutation: {
      onSuccess: (result: any) => {
        toast.success(`Đã đánh dấu ${result?.data || 0} thông báo đã đọc`)
        refetch()
      },
      onError: (error: any) => {
        toast.error('Có lỗi xảy ra khi đánh dấu tất cả đã đọc')
        console.error('Mark all as read error:', error)
      }
    }
  })

  const deleteNotificationMutation = useDeleteNotification({
    mutation: {
      onSuccess: () => {
        toast.success('Đã xóa thông báo')
        refetch()
      },
      onError: (error: any) => {
        toast.error('Có lỗi xảy ra khi xóa thông báo')
        console.error('Delete notification error:', error)
      }
    }
  })

  const deleteAllNotificationsMutation = useDeleteAllNotifications({
    mutation: {
      onSuccess: (result: any) => {
        toast.success(`Đã xóa ${result?.data || 0} thông báo`)
        refetch()
      },
      onError: (error: any) => {
        toast.error('Có lỗi xảy ra khi xóa tất cả thông báo')
        console.error('Delete all notifications error:', error)
      }
    }
  })

  const notifications: NotificationResponse[] = notificationsData?.data?.content || []
  
  return {
    notifications,
    isLoading,
    refetch,
    hasNextPage: false,
    fetchNextPage: () => {},
    isFetchingNextPage: false,
    unreadCount: unreadCountData?.data || 0,
    markAsReadMutation,
    markAllAsReadMutation,
    deleteNotificationMutation,
    deleteAllNotificationsMutation,
  }
}

// Hook cho notification preferences
export function useNotificationPreferences() {
  const { data: preferences, isLoading, refetch } = useGetNotificationPreferences()
  
  const updatePreferencesMutation = useUpdateNotificationPreferences({
    mutation: {
      onSuccess: () => {
        toast.success('Cài đặt thông báo đã được cập nhật')
        refetch()
      },
      onError: (error: any) => {
        toast.error('Có lỗi xảy ra khi cập nhật cài đặt')
        console.error('Update preferences error:', error)
      }
    }
  })

  return {
    preferences: preferences?.data,
    isLoading,
    updatePreferencesMutation,
    refetch
  }
}
