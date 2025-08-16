'use client'

import { useState } from 'react'
import { Bell, Mail, Smartphone, Calendar, Heart, MessageSquare, CreditCard, Settings } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { toast } from 'sonner'

interface NotificationSetting {
  id: string
  title: string
  description: string
  icon: React.ComponentType<any>
  categories: {
    email: boolean
    push: boolean
    sms: boolean
  }
}

export function NotificationSettings() {
  const [settings, setSettings] = useState<NotificationSetting[]>([
    {
      id: 'events',
      title: 'Sự kiện mới',
      description: 'Thông báo về sự kiện mới phù hợp với sở thích của bạn',
      icon: Calendar,
      categories: { email: true, push: true, sms: false }
    },
    {
      id: 'bookings',
      title: 'Đặt vé và thanh toán',
      description: 'Xác nhận đặt vé, thanh toán và thông tin vé',
      icon: CreditCard,
      categories: { email: true, push: true, sms: true }
    },
    {
      id: 'reminders',
      title: 'Nhắc nhở sự kiện',
      description: 'Nhắc nhở trước khi sự kiện diễn ra',
      icon: Bell,
      categories: { email: true, push: true, sms: false }
    },
    {
      id: 'social',
      title: 'Hoạt động xã hội',
      description: 'Lượt thích, bình luận và theo dõi từ người khác',
      icon: Heart,
      categories: { email: false, push: true, sms: false }
    },
    {
      id: 'messages',
      title: 'Tin nhắn',
      description: 'Tin nhắn từ ban tổ chức và người dùng khác',
      icon: MessageSquare,
      categories: { email: true, push: true, sms: false }
    },
    {
      id: 'account',
      title: 'Tài khoản',
      description: 'Thay đổi tài khoản, bảo mật và cập nhật hệ thống',
      icon: Settings,
      categories: { email: true, push: false, sms: true }
    }
  ])

  const [isSaving, setIsSaving] = useState(false)

  const toggleNotification = async (settingId: string, category: keyof NotificationSetting['categories']) => {
    setSettings(prev => prev.map(setting => 
      setting.id === settingId 
        ? {
            ...setting,
            categories: {
              ...setting.categories,
              [category]: !setting.categories[category]
            }
          }
        : setting
    ))

    // Auto-save after toggle
    try {
      await new Promise(resolve => setTimeout(resolve, 500))
      toast.success('Cài đặt đã được cập nhật!')
    } catch (error) {
      toast.error('Có lỗi xảy ra khi cập nhật cài đặt')
    }
  }

  const handleSaveSettings = async () => {
    setIsSaving(true)
    try {
      // TODO: Implement API call to save notification settings
      await new Promise(resolve => setTimeout(resolve, 1500))
      toast.success('Cài đặt thông báo đã được lưu!')
    } catch (error) {
      toast.error('Có lỗi xảy ra khi lưu cài đặt')
    } finally {
      setIsSaving(false)
    }
  }

  const getMethodIcon = (method: string) => {
    switch (method) {
      case 'email': return Mail
      case 'push': return Smartphone
      case 'sms': return MessageSquare
      default: return Bell
    }
  }

  const getMethodLabel = (method: string) => {
    switch (method) {
      case 'email': return 'Email'
      case 'push': return 'Thông báo đẩy'
      case 'sms': return 'SMS'
      default: return method
    }
  }

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div>
        <h2 className="text-xl font-semibold text-gray-900 flex items-center gap-2">
          <Bell className="w-5 h-5" />
          Cài đặt thông báo
        </h2>
        <p className="text-gray-600 text-sm mt-1">
          Quản lý cách bạn nhận thông báo từ EventTicketing
        </p>
      </div>

      {/* Notification Methods Legend */}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <h3 className="font-medium text-blue-900 mb-3">Phương thức thông báo</h3>
        <div className="grid grid-cols-3 gap-4">
          {['email', 'push', 'sms'].map((method) => {
            const Icon = getMethodIcon(method)
            return (
              <div key={method} className="flex items-center gap-2">
                <Icon className="w-4 h-4 text-blue-600" />
                <span className="text-sm text-blue-800">{getMethodLabel(method)}</span>
              </div>
            )
          })}
        </div>
      </div>

      {/* Notification Settings */}
      <div className="space-y-4">
        {settings.map((setting) => {
          const Icon = setting.icon
          return (
            <div key={setting.id} className="border border-gray-200 rounded-lg p-6">
              <div className="flex items-start gap-4">
                {/* Icon */}
                <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center flex-shrink-0">
                  <Icon className="w-5 h-5 text-blue-600" />
                </div>

                {/* Content */}
                <div className="flex-1 min-w-0">
                  <h3 className="font-medium text-gray-900">{setting.title}</h3>
                  <p className="text-sm text-gray-600 mt-1">{setting.description}</p>

                  {/* Toggle Switches */}
                  <div className="flex items-center gap-6 mt-4">
                    {Object.entries(setting.categories).map(([method, enabled]) => {
                      const MethodIcon = getMethodIcon(method)
                      return (
                        <label
                          key={method}
                          className="flex items-center gap-2 cursor-pointer"
                        >
                          <div className="flex items-center gap-2 text-sm text-gray-600">
                            <MethodIcon className="w-4 h-4" />
                            {getMethodLabel(method)}
                          </div>
                          <div
                            onClick={() => toggleNotification(setting.id, method as keyof NotificationSetting['categories'])}
                            className={`relative inline-flex h-5 w-9 items-center rounded-full transition-colors ${
                              enabled ? 'bg-blue-600' : 'bg-gray-300'
                            }`}
                          >
                            <span
                              className={`inline-block h-3 w-3 transform rounded-full bg-white transition-transform ${
                                enabled ? 'translate-x-5' : 'translate-x-1'
                              }`}
                            />
                          </div>
                        </label>
                      )
                    })}
                  </div>
                </div>
              </div>
            </div>
          )
        })}
      </div>

      {/* Global Settings */}
      <div className="border border-gray-200 rounded-lg p-6">
        <h3 className="font-medium text-gray-900 mb-4">Cài đặt chung</h3>
        <div className="space-y-4">
          <label className="flex items-center justify-between cursor-pointer">
            <div>
              <div className="font-medium text-gray-900">Thông báo marketing</div>
              <div className="text-sm text-gray-600">Nhận thông tin về sự kiện khuyến mãi và ưu đãi đặc biệt</div>
            </div>
            <div className="relative inline-flex h-5 w-9 items-center rounded-full bg-gray-300">
              <span className="inline-block h-3 w-3 transform rounded-full bg-white translate-x-1" />
            </div>
          </label>

          <label className="flex items-center justify-between cursor-pointer">
            <div>
              <div className="font-medium text-gray-900">Thông báo không hoạt động</div>
              <div className="text-sm text-gray-600">Nhận thông báo khi bạn không hoạt động trong thời gian dài</div>
            </div>
            <div className="relative inline-flex h-5 w-9 items-center rounded-full bg-blue-600">
              <span className="inline-block h-3 w-3 transform rounded-full bg-white translate-x-5" />
            </div>
          </label>

          <label className="flex items-center justify-between cursor-pointer">
            <div>
              <div className="font-medium text-gray-900">Tóm tắt hàng tuần</div>
              <div className="text-sm text-gray-600">Nhận email tóm tắt hoạt động hàng tuần</div>
            </div>
            <div className="relative inline-flex h-5 w-9 items-center rounded-full bg-blue-600">
              <span className="inline-block h-3 w-3 transform rounded-full bg-white translate-x-5" />
            </div>
          </label>
        </div>
      </div>

      {/* Notification Schedule */}
      <div className="border border-gray-200 rounded-lg p-6">
        <h3 className="font-medium text-gray-900 mb-4">Lịch trình thông báo</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="text-sm font-medium text-gray-700">Giờ không làm phiền (từ)</label>
            <input
              type="time"
              defaultValue="22:00"
              className="mt-1 w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          <div>
            <label className="text-sm font-medium text-gray-700">Giờ không làm phiền (đến)</label>
            <input
              type="time"
              defaultValue="07:00"
              className="mt-1 w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
        </div>
        <p className="text-xs text-gray-500 mt-2">
          Bạn sẽ không nhận thông báo đẩy trong khoảng thời gian này (trừ thông báo khẩn cấp)
        </p>
      </div>

      {/* Save Button */}
      <div className="flex justify-end pt-4 border-t">
        <Button
          onClick={handleSaveSettings}
          disabled={isSaving}
        >
          {isSaving ? (
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
              Đang lưu...
            </div>
          ) : (
            'Lưu cài đặt'
          )}
        </Button>
      </div>
    </div>
  )
}
