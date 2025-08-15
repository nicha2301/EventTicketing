"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { loginSchema, type LoginInput } from "@/lib/validation/auth";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { useLogin } from "@/hooks/useAuth";
import { useAuthStore } from "@/store/auth";
import Modal from "@/components/ui/modal";
import { Mail, Lock, Eye, EyeOff } from "lucide-react";
import { useState } from "react";

interface LoginModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSwitchToRegister: () => void;
}

export default function LoginModal({ isOpen, onClose, onSwitchToRegister }: LoginModalProps) {
  const { isLoading } = useAuthStore();
  const loginMutation = useLogin();
  const [showPassword, setShowPassword] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<LoginInput>({
    resolver: zodResolver(loginSchema),
    mode: "onSubmit",
  });

  const onSubmit = (values: LoginInput) => {
    loginMutation.mutate(values, {
      onSuccess: () => {
        onClose();
        reset();
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
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Chào mừng trở lại</h2>
        <p className="text-gray-600">Đăng nhập để tiếp tục với EventTicketing</p>
      </div>

      {loginMutation.error && (
        <div className="mb-6 p-4 bg-red-50 border border-red-200 text-red-700 rounded-xl flex items-center gap-3">
          <div className="w-2 h-2 bg-red-500 rounded-full flex-shrink-0"></div>
          <span className="text-sm">
            {loginMutation.error?.response?.data?.message || "Đăng nhập thất bại"}
          </span>
        </div>
      )}

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
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
              placeholder="Nhập mật khẩu"
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

        <div className="flex items-center justify-between text-sm">
          <label className="flex items-center gap-2 cursor-pointer">
            <input type="checkbox" className="rounded border-gray-300" />
            <span className="text-gray-600">Ghi nhớ đăng nhập</span>
          </label>
          <button type="button" className="text-blue-600 hover:text-blue-800 font-medium">
            Quên mật khẩu?
          </button>
        </div>
        
        <Button
          type="submit"
          disabled={isLoading || loginMutation.isPending}
          className="w-full h-12 bg-gradient-to-r from-blue-600 to-blue-700 hover:from-blue-700 hover:to-blue-800 text-white font-medium rounded-xl shadow-lg shadow-blue-500/25 transition-all duration-200"
        >
          {isLoading || loginMutation.isPending ? (
            <div className="flex items-center gap-2">
              <div className="w-4 h-4 border-2 border-white/20 border-t-white rounded-full animate-spin"></div>
              Đang đăng nhập...
            </div>
          ) : (
            "Đăng nhập"
          )}
        </Button>
      </form>

      <div className="mt-8 pt-6 border-t border-gray-100 text-center">
        <p className="text-gray-600">
          Chưa có tài khoản?{" "}
          <button
            onClick={onSwitchToRegister}
            className="text-blue-600 hover:text-blue-800 font-medium"
          >
            Đăng ký ngay
          </button>
        </p>
      </div>
    </Modal>
  );
}
