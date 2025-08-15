"use client";

import { create } from "zustand";
import { persist } from "zustand/middleware";
import { setAccessToken, clearAccessToken } from "@/lib/auth/token";

export interface User {
  id: string;
  email: string;
  fullName: string;
  role: "USER" | "ORGANIZER" | "ADMIN";
  profilePictureUrl?: string;
}

interface AuthState {
  currentUser: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  isHydrated: boolean;
  setSession: (token: string, user: User, refreshToken?: string) => void;
  clearSession: () => void;
  updateUser: (updates: Partial<User>) => void;
  setLoading: (loading: boolean) => void;
  setHydrated: (hydrated: boolean) => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      currentUser: null,
      isAuthenticated: false,
      isLoading: false,
      isHydrated: false,
      setSession: (token: string, user: User, refreshToken?: string) => {
        setAccessToken(token);
        set({
          currentUser: user,
          isAuthenticated: true,
          isLoading: false,
        });
      },
      clearSession: () => {
        clearAccessToken();
        set({
          currentUser: null,
          isAuthenticated: false,
          isLoading: false,
        });
      },
      updateUser: (updates: Partial<User>) => {
        const currentUser = get().currentUser;
        if (currentUser) {
          set({
            currentUser: { ...currentUser, ...updates },
          });
        }
      },
      setLoading: (loading: boolean) => {
        set({ isLoading: loading });
      },
      setHydrated: (hydrated: boolean) => {
        set({ isHydrated: hydrated });
      },
    }),
    {
      name: "auth-storage",
      partialize: (state) => ({
        currentUser: state.currentUser,
        isAuthenticated: state.isAuthenticated,
      }),
      onRehydrateStorage: () => (state) => {
        if (state) {
          state.setHydrated(true);
        }
      },
    }
  )
);



