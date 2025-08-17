'use client'

import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { User, Mail, Phone, Save, X } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { toast } from 'sonner'
import { useAuthStore } from '@/store/auth'
import { useUpdateCurrentUser } from '@/lib/api/generated/client'

interface User {
  id: string
  email: string
  fullName: string
  role: 'USER' | 'ORGANIZER' | 'ADMIN'
  profilePictureUrl?: string
  phoneNumber?: string
  address?: string
  dateOfBirth?: string
  occupation?: string
  bio?: string
}

interface PersonalInfoProps {
  user: User
}

// Validation schema - chỉ các trường từ UserUpdateDto
const personalInfoSchema = z.object({
  fullName: z.string().min(2, 'Họ tên phải có ít nhất 2 ký tự'),
  phoneNumber: z.string().optional(),
})

type PersonalInfoInput = z.infer<typeof personalInfoSchema>

export function PersonalInfo({ user }: PersonalInfoProps) {
  const [isEditing, setIsEditing] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const updateCurrentUserMutation = useUpdateCurrentUser()

  const {
    register,
    handleSubmit,
    formState: { errors, isDirty },
    reset,
  } = useForm<PersonalInfoInput>({
    resolver: zodResolver(personalInfoSchema),
    defaultValues: {
      fullName: user?.fullName || '',
      phoneNumber: user?.phoneNumber || '',
    },
  })

  const onSubmit = async (data: PersonalInfoInput) => {
    setIsSaving(true)
    try {
      // Call real API to update user profile
      await updateCurrentUserMutation.mutateAsync({
        data: {
          fullName: data.fullName,
          phoneNumber: data.phoneNumber || undefined,
        }
      })
      
      // Update auth store with new data
      const { updateUser } = useAuthStore.getState()
      updateUser({
        fullName: data.fullName,
        ...(data.phoneNumber && { phoneNumber: data.phoneNumber }),
      } as any)
      
      toast.success('Thông tin đã được cập nhật thành công!')
      setIsEditing(false)
    } catch (error) {
      console.error('Update profile error:', error)
      toast.error('Có lỗi xảy ra khi cập nhật thông tin')
    } finally {
      setIsSaving(false)
    }
  }

  const handleCancel = () => {
    reset()
    setIsEditing(false)
  }

  if (!user) {
    return (
      <div className="p-6">
        <div className="animate-pulse space-y-6">
          {Array.from({ length: 6 }).map((_, i) => (
            <div key={i} className="space-y-2">
              <div className="h-4 bg-gray-200 rounded w-24"></div>
              <div className="h-10 bg-gray-200 rounded"></div>
            </div>
          ))}
        </div>
      </div>
    )
  }

  return (
    <div className="p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-xl font-semibold text-gray-900">Thông tin cá nhân</h2>
          <p className="text-gray-600 text-sm mt-1">
            Quản lý thông tin cá nhân và cách người khác nhìn thấy bạn trên EventTicketing
          </p>
        </div>
        {!isEditing && (
          <Button 
            onClick={() => setIsEditing(true)}
            variant="outline"
          >
            Chỉnh sửa
          </Button>
        )}
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        {/* Basic Information */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Full Name */}
          <div className="space-y-2">
            <label className="text-sm font-medium text-gray-700 flex items-center gap-2">
              <User className="w-4 h-4" />
              Họ và tên *
            </label>
            <Input
              {...register('fullName')}
              disabled={!isEditing}
              className={!isEditing ? 'bg-gray-50' : ''}
              placeholder="Nhập họ và tên"
            />
            {errors.fullName && (
              <p className="text-red-500 text-sm">{errors.fullName.message}</p>
            )}
          </div>

          {/* Phone Number */}
          <div className="space-y-2">
            <label className="text-sm font-medium text-gray-700 flex items-center gap-2">
              <Phone className="w-4 h-4" />
              Số điện thoại
            </label>
            <Input
              {...register('phoneNumber')}
              disabled={!isEditing}
              className={!isEditing ? 'bg-gray-50' : ''}
              placeholder="Nhập số điện thoại"
            />
          </div>
        </div>

        {/* Read-only Information */}
        <div className="border-t pt-6">
          <h3 className="text-md font-medium text-gray-900 mb-4">Thông tin chỉ đọc</h3>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Email - Read Only */}
            <div className="space-y-2">
              <label className="text-sm font-medium text-gray-700 flex items-center gap-2">
                <Mail className="w-4 h-4" />
                Email
              </label>
              <Input
                value={user?.email || ''}
                disabled
                className="bg-gray-50"
                placeholder="Email không thể thay đổi"
              />
              <p className="text-xs text-gray-500">Email không thể thay đổi</p>
            </div>

            {/* Role - Read Only */}
            <div className="space-y-2">
              <label className="text-sm font-medium text-gray-700">
                Vai trò
              </label>
              <div className="p-2 bg-gray-50 rounded-md">
                <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                  user?.role === 'ADMIN' ? 'bg-red-100 text-red-800' :
                  user?.role === 'ORGANIZER' ? 'bg-purple-100 text-purple-800' :
                  'bg-blue-100 text-blue-800'
                }`}>
                  {user?.role === 'USER' ? 'Người dùng' :
                   user?.role === 'ORGANIZER' ? 'Tổ chức sự kiện' :
                   'Quản trị viên'}
                </span>
              </div>
              <p className="text-xs text-gray-500">Vai trò không thể thay đổi</p>
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        {isEditing && (
          <div className="flex items-center justify-end gap-3 pt-4 border-t">
            <Button
              type="button"
              variant="outline"
              onClick={handleCancel}
              disabled={isSaving}
            >
              <X className="w-4 h-4 mr-2" />
              Hủy
            </Button>
            <Button
              type="submit"
              disabled={!isDirty || isSaving}
            >
              {isSaving ? (
                <div className="flex items-center gap-2">
                  <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                  Đang lưu...
                </div>
              ) : (
                <>
                  <Save className="w-4 h-4 mr-2" />
                  Lưu thay đổi
                </>
              )}
            </Button>
          </div>
        )}
      </form>

      {/* Profile Stats */}
      <div className="mt-8 pt-6 border-t">
        <h3 className="text-lg font-medium text-gray-900 mb-4">Thống kê tài khoản</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="bg-blue-50 rounded-lg p-4">
            <div className="text-2xl font-bold text-blue-600">12</div>
            <div className="text-sm text-blue-700">Sự kiện đã tham gia</div>
          </div>
          <div className="bg-green-50 rounded-lg p-4">
            <div className="text-2xl font-bold text-green-600">5</div>
            <div className="text-sm text-green-700">Sự kiện đã đánh giá</div>
          </div>
          <div className="bg-purple-50 rounded-lg p-4">
            <div className="text-2xl font-bold text-purple-600">3</div>
            <div className="text-sm text-purple-700">Sự kiện yêu thích</div>
          </div>
        </div>
      </div>
    </div>
  )
}
