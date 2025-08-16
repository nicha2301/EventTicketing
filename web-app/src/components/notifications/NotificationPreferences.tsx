'use client'

import { useState, useEffect } from 'react'
import { useNotificationPreferences } from '@/hooks/useNotifications-simple'
import { NotificationChannelPreference } from '@/lib/api/generated/client'
import { Button } from '@/components/ui/button'
import { X, Mail, Smartphone, Bell } from 'lucide-react'

interface NotificationPreferencesProps {
  isOpen: boolean
  onClose: () => void
}

interface PreferenceItemProps {
  label: string
  description: string
  checked: boolean
  onChange: (checked: boolean) => void
}

function PreferenceItem({ label, description, checked, onChange }: PreferenceItemProps) {
  return (
    <div className="flex items-start justify-between py-3">
      <div className="flex-1">
        <h4 className="text-sm font-medium text-gray-900">{label}</h4>
        <p className="text-sm text-gray-500 mt-1">{description}</p>
      </div>
      <label className="relative inline-flex items-center cursor-pointer ml-4">
        <input
          type="checkbox"
          checked={checked}
          onChange={(e) => onChange(e.target.checked)}
          className="sr-only peer"
        />
        <div className="w-11 h-6 bg-gray-200 peer-focus:outline-none peer-focus:ring-4 peer-focus:ring-blue-300 rounded-full peer peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:bg-blue-600"></div>
      </label>
    </div>
  )
}

export function NotificationPreferences({ isOpen, onClose }: NotificationPreferencesProps) {
  const { preferences, isLoading, updatePreferencesMutation } = useNotificationPreferences()
  
  const [emailPrefs, setEmailPrefs] = useState<NotificationChannelPreference>({
    enabled: true,
    accountNotifications: true,
    eventReminders: true,
    marketingNotifications: false,
    commentNotifications: true,
    ratingNotifications: true,
  })
  
  const [pushPrefs, setPushPrefs] = useState<NotificationChannelPreference>({
    enabled: true,
    accountNotifications: true,
    eventReminders: true,
    marketingNotifications: false,
    commentNotifications: true,
    ratingNotifications: true,
  })

  // Load preferences when data is available
  useEffect(() => {
    if (preferences) {
      setEmailPrefs(preferences.email)
      setPushPrefs(preferences.push)
    }
  }, [preferences])

  const handleSave = async () => {
    try {
      await updatePreferencesMutation.mutateAsync({
        data: {
          email: emailPrefs,
          push: pushPrefs
        }
      })
      onClose()
    } catch (error) {
      console.error('Error saving preferences:', error)
    }
  }

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <div className="flex items-center gap-3">
            <Bell className="w-6 h-6 text-blue-600" />
            <h2 className="text-lg font-semibold text-gray-900">Cài đặt thông báo</h2>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 space-y-6">
          {isLoading ? (
            <div className="space-y-4">
              {[...Array(3)].map((_, i) => (
                <div key={i} className="animate-pulse">
                  <div className="h-4 bg-gray-200 rounded w-1/4 mb-2"></div>
                  <div className="h-3 bg-gray-200 rounded w-3/4"></div>
                </div>
              ))}
            </div>
          ) : (
            <>
              {/* Email Notifications */}
              <div>
                <div className="flex items-center gap-2 mb-4">
                  <Mail className="w-5 h-5 text-gray-600" />
                  <h3 className="text-base font-semibold text-gray-900">Thông báo Email</h3>
                </div>
                
                <div className="space-y-1">
                  <PreferenceItem
                    label="Bật thông báo email"
                    description="Nhận tất cả thông báo qua email"
                    checked={emailPrefs.enabled}
                    onChange={(checked) => setEmailPrefs(prev => ({ ...prev, enabled: checked }))}
                  />
                  
                  {emailPrefs.enabled && (
                    <div className="ml-4 pl-4 border-l-2 border-gray-100 space-y-1">
                      <PreferenceItem
                        label="Thông báo tài khoản"
                        description="Kích hoạt tài khoản, đặt lại mật khẩu"
                        checked={emailPrefs.accountNotifications}
                        onChange={(checked) => setEmailPrefs(prev => ({ ...prev, accountNotifications: checked }))}
                      />
                      
                      <PreferenceItem
                        label="Nhắc nhở sự kiện"
                        description="Nhắc nhở về các sự kiện sắp diễn ra"
                        checked={emailPrefs.eventReminders}
                        onChange={(checked) => setEmailPrefs(prev => ({ ...prev, eventReminders: checked }))}
                      />
                      
                      <PreferenceItem
                        label="Bình luận mới"
                        description="Thông báo khi có bình luận mới về sự kiện"
                        checked={emailPrefs.commentNotifications}
                        onChange={(checked) => setEmailPrefs(prev => ({ ...prev, commentNotifications: checked }))}
                      />
                      
                      <PreferenceItem
                        label="Đánh giá mới"
                        description="Thông báo khi có đánh giá mới"
                        checked={emailPrefs.ratingNotifications}
                        onChange={(checked) => setEmailPrefs(prev => ({ ...prev, ratingNotifications: checked }))}
                      />
                      
                      <PreferenceItem
                        label="Thông báo marketing"
                        description="Nhận thông báo về ưu đãi và sự kiện đặc biệt"
                        checked={emailPrefs.marketingNotifications}
                        onChange={(checked) => setEmailPrefs(prev => ({ ...prev, marketingNotifications: checked }))}
                      />
                    </div>
                  )}
                </div>
              </div>

              {/* Push Notifications */}
              <div>
                <div className="flex items-center gap-2 mb-4">
                  <Smartphone className="w-5 h-5 text-gray-600" />
                  <h3 className="text-base font-semibold text-gray-900">Thông báo Push</h3>
                </div>
                
                <div className="space-y-1">
                  <PreferenceItem
                    label="Bật thông báo push"
                    description="Nhận thông báo ngay trên thiết bị"
                    checked={pushPrefs.enabled}
                    onChange={(checked) => setPushPrefs(prev => ({ ...prev, enabled: checked }))}
                  />
                  
                  {pushPrefs.enabled && (
                    <div className="ml-4 pl-4 border-l-2 border-gray-100 space-y-1">
                      <PreferenceItem
                        label="Thông báo tài khoản"
                        description="Kích hoạt tài khoản, đặt lại mật khẩu"
                        checked={pushPrefs.accountNotifications}
                        onChange={(checked) => setPushPrefs(prev => ({ ...prev, accountNotifications: checked }))}
                      />
                      
                      <PreferenceItem
                        label="Nhắc nhở sự kiện"
                        description="Nhắc nhở về các sự kiện sắp diễn ra"
                        checked={pushPrefs.eventReminders}
                        onChange={(checked) => setPushPrefs(prev => ({ ...prev, eventReminders: checked }))}
                      />
                      
                      <PreferenceItem
                        label="Bình luận mới"
                        description="Thông báo khi có bình luận mới về sự kiện"
                        checked={pushPrefs.commentNotifications}
                        onChange={(checked) => setPushPrefs(prev => ({ ...prev, commentNotifications: checked }))}
                      />
                      
                      <PreferenceItem
                        label="Đánh giá mới"
                        description="Thông báo khi có đánh giá mới"
                        checked={pushPrefs.ratingNotifications}
                        onChange={(checked) => setPushPrefs(prev => ({ ...prev, ratingNotifications: checked }))}
                      />
                      
                      <PreferenceItem
                        label="Thông báo marketing"
                        description="Nhận thông báo về ưu đãi và sự kiện đặc biệt"
                        checked={pushPrefs.marketingNotifications}
                        onChange={(checked) => setPushPrefs(prev => ({ ...prev, marketingNotifications: checked }))}
                      />
                    </div>
                  )}
                </div>
              </div>
            </>
          )}
        </div>

        {/* Footer */}
        <div className="flex items-center justify-end gap-3 p-6 border-t border-gray-200">
          <Button variant="outline" onClick={onClose}>
            Hủy
          </Button>
          <Button 
            onClick={handleSave}
            disabled={updatePreferencesMutation.isPending}
          >
            {updatePreferencesMutation.isPending ? (
              <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin mr-2" />
            ) : null}
            Lưu cài đặt
          </Button>
        </div>
      </div>
    </div>
  )
}
