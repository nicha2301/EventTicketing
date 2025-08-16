"use client";

import { useEffect, useState } from "react";
import { useAuthStore } from "@/store/auth";
import { getAccessToken } from "@/lib/auth/token";

export function useAuthHydration() {
  const { setHydrated, setSession, clearSession, isHydrated } = useAuthStore();
  const [isClient, setIsClient] = useState(false);

  useEffect(() => {
    if (typeof window === 'undefined') return;

    setIsClient(true);

    const handleAuthClear = () => {
      clearSession();
    };
    
    window.addEventListener('auth-clear', handleAuthClear);

    const token = getAccessToken();
    
    if (token) {
      const persistedState = useAuthStore.getState();
      if (persistedState.currentUser) {
        setSession(token, persistedState.currentUser);
      }
    }
    
    const timer = setTimeout(() => {
      setHydrated(true);
    }, 50);
    
    return () => {
      clearTimeout(timer);
      window.removeEventListener('auth-clear', handleAuthClear);
    };
  }, [setHydrated, setSession, clearSession]);

  return isClient && isHydrated;
}
