"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Eye, EyeOff, Shield, Lock } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { passwordChangeSchema, type PasswordChangeFormData } from "@/lib/validation/profile";
import { useChangePassword } from "@/hooks/useUserProfile";

export default function PasswordChangeForm() {
  const [showPasswords, setShowPasswords] = useState({
    current: false,
    new: false,
    confirm: false,
  });

  const changePasswordMutation = useChangePassword();

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    watch,
  } = useForm<PasswordChangeFormData>({
    resolver: zodResolver(passwordChangeSchema),
    defaultValues: {
      currentPassword: "",
      newPassword: "",
      confirmPassword: "",
    },
  });

  const newPassword = watch("newPassword");

  const onSubmit = async (data: PasswordChangeFormData) => {
    await changePasswordMutation.mutateAsync(data);
    reset(); 
  };

  const togglePasswordVisibility = (field: keyof typeof showPasswords) => {
    setShowPasswords(prev => ({
      ...prev,
      [field]: !prev[field]
    }));
  };

  const getPasswordStrength = (password: string) => {
    if (!password) return { strength: 0, label: "", color: "" };
    
    let strength = 0;
    if (password.length >= 8) strength++;
    if (/[a-z]/.test(password)) strength++;
    if (/[A-Z]/.test(password)) strength++;
    if (/\d/.test(password)) strength++;
    if (/[^a-zA-Z0-9]/.test(password)) strength++;

    const levels = [
      { label: "Rất yếu", color: "bg-red-500" },
      { label: "Yếu", color: "bg-red-400" },
      { label: "Trung bình", color: "bg-yellow-500" },
      { label: "Khá", color: "bg-blue-500" },
      { label: "Mạnh", color: "bg-green-500" },
    ];

    return { strength, ...levels[Math.min(strength, 4)] };
  };

  const passwordStrength = getPasswordStrength(newPassword);

  return (
    <div className="max-w-md">
      {/* Security info banner */}
      <div className="bg-blue-50 border border-blue-200 rounded-xl p-4 mb-6">
        <div className="flex items-center gap-3 mb-2">
          <Shield className="h-5 w-5 text-blue-600" />
          <span className="font-medium text-blue-900">Bảo mật tài khoản</span>
        </div>
        <p className="text-sm text-blue-700 leading-relaxed">
          Sử dụng mật khẩu mạnh với ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số.
        </p>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
        {/* Current Password */}
        <div className="space-y-2">
          <label className="text-sm font-medium text-gray-700">
            Mật khẩu hiện tại <span className="text-red-500">*</span>
          </label>
          <div className="relative">
            <div className="absolute inset-y-0 left-3 flex items-center">
              <Lock className="h-4 w-4 text-gray-400" />
            </div>
            <Input
              {...register("currentPassword")}
              type={showPasswords.current ? "text" : "password"}
              placeholder="Nhập mật khẩu hiện tại"
              className={`pl-10 pr-10 ${errors.currentPassword ? "border-red-300 focus:border-red-500" : ""}`}
            />
            <button
              type="button"
              onClick={() => togglePasswordVisibility("current")}
              className="absolute inset-y-0 right-3 flex items-center text-gray-400 hover:text-gray-600"
            >
              {showPasswords.current ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
            </button>
          </div>
          {errors.currentPassword && (
            <p className="text-sm text-red-600">{errors.currentPassword.message}</p>
          )}
        </div>

        {/* New Password */}
        <div className="space-y-2">
          <label className="text-sm font-medium text-gray-700">
            Mật khẩu mới <span className="text-red-500">*</span>
          </label>
          <div className="relative">
            <div className="absolute inset-y-0 left-3 flex items-center">
              <Lock className="h-4 w-4 text-gray-400" />
            </div>
            <Input
              {...register("newPassword")}
              type={showPasswords.new ? "text" : "password"}
              placeholder="Nhập mật khẩu mới"
              className={`pl-10 pr-10 ${errors.newPassword ? "border-red-300 focus:border-red-500" : ""}`}
            />
            <button
              type="button"
              onClick={() => togglePasswordVisibility("new")}
              className="absolute inset-y-0 right-3 flex items-center text-gray-400 hover:text-gray-600"
            >
              {showPasswords.new ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
            </button>
          </div>
          
          {/* Password strength indicator */}
          {newPassword && (
            <div className="space-y-2">
              <div className="flex items-center gap-2">
                <div className="flex-1 bg-gray-200 rounded-full h-2">
                  <div 
                    className={`h-2 rounded-full transition-all duration-300 ${passwordStrength.color}`}
                    style={{ width: `${(passwordStrength.strength / 5) * 100}%` }}
                  />
                </div>
                <span className="text-xs font-medium text-gray-600">
                  {passwordStrength.label}
                </span>
              </div>
            </div>
          )}
          
          {errors.newPassword && (
            <p className="text-sm text-red-600">{errors.newPassword.message}</p>
          )}
        </div>

        {/* Confirm Password */}
        <div className="space-y-2">
          <label className="text-sm font-medium text-gray-700">
            Xác nhận mật khẩu <span className="text-red-500">*</span>
          </label>
          <div className="relative">
            <div className="absolute inset-y-0 left-3 flex items-center">
              <Lock className="h-4 w-4 text-gray-400" />
            </div>
            <Input
              {...register("confirmPassword")}
              type={showPasswords.confirm ? "text" : "password"}
              placeholder="Nhập lại mật khẩu mới"
              className={`pl-10 pr-10 ${errors.confirmPassword ? "border-red-300 focus:border-red-500" : ""}`}
            />
            <button
              type="button"
              onClick={() => togglePasswordVisibility("confirm")}
              className="absolute inset-y-0 right-3 flex items-center text-gray-400 hover:text-gray-600"
            >
              {showPasswords.confirm ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
            </button>
          </div>
          {errors.confirmPassword && (
            <p className="text-sm text-red-600">{errors.confirmPassword.message}</p>
          )}
        </div>

        {/* Password requirements */}
        <div className="bg-gray-50 rounded-lg p-4 space-y-2">
          <h4 className="text-sm font-medium text-gray-700">Yêu cầu mật khẩu:</h4>
          <ul className="text-xs text-gray-600 space-y-1">
            <li className="flex items-center gap-2">
              <div className={`w-1.5 h-1.5 rounded-full ${newPassword?.length >= 8 ? 'bg-green-500' : 'bg-gray-300'}`} />
              Ít nhất 8 ký tự
            </li>
            <li className="flex items-center gap-2">
              <div className={`w-1.5 h-1.5 rounded-full ${/[a-z]/.test(newPassword) ? 'bg-green-500' : 'bg-gray-300'}`} />
              Chứa chữ thường (a-z)
            </li>
            <li className="flex items-center gap-2">
              <div className={`w-1.5 h-1.5 rounded-full ${/[A-Z]/.test(newPassword) ? 'bg-green-500' : 'bg-gray-300'}`} />
              Chứa chữ hoa (A-Z)
            </li>
            <li className="flex items-center gap-2">
              <div className={`w-1.5 h-1.5 rounded-full ${/\d/.test(newPassword) ? 'bg-green-500' : 'bg-gray-300'}`} />
              Chứa số (0-9)
            </li>
          </ul>
        </div>

        {/* Submit button */}
        <div className="pt-4">
          <Button
            type="submit"
            disabled={changePasswordMutation.isPending}
            className="w-full py-2.5 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200"
          >
            {changePasswordMutation.isPending ? (
              <div className="flex items-center gap-2">
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                <span>Đang đổi mật khẩu...</span>
              </div>
            ) : (
              <div className="flex items-center gap-2">
                <Shield className="w-4 h-4" />
                <span>Đổi mật khẩu</span>
              </div>
            )}
          </Button>
        </div>
      </form>
    </div>
  );
}
