'use client'

import { useState } from 'react'
import { User, Settings, Shield, Bell, Camera, Edit3, CheckCircle } from 'lucide-react'
import { cn } from '@/lib/utils/cn'
import { useUserProfile } from '@/hooks/useUserProfile'
import { useAuthHydration } from '@/hooks/useAuthHydration'
import { useAuthStore } from '@/store/auth'
import { PageLoading, ErrorState } from '@/components/ui/LoadingSpinner'
import { Button } from '@/components/ui/button'
import { ProfileHeader } from '@/components/profile/ProfileHeader'
import { ProfileTabs } from '@/components/profile/ProfileTabs'
import { PersonalInfo } from '@/components/profile/PersonalInfo'
import { AccountSecurity } from '@/components/profile/AccountSecurity'
import { NotificationSettings } from '@/components/profile/NotificationSettings'
import { ActivityHistory } from '@/components/profile/ActivityHistory'
import Image from 'next/image'

// Tab definitions với detailed info
const tabs = [
  {
    id: 'personal',
    label: 'Thông tin cá nhân',
    icon: User,
    description: 'Quản lý thông tin và ảnh đại diện của bạn',
  },
  {
    id: 'security', 
    label: 'Bảo mật tài khoản',
    icon: Shield,
    description: 'Đổi mật khẩu và cài đặt bảo mật',
  },
  {
    id: 'notifications',
    label: 'Thông báo', 
    icon: Bell,
    description: 'Cài đặt thông báo và email',
  },
  {
    id: 'activity',
    label: 'Hoạt động',
    icon: Settings,
    description: 'Lịch sử hoạt động và phiên đăng nhập',
  },
]

export default function ProfilePage() {
  const [activeTab, setActiveTab] = useState('personal')
  const isHydrated = useAuthHydration()
  const { currentUser } = useAuthStore()
  const { data: user, isLoading, isError, refetch } = useUserProfile()
  const currentUserData = user || currentUser

  // Show loading on SSR or when data is loading
  if (!isHydrated || isLoading) {
    return <PageLoading message="Đang tải thông tin tài khoản..." />
  }

  // Show error state
  if (isError) {
    return (
      <ErrorState
        title="Không thể tải thông tin tài khoản"
        message="Vui lòng thử lại sau hoặc kiểm tra kết nối mạng."
        onRetry={() => refetch()}
      />
    )
  }

  const renderTabContent = () => {
    if (!currentUserData) {
      return (
        <div className="p-6 text-center">
          <p className="text-gray-500">Không thể tải thông tin người dùng</p>
        </div>
      )
    }

    switch (activeTab) {
      case 'personal':
        return <PersonalInfo user={currentUserData as any} />
      case 'security':
        return <AccountSecurity />
      case 'notifications':
        return <NotificationSettings />
      case 'activity':
        return <ActivityHistory />
      default:
        return <PersonalInfo user={currentUserData as any} />
    }
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header Section */}
      <div className="bg-white border-b">
        <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
          <ProfileHeader user={currentUserData as any} />
        </div>
      </div>

      {/* Main Content */}
      <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
          {/* Sidebar Navigation */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-xl shadow-sm p-6 sticky top-8">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">Cài đặt tài khoản</h2>
              <ProfileTabs 
                tabs={tabs}
                activeTab={activeTab}
                onTabChange={setActiveTab}
              />
            </div>
          </div>

          {/* Main Content Area */}
          <div className="lg:col-span-3">
            <div className="bg-white rounded-xl shadow-sm">
              {renderTabContent()}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
