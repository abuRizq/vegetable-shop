/* eslint-disable @typescript-eslint/no-explicit-any */
import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import { User } from "./type";

interface AuthState {
  // State
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  // Actions
  setUser: (user: User) => void;
  setToken: (token: string) => void;
  login: (user: User, token: string) => void;
  logout: () => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  clearError: () => void;

  // Computed values (getters)
  isAdmin: () => boolean;
  isUser: () => boolean;
  getUserName: () => string;
  //   getInitials: () => string;
}
const useAuthStore = create<AuthState>()(
    
  persist(
    (set, get) => ({
      // Initial state
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,
      
      setUser: (user) => set({ user }),
      
      setToken: (token) => set({ token }),
      
      login: (user, token) => set({ 
        user, 
        token, 
        isAuthenticated: true,
        isLoading: false,
        error: null 
      }),
      logout: () => set({ 
        user: null, 
        token: null, 
        isAuthenticated: false,
        error: null 
      }),
      
      setLoading: (isLoading) => set({ isLoading }),
      
      setError: (error) => set({ error }),
      
      clearError: () => set({ error: null }),
      
      // Computed values (business logic)
      isAdmin: () => get().user?.role === 'ADMIN',
      
      isUser: () => get().user?.role === 'USER',
      
      getUserName: () => get().user?.name || 'Guest',
      
      getInitials: () => {
        const user = get().user
        if (!user) return 'G'
        return user.name
          .split(' ')
          .map(word => word[0])
          .join('')
          .toUpperCase()
          .slice(0, 2)
      }
    }),
    {
      name: "auth-storage",
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({ 
        user: state.user,
        token: state.token,
        isAuthenticated: state.isAuthenticated
      })
    }
  )
);
