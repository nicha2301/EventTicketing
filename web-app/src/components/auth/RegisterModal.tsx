"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { registerSchema, type RegisterInput } from "@/lib/validation/auth";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { useRegister } from "@/hooks/useAuth";
import { useAuthStore } from "@/store/auth";
import { useAuthModal } from "@/hooks/useAuthModal";
import Modal from "@/components/ui/modal";
import { Mail, Lock, User, Phone, Eye, EyeOff } from "lucide-react";
import { useState } from "react";

interface RegisterModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSwitchToLogin: () => void;
}

export default function RegisterModal({ isOpen, onClose, onSwitchToLogin }: RegisterModalProps) {
  const { isLoading } = useAuthStore();
  const { openEmailVerification } = useAuthModal();
  const registerMutation = useRegister();
  const [showPassword, setShowPassword] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
    getValues,
  } = useForm<RegisterInput>({
    resolver: zodResolver(registerSchema),
    mode: "onSubmit",
  });

  const onSubmit = (values: RegisterInput) => {
    const userData = {
      ...values,
      role: "USER" as const,
    };
    
    registerMutation.mutate(userData, {
      onSuccess: () => {
        onClose();
        reset();
        openEmailVerification(values.email);
      }
    });
  };

  const handleClose = () => {
    onClose();
    reset();
  };

  return (
    <Modal isOpen={isOpen} onClose={handleClose} className="max-w-lg">
      <div className="text-center mb-8">
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Tạo tài khoản mới</h2>
        <p className="text-gray-600">Tham gia EventTicketing ngay hôm nay</p>
      </div>

      {registerMutation.error && (
        <div className="mb-6 p-4 bg-red-50 border border-red-200 text-red-700 rounded-xl flex items-center gap-3">
          <div className="w-2 h-2 bg-red-500 rounded-full flex-shrink-0"></div>
          <span className="text-sm">
            {registerMutation.error?.response?.data?.message || "Đăng ký thất bại"}
          </span>
        </div>
      )}

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
        <div className="space-y-2">
          <label className="text-sm font-medium text-gray-700">Họ và tên</label>
          <div className="relative">
            <User className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            <Input
              placeholder="Nhập họ và tên"
              className="pl-11 h-12 border-gray-200 rounded-xl focus:border-blue-500 focus:ring-blue-500/20"
              {...register("fullName")}
              invalid={!!errors.fullName}
            />
          </div>
          {errors.fullName?.message && (
            <p className="text-red-500 text-sm mt-1 flex items-center gap-1">
              <span className="w-1 h-1 bg-red-500 rounded-full"></span>
              {errors.fullName.message}
            </p>
          )}
        </div>
        
        <div className="space-y-2">
          <label className="text-sm font-medium text-gray-700">Email</label>
          <div className="relative">
            <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            <Input
              placeholder="Nhập email của bạn"
              type="email"
              className="pl-11 h-12 border-gray-200 rounded-xl focus:border-blue-500 focus:ring-blue-500/20"
              {...register("email")}
              invalid={!!errors.email}
            />
          </div>
          {errors.email?.message && (
            <p className="text-red-500 text-sm mt-1 flex items-center gap-1">
              <span className="w-1 h-1 bg-red-500 rounded-full"></span>
              {errors.email.message}
            </p>
          )}
        </div>
        
        <div className="space-y-2">
          <label className="text-sm font-medium text-gray-700">Mật khẩu</label>
          <div className="relative">
            <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            <Input
              placeholder="Tạo mật khẩu mạnh"
              type={showPassword ? "text" : "password"}
              className="pl-11 pr-11 h-12 border-gray-200 rounded-xl focus:border-blue-500 focus:ring-blue-500/20"
              {...register("password")}
              invalid={!!errors.password}
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
            >
              {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
            </button>
          </div>
          {errors.password?.message && (
            <p className="text-red-500 text-sm mt-1 flex items-center gap-1">
              <span className="w-1 h-1 bg-red-500 rounded-full"></span>
              {errors.password.message}
            </p>
          )}
        </div>

        <div className="space-y-2">
          <label className="text-sm font-medium text-gray-700">Số điện thoại *</label>
          <div className="relative">
            <Phone className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
            <Input
              placeholder="Nhập số điện thoại"
              className="pl-11 h-12 border-gray-200 rounded-xl focus:border-blue-500 focus:ring-blue-500/20"
              {...register("phoneNumber")}
              invalid={!!errors.phoneNumber}
            />
          </div>
          {errors.phoneNumber?.message && (
            <p className="text-red-500 text-sm mt-1 flex items-center gap-1">
              <span className="w-1 h-1 bg-red-500 rounded-full"></span>
              {errors.phoneNumber.message}
            </p>
          )}
        </div>

        <div className="flex items-start gap-3 text-sm">
          <input type="checkbox" className="mt-0.5 rounded border-gray-300" required />
          <span className="text-gray-600">
            Tôi đồng ý với{" "}
            <button type="button" className="text-blue-600 hover:text-blue-800 underline">
              Điều khoản dịch vụ
            </button>{" "}
            và{" "}
            <button type="button" className="text-blue-600 hover:text-blue-800 underline">
              Chính sách bảo mật
            </button>
          </span>
        </div>
        
        <Button
          type="submit"
          disabled={isLoading || registerMutation.isPending}
          className="w-full h-12 bg-gradient-to-r from-green-600 to-green-700 hover:from-green-700 hover:to-green-800 text-white font-medium rounded-xl shadow-lg shadow-green-500/25 transition-all duration-200"
        >
          {isLoading || registerMutation.isPending ? (
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 border-2 border-white/20 border-t-white rounded-full animate-spin"></div>
              Đang tạo tài khoản...
            </div>
          ) : (
            "Tạo tài khoản"
          )}
        </Button>
      </form>

      <div className="mt-8 pt-6 border-t border-gray-100 text-center">
        <p className="text-gray-600">
          Đã có tài khoản?{" "}
          <button
            onClick={onSwitchToLogin}
            className="text-blue-600 hover:text-blue-800 font-medium"
          >
            Đăng nhập ngay
          </button>
        </p>
      </div>
    </Modal>
  );
}
