'use client'

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import {
  getUserNotifications,
  getUnreadNotificationsCount,
  markNotificationAsRead,
  markAllNotificationsAsRead,
  deleteNotification,
  deleteAllNotifications,
  getNotificationPreferences,
  updateNotificationPreferences
} from '@/lib/api/modules/notifications'
import type { NotificationResponse, NotificationResponseNotificationType } from '@/lib/api/generated/client'
import { useInitialization } from '@/hooks/useInitialization'

interface UseNotificationsOptions {
  type?: NotificationResponseNotificationType
  unreadOnly?: boolean
}

export function useNotifications(options: UseNotificationsOptions = {}) {
  const queryClient = useQueryClient()
  const { isAuthenticated } = useInitialization()

  const { data: notificationsData, isLoading, refetch } = useQuery({
    queryKey: ['notifications'],
    queryFn: ({ signal }) => getUserNotifications(0, 50, signal),
    enabled: isAuthenticated,
  })

  const { data: unreadCountData } = useQuery({
    queryKey: ['notifications', 'unread-count'],
    queryFn: () => getUnreadNotificationsCount(),
    enabled: isAuthenticated,
  })
  
  const markAsReadMutation = useMutation({
    mutationFn: (notificationId: string) => markNotificationAsRead(notificationId),
    onSuccess: () => {
      toast.success('Đã đánh dấu thông báo đã đọc')
      queryClient.invalidateQueries({ queryKey: ['notifications'] })
      queryClient.invalidateQueries({ queryKey: ['notifications', 'unread-count'] })
    },
    onError: (error: any) => {
      toast.error('Có lỗi xảy ra khi đánh dấu đã đọc')
    }
  })

  const markAllAsReadMutation = useMutation({
    mutationFn: () => markAllNotificationsAsRead(),
    onSuccess: (result: any) => {
      toast.success(`Đã đánh dấu ${result?.data || 0} thông báo đã đọc`)
      queryClient.invalidateQueries({ queryKey: ['notifications'] })
      queryClient.invalidateQueries({ queryKey: ['notifications', 'unread-count'] })
    },
    onError: (error: any) => {
      toast.error('Có lỗi xảy ra khi đánh dấu tất cả đã đọc')
    }
  })

  const deleteNotificationMutation = useMutation({
    mutationFn: (notificationId: string) => deleteNotification(notificationId),
    onSuccess: () => {
      toast.success('Đã xóa thông báo')
      queryClient.invalidateQueries({ queryKey: ['notifications'] })
      queryClient.invalidateQueries({ queryKey: ['notifications', 'unread-count'] })
    },
    onError: (error: any) => {
      toast.error('Có lỗi xảy ra khi xóa thông báo')
    }
  })

  const deleteAllNotificationsMutation = useMutation({
    mutationFn: () => deleteAllNotifications(),
    onSuccess: (result: any) => {
      toast.success(`Đã xóa ${result?.data || 0} thông báo`)
      queryClient.invalidateQueries({ queryKey: ['notifications'] })
      queryClient.invalidateQueries({ queryKey: ['notifications', 'unread-count'] })
    },
    onError: (error: any) => {
      toast.error('Có lỗi xảy ra khi xóa tất cả thông báo')
    }
  })

  const notifications: NotificationResponse[] = (notificationsData as any)?.data?.content || []
  
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
  const queryClient = useQueryClient()
  const { isAuthenticated } = useInitialization()
  
  const { data: preferences, isLoading, refetch } = useQuery({
    queryKey: ['notification-preferences'],
    queryFn: () => getNotificationPreferences(),
    enabled: isAuthenticated,
    staleTime: 5 * 60 * 1000,
  })
  
  const updatePreferencesMutation = useMutation({
    mutationFn: (preferences: any) => updateNotificationPreferences(preferences),
    onSuccess: () => {
      toast.success('Cài đặt thông báo đã được cập nhật')
      queryClient.invalidateQueries({ queryKey: ['notification-preferences'] })
    },
    onError: (error: any) => {
      toast.error('Có lỗi xảy ra khi cập nhật cài đặt')
    }
  })

  return {
    preferences: preferences?.data,
    isLoading,
    updatePreferencesMutation,
    refetch
  }
}
