'use client'

import { useState } from 'react'
import { NotificationCard } from '@/components/notifications/NotificationCard'
import { NotificationFilter } from '@/components/notifications/NotificationFilter'
import { NotificationActions } from '@/components/notifications/NotificationActions'
import { NotificationPreferences } from '@/components/notifications/NotificationPreferences'
import { NotificationResponseNotificationType } from '@/lib/api/generated/client'
import { Bell, Settings, CheckCircle } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { useNotifications } from '@/hooks/useNotifications'
import { useAuthHydration } from '@/hooks/useAuthHydration'
import { PageLoading, ErrorState } from '@/components/ui/LoadingSpinner'

export default function NotificationsPage() {
  const [selectedType, setSelectedType] = useState<NotificationResponseNotificationType | 'ALL'>('ALL')
  const [showPreferences, setShowPreferences] = useState(false)
  const [selectedNotifications, setSelectedNotifications] = useState<string[]>([])
  
  const isHydrated = useAuthHydration()
  const {
    notifications,
    isLoading,
    hasNextPage,
    fetchNextPage,
    isFetchingNextPage,
    markAsReadMutation,
    markAllAsReadMutation,
    deleteNotificationMutation,
    deleteAllNotificationsMutation,
    unreadCount
  } = useNotifications({ type: selectedType === 'ALL' ? undefined : selectedType })

  if (!isHydrated || isLoading) {
    return <PageLoading message="Đang tải thông báo..." />
  }

  const handleSelectNotification = (id: string) => {
    setSelectedNotifications(prev => 
      prev.includes(id) ? prev.filter(notificationId => notificationId !== id) : [...prev, id]
    )
  }

  const handleSelectAll = () => {
    if (selectedNotifications.length === notifications.length) {
      setSelectedNotifications([])
    } else {
      setSelectedNotifications(notifications.map(n => n.id))
    }
  }

  const handleBulkMarkAsRead = async () => {
    if (selectedNotifications.length === 0) return
    
    try {
      await Promise.all(
        selectedNotifications.map(id => markAsReadMutation.mutateAsync(id))
      )
      setSelectedNotifications([])
    } catch (error) {
      console.error('Error marking notifications as read:', error)
    }
  }

  const handleBulkDelete = async () => {
    if (selectedNotifications.length === 0) return
    
    try {
      await Promise.all(
        selectedNotifications.map(id => deleteNotificationMutation.mutateAsync(id))
      )
      setSelectedNotifications([])
    } catch (error) {
      console.error('Error deleting notifications:', error)
    }
  }

  const allNotifications = notifications || []
  const hasUnread = allNotifications.some(n => !n.isRead)

  return (
    <div className="container-page py-8">
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <div className="flex items-center gap-3">
          <div className="relative">
            <Bell className="w-8 h-8 text-blue-600" />
            {(unreadCount as number) > 0 && (
              <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                {(unreadCount as number) > 99 ? '99+' : (unreadCount as number)}
              </span>
            )}
          </div>
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Thông báo</h1>
            <p className="text-gray-600">
              {(unreadCount as number) > 0 ? `${unreadCount as number} thông báo chưa đọc` : 'Tất cả thông báo đã được đọc'}
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          {hasUnread && (
            <Button
              onClick={() => markAllAsReadMutation.mutate()}
              disabled={markAllAsReadMutation.isPending}
              variant="outline"
              size="sm"
            >
              <CheckCircle className="w-4 h-4 mr-2" />
              Đánh dấu tất cả đã đọc
            </Button>
          )}
          
          <Button
            onClick={() => setShowPreferences(true)}
            variant="outline"
            size="sm"
          >
            <Settings className="w-4 h-4 mr-2" />
            Cài đặt
          </Button>
        </div>
      </div>

      {/* Filter */}
      <div className="mb-6">
        <NotificationFilter
          selectedType={selectedType}
          onTypeChange={setSelectedType}
          counts={{
            ALL: allNotifications.length,
            ACCOUNT_ACTIVATION: allNotifications.filter(n => n.notificationType === 'ACCOUNT_ACTIVATION').length,
            PASSWORD_RESET: allNotifications.filter(n => n.notificationType === 'PASSWORD_RESET').length,
            TICKET_CONFIRMATION: allNotifications.filter(n => n.notificationType === 'TICKET_CONFIRMATION').length,
            EVENT_REMINDER: allNotifications.filter(n => n.notificationType === 'EVENT_REMINDER').length,
            NEW_COMMENT: allNotifications.filter(n => n.notificationType === 'NEW_COMMENT').length,
            NEW_RATING: allNotifications.filter(n => n.notificationType === 'NEW_RATING').length,
            TICKET_PURCHASE: allNotifications.filter(n => n.notificationType === 'TICKET_PURCHASE').length,
            SYSTEM: allNotifications.filter(n => n.notificationType === 'SYSTEM').length,
          }}
        />
      </div>

      {/* Bulk Actions */}
      {selectedNotifications.length > 0 && (
        <div className="mb-6">
          <NotificationActions
            selectedCount={selectedNotifications.length}
            totalCount={allNotifications.length}
            onSelectAll={handleSelectAll}
            onMarkAsRead={handleBulkMarkAsRead}
            onDelete={handleBulkDelete}
            onDeleteAll={() => deleteAllNotificationsMutation.mutate()}
            isMarkingAsRead={markAsReadMutation.isPending}
            isDeleting={deleteNotificationMutation.isPending}
            isDeletingAll={deleteAllNotificationsMutation.isPending}
          />
        </div>
      )}

      {/* Notifications List */}
      <div className="space-y-4">
        {allNotifications.length === 0 ? (
          // Empty state
          <div className="text-center py-12">
            <Bell className="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <h3 className="text-lg font-semibold text-gray-900 mb-2">
              {selectedType === 'ALL' ? 'Không có thông báo nào' : 'Không có thông báo loại này'}
            </h3>
            <p className="text-gray-500 mb-6">
              {selectedType === 'ALL' 
                ? 'Bạn sẽ nhận được thông báo về các hoạt động quan trọng tại đây.'
                : 'Thay đổi bộ lọc để xem các loại thông báo khác.'
              }
            </p>
            {selectedType !== 'ALL' && (
              <Button onClick={() => setSelectedType('ALL')} variant="outline">
                Xem tất cả thông báo
              </Button>
            )}
          </div>
        ) : (
          <>
            {allNotifications.map((notification) => (
              <NotificationCard
                key={notification.id}
                notification={notification}
                isSelected={selectedNotifications.includes(notification.id)}
                onSelect={handleSelectNotification}
                onMarkAsRead={(id) => markAsReadMutation.mutate(id)}
                onDelete={(id) => deleteNotificationMutation.mutate(id)}
              />
            ))}

            {/* Load More */}
            {hasNextPage && (
              <div className="text-center py-4">
                <Button
                  onClick={() => fetchNextPage()}
                  disabled={isFetchingNextPage}
                  variant="outline"
                >
                  {isFetchingNextPage ? 'Đang tải...' : 'Tải thêm'}
                </Button>
              </div>
            )}
          </>
        )}
      </div>

      {/* Preferences Modal */}
      <NotificationPreferences
        isOpen={showPreferences}
        onClose={() => setShowPreferences(false)}
      />
    </div>
  )
}
