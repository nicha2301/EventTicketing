"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { registerSchema, type RegisterInput } from "@/lib/validation/auth";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { useRegister } from "@/hooks/useAuth";
import { useAuthStore } from "@/store/auth";
import { useSearchParams, useRouter } from "next/navigation";
import { useEffect } from "react";
import Link from "next/link";

export default function RegisterPage() {
  const { isLoading, isAuthenticated } = useAuthStore();
  const searchParams = useSearchParams();
  const router = useRouter();
  const redirectPath = searchParams.get("redirect") || "/";
  const registerMutation = useRegister();

  useEffect(() => {
    if (isAuthenticated) {
      router.push(redirectPath);
    }
  }, [isAuthenticated, router, redirectPath]);

  const {
    register,
    handleSubmit,
    formState: { errors },
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
        router.push(redirectPath);
      }
    });
  };

  return (
    <div className="max-w-md mx-auto py-16 px-6">
      <h1 className="text-2xl font-semibold mb-6">Đăng ký</h1>
      
      {registerMutation.error && (
        <div className="mb-4 p-3 bg-red-100 border border-red-300 text-red-700 rounded">
          {registerMutation.error?.response?.data?.message || "Đăng ký thất bại"}
        </div>
      )}

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        <div>
          <Input
            placeholder="Họ và tên"
            {...register("fullName")}
            invalid={!!errors.fullName}
          />
          {errors.fullName?.message && (
            <p className="text-red-600 text-sm mt-1">{errors.fullName.message}</p>
          )}
        </div>
        
        <div>
          <Input
            placeholder="Email"
            type="email"
            {...register("email")}
            invalid={!!errors.email}
          />
          {errors.email?.message && (
            <p className="text-red-600 text-sm mt-1">{errors.email.message}</p>
          )}
        </div>
        
        <div>
          <Input
            placeholder="Mật khẩu"
            type="password"
            {...register("password")}
            invalid={!!errors.password}
          />
          {errors.password?.message && (
            <p className="text-red-600 text-sm mt-1">{errors.password.message}</p>
          )}
        </div>
        
        <div>
          <Input
            placeholder="Số điện thoại (tùy chọn)"
            {...register("phoneNumber")}
          />
        </div>
        
        <Button
          type="submit"
          disabled={isLoading || registerMutation.isPending}
          className="w-full"
        >
          {isLoading || registerMutation.isPending ? "Đang tạo tài khoản..." : "Tạo tài khoản"}
        </Button>
      </form>
      
      <div className="mt-6 text-center">
        <p className="text-gray-600">
          Đã có tài khoản?{" "}
          <Link href="/login" className="text-blue-600 hover:underline">
            Đăng nhập ngay
          </Link>
        </p>
      </div>
    </div>
  );
}
