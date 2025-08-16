"use client";

import { useEffect } from "react";
import { useAuthStore } from "@/store/auth";
import { useAuthHydration } from "./useAuthHydration";

export function useInitialization() {
  const isHydrated = useAuthHydration();
  const { isAuthenticated } = useAuthStore();

  useEffect(() => {
    if (isHydrated) {
      const timer = setTimeout(() => {
        window.dispatchEvent(new CustomEvent('auth-ready'));
      }, 10);
      
      return () => clearTimeout(timer);
    }
  }, [isHydrated]);

  return {
    isReady: isHydrated,
    isAuthenticated: isAuthenticated && isHydrated,
  };
}
