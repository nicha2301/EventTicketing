'use client'

import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { 
  Shield, 
  Lock, 
  Eye, 
  EyeOff, 
  Key, 
  Smartphone, 
  Mail, 
  AlertTriangle,
  CheckCircle,
  Clock,
  MapPin
} from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { toast } from 'sonner'

const passwordChangeSchema = z.object({
  currentPassword: z.string().min(1, 'Mật khẩu hiện tại là bắt buộc'),
  newPassword: z
    .string()
    .min(8, 'Mật khẩu mới phải có ít nhất 8 ký tự')
    .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/, 'Mật khẩu phải chứa chữ hoa, chữ thường và số'),
  confirmPassword: z.string().min(1, 'Xác nhận mật khẩu là bắt buộc'),
}).refine((data) => data.newPassword === data.confirmPassword, {
  message: 'Mật khẩu xác nhận không khớp',
  path: ['confirmPassword'],
})

type PasswordChangeInput = z.infer<typeof passwordChangeSchema>

const loginSessions = [
  {
    id: '1',
    device: 'Chrome on Windows',
    location: 'Ho Chi Minh City, Vietnam',
    ip: '123.456.789.012',
    lastActive: '2 phút trước',
    isCurrent: true,
  },
  {
    id: '2',
    device: 'Safari on iPhone',
    location: 'Ho Chi Minh City, Vietnam',
    ip: '123.456.789.013',
    lastActive: '2 giờ trước',
    isCurrent: false,
  },
  {
    id: '3',
    device: 'Chrome on Android',
    location: 'Hanoi, Vietnam',
    ip: '123.456.789.014',
    lastActive: '1 ngày trước',
    isCurrent: false,
  },
]

export function AccountSecurity() {
  const [showPasswords, setShowPasswords] = useState({
    current: false,
    new: false,
    confirm: false,
  })
  const [isChangingPassword, setIsChangingPassword] = useState(false)
  const [is2FAEnabled, setIs2FAEnabled] = useState(false)
  const [isToggling2FA, setIsToggling2FA] = useState(false)
  const [terminatingSessions, setTerminatingSessions] = useState<Set<string>>(new Set())

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    watch,
  } = useForm<PasswordChangeInput>({
    resolver: zodResolver(passwordChangeSchema),
  })

  const newPassword = watch('newPassword')

  // Password strength calculator
  const getPasswordStrength = (password: string) => {
    if (!password) return { score: 0, label: '', color: '' }
    
    let score = 0
    if (password.length >= 8) score++
    if (/[a-z]/.test(password)) score++
    if (/[A-Z]/.test(password)) score++
    if (/\d/.test(password)) score++
    if (/[^A-Za-z0-9]/.test(password)) score++

    const levels = [
      { score: 0, label: '', color: '' },
      { score: 1, label: 'Rất yếu', color: 'bg-red-500' },
      { score: 2, label: 'Yếu', color: 'bg-orange-500' },
      { score: 3, label: 'Trung bình', color: 'bg-yellow-500' },
      { score: 4, label: 'Mạnh', color: 'bg-blue-500' },
      { score: 5, label: 'Rất mạnh', color: 'bg-green-500' },
    ]

    return levels[score] || levels[0]
  }

  const passwordStrength = getPasswordStrength(newPassword || '')

  const onSubmitPasswordChange = async (data: PasswordChangeInput) => {
    setIsChangingPassword(true)
    try {
      // TODO: Implement password change API call
      await new Promise(resolve => setTimeout(resolve, 2000))
      toast.success('Mật khẩu đã được thay đổi thành công!')
      reset()
    } catch (error) {
      toast.error('Có lỗi xảy ra khi thay đổi mật khẩu')
    } finally {
      setIsChangingPassword(false)
    }
  }

  const handleTerminateSession = async (sessionId: string) => {
    setTerminatingSessions(prev => new Set(prev).add(sessionId))
    try {
      // TODO: Implement session termination
      await new Promise(resolve => setTimeout(resolve, 1000))
      toast.success('Phiên đăng nhập đã được chấm dứt')
    } catch (error) {
      toast.error('Có lỗi xảy ra')
    } finally {
      setTerminatingSessions(prev => {
        const newSet = new Set(prev)
        newSet.delete(sessionId)
        return newSet
      })
    }
  }

  const toggle2FA = async () => {
    setIsToggling2FA(true)
    try {
      // TODO: Implement 2FA toggle
      await new Promise(resolve => setTimeout(resolve, 1500))
      setIs2FAEnabled(!is2FAEnabled)
      toast.success(is2FAEnabled ? 'Đã tắt xác thực 2 lớp' : 'Đã bật xác thực 2 lớp')
    } catch (error) {
      toast.error('Có lỗi xảy ra')
    } finally {
      setIsToggling2FA(false)
    }
  }

  return (
    <div className="p-6 space-y-8">
      {/* Header */}
      <div>
        <h2 className="text-xl font-semibold text-gray-900 flex items-center gap-2">
          <Shield className="w-5 h-5" />
          Bảo mật tài khoản
        </h2>
        <p className="text-gray-600 text-sm mt-1">
          Quản lý mật khẩu và cài đặt bảo mật cho tài khoản của bạn
        </p>
      </div>

      {/* Security Overview */}
      <div className="bg-green-50 border border-green-200 rounded-lg p-4">
        <div className="flex items-center gap-2 mb-2">
          <CheckCircle className="w-5 h-5 text-green-600" />
          <span className="font-medium text-green-800">Tài khoản được bảo vệ tốt</span>
        </div>
        <p className="text-green-700 text-sm">
          Tài khoản của bạn có mức độ bảo mật cao với mật khẩu mạnh và email được xác thực.
        </p>
      </div>

      {/* Password Change Section */}
      <div className="border border-gray-200 rounded-lg p-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4 flex items-center gap-2">
          <Lock className="w-5 h-5" />
          Thay đổi mật khẩu
        </h3>
        
        <form onSubmit={handleSubmit(onSubmitPasswordChange)} className="space-y-4">
          {/* Current Password */}
          <div className="space-y-2">
            <label className="text-sm font-medium text-gray-700">
              Mật khẩu hiện tại *
            </label>
            <div className="relative">
              <Input
                {...register('currentPassword')}
                type={showPasswords.current ? 'text' : 'password'}
                placeholder="Nhập mật khẩu hiện tại"
              />
              <button
                type="button"
                onClick={() => setShowPasswords(prev => ({ ...prev, current: !prev.current }))}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                {showPasswords.current ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
            {errors.currentPassword && (
              <p className="text-red-500 text-sm">{errors.currentPassword.message}</p>
            )}
          </div>

          {/* New Password */}
          <div className="space-y-2">
            <label className="text-sm font-medium text-gray-700">
              Mật khẩu mới *
            </label>
            <div className="relative">
              <Input
                {...register('newPassword')}
                type={showPasswords.new ? 'text' : 'password'}
                placeholder="Nhập mật khẩu mới"
              />
              <button
                type="button"
                onClick={() => setShowPasswords(prev => ({ ...prev, new: !prev.new }))}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                {showPasswords.new ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
            
            {/* Password Strength */}
            {newPassword && (
              <div className="space-y-2">
                <div className="flex items-center gap-2">
                  <div className="flex-1 bg-gray-200 rounded-full h-2">
                    <div 
                      className={`h-2 rounded-full transition-all ${passwordStrength.color}`}
                      style={{ width: `${(passwordStrength.score / 5) * 100}%` }}
                    />
                  </div>
                  <span className="text-xs font-medium">{passwordStrength.label}</span>
                </div>
              </div>
            )}
            
            {errors.newPassword && (
              <p className="text-red-500 text-sm">{errors.newPassword.message}</p>
            )}
          </div>

          {/* Confirm Password */}
          <div className="space-y-2">
            <label className="text-sm font-medium text-gray-700">
              Xác nhận mật khẩu mới *
            </label>
            <div className="relative">
              <Input
                {...register('confirmPassword')}
                type={showPasswords.confirm ? 'text' : 'password'}
                placeholder="Nhập lại mật khẩu mới"
              />
              <button
                type="button"
                onClick={() => setShowPasswords(prev => ({ ...prev, confirm: !prev.confirm }))}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
              >
                {showPasswords.confirm ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
              </button>
            </div>
            {errors.confirmPassword && (
              <p className="text-red-500 text-sm">{errors.confirmPassword.message}</p>
            )}
          </div>

          <Button
            type="submit"
            disabled={isChangingPassword}
            className="w-full sm:w-auto"
          >
            {isChangingPassword ? (
              <div className="flex items-center gap-2">
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                Đang cập nhật...
              </div>
            ) : (
              'Thay đổi mật khẩu'
            )}
          </Button>
        </form>
      </div>

      {/* Two-Factor Authentication */}
      <div className="border border-gray-200 rounded-lg p-6">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h3 className="text-lg font-medium text-gray-900 flex items-center gap-2">
              <Smartphone className="w-5 h-5" />
              Xác thực hai lớp (2FA)
            </h3>
            <p className="text-gray-600 text-sm mt-1">
              Tăng cường bảo mật bằng cách yêu cầu mã xác thực từ điện thoại
            </p>
          </div>
          <Button
            onClick={toggle2FA}
            variant={is2FAEnabled ? 'destructive' : 'default'}
            disabled={isToggling2FA}
          >
            {isToggling2FA ? (
              <div className="flex items-center gap-2">
                <div className="w-4 h-4 border-2 border-current border-t-transparent rounded-full animate-spin"></div>
                {is2FAEnabled ? 'Đang tắt...' : 'Đang bật...'}
              </div>
            ) : (
              is2FAEnabled ? 'Tắt 2FA' : 'Bật 2FA'
            )}
          </Button>
        </div>
        
        {is2FAEnabled ? (
          <div className="bg-green-50 border border-green-200 rounded-lg p-4">
            <div className="flex items-center gap-2">
              <CheckCircle className="w-5 h-5 text-green-600" />
              <span className="font-medium text-green-800">Xác thực 2 lớp đang hoạt động</span>
            </div>
          </div>
        ) : (
          <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
            <div className="flex items-center gap-2">
              <AlertTriangle className="w-5 h-5 text-yellow-600" />
              <span className="font-medium text-yellow-800">Bật xác thực 2 lớp để tăng cường bảo mật</span>
            </div>
          </div>
        )}
      </div>

      {/* Active Sessions */}
      <div className="border border-gray-200 rounded-lg p-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4 flex items-center gap-2">
          <Clock className="w-5 h-5" />
          Phiên đăng nhập đang hoạt động
        </h3>
        
        <div className="space-y-4">
          {loginSessions.map((session) => (
            <div
              key={session.id}
              className="flex items-center justify-between p-4 border border-gray-200 rounded-lg"
            >
              <div className="flex items-center gap-4">
                <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center">
                  <MapPin className="w-5 h-5 text-blue-600" />
                </div>
                <div>
                  <div className="font-medium text-gray-900 flex items-center gap-2">
                    {session.device}
                    {session.isCurrent && (
                      <span className="text-xs bg-green-100 text-green-800 px-2 py-1 rounded-full">
                        Hiện tại
                      </span>
                    )}
                  </div>
                  <div className="text-sm text-gray-600">
                    {session.location} • {session.ip}
                  </div>
                  <div className="text-xs text-gray-500">
                    Hoạt động lần cuối: {session.lastActive}
                  </div>
                </div>
              </div>
              
              {!session.isCurrent && (
                <Button
                  variant="outline"
                  size="sm"
                  onClick={() => handleTerminateSession(session.id)}
                  disabled={terminatingSessions.has(session.id)}
                >
                  {terminatingSessions.has(session.id) ? (
                    <div className="flex items-center gap-2">
                      <div className="w-3 h-3 border-2 border-current border-t-transparent rounded-full animate-spin"></div>
                      Đang chấm dứt...
                    </div>
                  ) : (
                    'Chấm dứt'
                  )}
                </Button>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Email Verification */}
      <div className="border border-gray-200 rounded-lg p-6">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-lg font-medium text-gray-900 flex items-center gap-2">
              <Mail className="w-5 h-5" />
              Xác thực email
            </h3>
            <p className="text-gray-600 text-sm mt-1">
              Email của bạn đã được xác thực thành công
            </p>
          </div>
          <div className="flex items-center gap-2 text-green-600">
            <CheckCircle className="w-5 h-5" />
            <span className="font-medium">Đã xác thực</span>
          </div>
        </div>
      </div>
    </div>
  )
}
