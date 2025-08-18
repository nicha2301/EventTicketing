"use client";
import { Suspense, useEffect, useMemo } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import { useAuthStore } from "@/store/auth";

function GateContent() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const { currentUser, isHydrated } = useAuthStore();

  const returnTo = useMemo(() => searchParams.get("returnTo") || "/", [searchParams]);

  useEffect(() => {
    if (typeof window !== 'undefined') {
      try {
        sessionStorage.setItem('returnTo', returnTo);
      } catch {}
    }
  }, [returnTo]);

  useEffect(() => {
    if (!isHydrated) return;
    if (currentUser?.id) {
      router.replace(returnTo);
    }
  }, [currentUser, isHydrated, router, returnTo]);

  return (
    <section className="py-20">
      <div className="container-page max-w-md text-center space-y-4">
        <h1 className="text-2xl font-semibold">Vui lòng đăng nhập</h1>
        <p className="text-slate-600">Bạn cần đăng nhập để tiếp tục.</p>
        <p className="text-xs text-slate-400">Sử dụng nút Đăng nhập ở thanh điều hướng.</p>
        <button
          onClick={() => router.push("/")}
          className="mt-2 rounded-md border px-4 py-2 text-gray-700"
        >Về trang chủ</button>
      </div>
    </section>
  );
}

export default function AuthGatePage() {
  return (
    <Suspense fallback={<section className="py-20"><div className="container-page">Đang tải...</div></section>}>
      <GateContent />
    </Suspense>
  );
}


