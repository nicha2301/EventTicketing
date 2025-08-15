"use client";

import { useEffect } from "react";
import { useAuthStore } from "@/store/auth";
import { getAccessToken } from "@/lib/auth/token";

export function useAuthHydration() {
  const { setHydrated, setSession, clearSession, isHydrated } = useAuthStore();

  useEffect(() => {
    if (typeof window === 'undefined') return;

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
    
    setHydrated(true);
    
    return () => {
      window.removeEventListener('auth-clear', handleAuthClear);
    };
  }, [setHydrated, setSession, clearSession]);

  return isHydrated;
}
