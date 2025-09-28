"use client";

import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import { User } from "./type";

interface AuthState {
  // State
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  token: string | null;
  theme: "dark" | "light";
  // Actions

  setUser: (user: User, token: string) => void;
  clearUser: () => void;
  // setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  clearError: () => void;
  logout: () => void;
  setAuthentctedUser: (user: User, token: string) => void;
  startLoading: () => void;
  stopLoading: () => void;
  setTheme: (theme: "dark" | "light") => void;
  // Computed values

  isAdmin: () => boolean;
  getUserName: () => string;
  getUserInitials: () => string;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      // Initial state
      user: null,
      token: null,
      theme: "light",
      isAuthenticated: false,
      isLoading: false,
      error: null,
      setUser: (user, token) =>
        set({
          user,
          token: token,
          theme: get().theme,
          isAuthenticated: true,
          error: null,
        }),
      setTheme: () =>
        set({
          theme: get().theme === "dark" ? "light" : "dark",
        }),
      clearUser: () =>
        set({
          user: null,
          token: null,
          isAuthenticated: false,
          error: null,
        }),

      // setLoading: (isLoading) => set({ isLoading }),

      setError: (error) => set({ error }),

      clearError: () => set({ error: null }),
          
      setAuthentctedUser: (user, token) =>
        set({
          user,
          token,
          isAuthenticated: true,
          isLoading: false,
          error: null,
        }),

      logout: () =>
        set({
          user: null,
          token: null,
          isAuthenticated: false,
          isLoading: false,
          error: null,
        }),
      startLoading() {
        set({ isLoading: true, error: null });
      },
      stopLoading() {
        set({ isLoading: false });
      },

      // Computed values
      isAdmin: () => get().user?.role === "ADMIN" || get().user?.role === "USER",

      getUserName: () => get().user?.name || "Guest",

      getUserInitials: () => {
        const user = get().user;
        if (!user) return "G";
        return user.name
          .split(" ")
          .map((word) => word[0])
          .join("")
          .toUpperCase()
          .slice(0, 2);
      },
    }),
    {
      name: "auth-storage",
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        user: state.user,
        token: state.token,
        theme: state.theme,
        isAuthenticated: state.isAuthenticated,
      }),
      // Add this to prevent hydration issues
      skipHydration: true,
    }
  )
);
