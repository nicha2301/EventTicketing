"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/store/auth";

interface RequireRoleOptions {
  openModal?: boolean;
  allowAdmin?: boolean;
}

export function useRequireRole(requiredRole: string, options?: RequireRoleOptions) {
  const { currentUser } = useAuthStore();
  const router = useRouter();

  useEffect(() => {
    if (currentUser) {
      if (options?.allowAdmin && currentUser.role === 'ADMIN') {
        return;
      }
      
      if (currentUser.role !== requiredRole) {
        router.push("/");
      }
    }
  }, [currentUser, requiredRole, router, options?.allowAdmin]);

  if (options?.allowAdmin && currentUser?.role === 'ADMIN') {
    return true;
  }
  
  return currentUser?.role === requiredRole;
}





