"use client";
import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store/auth";
import { useAuthModal } from "@/hooks/useAuthModal";

interface RequireRoleOptions {
  openModal?: boolean;
  allowAdmin?: boolean;
}

export function useRequireRole(requiredRole: string, options?: RequireRoleOptions) {
  const router = useRouter();
  const { currentUser, isHydrated } = useAuthStore();
  const { openLogin } = useAuthModal();

  useEffect(() => {
    if (!isHydrated) return;
    const role = currentUser?.role;

    if (!role) {
      if (options?.openModal) {
        try {
          if (typeof window !== 'undefined') {
            const returnTo = window.location.pathname + window.location.search;
            sessionStorage.setItem('returnTo', returnTo);
          }
        } catch {}
        openLogin();
        return;
      }
      router.replace("/login");
      return;
    }

    if (role !== requiredRole) {
      if (options?.allowAdmin && role === "ADMIN") return;
      router.replace("/");
    }
  }, [currentUser, isHydrated, requiredRole, router, options?.openModal, options?.allowAdmin, openLogin]);
}


