'use client'

import Image from 'next/image'
import { User } from 'lucide-react'
import { cn } from '@/lib/utils/cn'

interface User {
  id: string
  email: string
  fullName: string
  role: 'USER' | 'ORGANIZER' | 'ADMIN'
  profilePictureUrl?: string
}

interface ProfileHeaderProps {
  user: User | null
}

export function ProfileHeader({ user }: ProfileHeaderProps) {
  const calculateProfileCompletion = () => {
    if (!user) return 0
    
    let completedFields = 0
    let totalFields = 3 // Chỉ tính name, email, phone (không có avatar)
    
    if (user.fullName) completedFields++
    if (user.email) completedFields++
    if ((user as any).phoneNumber) completedFields++
    
    return Math.round((completedFields / totalFields) * 100)
  }

  const profileCompletion = calculateProfileCompletion()
  
  const getCompletionMessage = () => {
    if (profileCompletion === 100) return 'Hồ sơ của bạn đã hoàn thiện!'
    if (profileCompletion >= 75) return 'Hồ sơ gần hoàn thiện rồi!'
    if (profileCompletion >= 50) return 'Hoàn thiện thêm để tăng độ tin cậy'
    return 'Hãy hoàn thiện hồ sơ để có trải nghiệm tốt hơn'
  }

  const getRoleBadge = (role: string) => {
    const styles = {
      USER: 'bg-blue-100 text-blue-800',
      ORGANIZER: 'bg-purple-100 text-purple-800',
      ADMIN: 'bg-red-100 text-red-800'
    }
    
    const labels = {
      USER: 'Người dùng',
      ORGANIZER: 'Tổ chức sự kiện',
      ADMIN: 'Quản trị viên'
    }

    return (
      <span className={cn(
        'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
        styles[role as keyof typeof styles] || styles.USER
      )}>
        {labels[role as keyof typeof labels] || labels.USER}
      </span>
    )
  }

  if (!user) {
    return (
      <div className="py-8">
        <div className="animate-pulse">
          <div className="flex items-center space-x-6">
            <div className="w-24 h-24 bg-gray-200 rounded-full"></div>
            <div className="space-y-3">
              <div className="h-6 bg-gray-200 rounded w-48"></div>
              <div className="h-4 bg-gray-200 rounded w-32"></div>
              <div className="h-4 bg-gray-200 rounded w-24"></div>
            </div>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="py-8">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-6">
        {/* Avatar and Basic Info */}
        <div className="flex items-center space-x-6">
          {/* Avatar - Display Only */}
          <div className="w-24 h-24 rounded-full overflow-hidden bg-gray-100 flex items-center justify-center">
            {user.profilePictureUrl ? (
              <Image
                src={user.profilePictureUrl}
                alt={user.fullName}
                width={96}
                height={96}
                className="w-full h-full object-cover"
              />
            ) : (
              <User className="w-10 h-10 text-gray-400" />
            )}
          </div>

          {/* User Info */}
          <div className="space-y-2">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">{user.fullName}</h1>
            </div>

            {/* Email and Role */}
            <div className="space-y-1">
              <p className="text-gray-600">{user.email}</p>
              <div className="flex items-center gap-2">
                {getRoleBadge(user.role)}
                <span className="text-xs text-gray-500">ID: {user.id.slice(0, 8)}...</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Profile Completion Progress */}
      <div className={cn(
        "mt-6 rounded-lg p-4",
        profileCompletion === 100 ? "bg-green-50" : "bg-blue-50"
      )}>
        <div className="flex items-center justify-between mb-2">
          <span className={cn(
            "text-sm font-medium",
            profileCompletion === 100 ? "text-green-900" : "text-blue-900"
          )}>
            Hoàn thiện hồ sơ
          </span>
          <span className={cn(
            "text-sm",
            profileCompletion === 100 ? "text-green-600" : "text-blue-600"
          )}>
            {profileCompletion}%
          </span>
        </div>
        <div className={cn(
          "w-full rounded-full h-2",
          profileCompletion === 100 ? "bg-green-200" : "bg-blue-200"
        )}>
          <div 
            className={cn(
              "h-2 rounded-full transition-all duration-500",
              profileCompletion === 100 ? "bg-green-600" : "bg-blue-600"
            )}
            style={{ width: `${profileCompletion}%` }}
          ></div>
        </div>
        <p className={cn(
          "text-xs mt-2",
          profileCompletion === 100 ? "text-green-700" : "text-blue-700"
        )}>
          {getCompletionMessage()}
        </p>
      </div>
    </div>
  )
}
