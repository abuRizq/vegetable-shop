import { create } from "zustand";
import { persist, createJSONStorage } from "zustand/middleware";
import { User } from "./type";

interface AuthState {
  // State
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;
  token :string | null;
  // Actions

  setUser: (user: User , token:string) => void;
  clearUser: () => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  clearError: () => void;
  logout: () => void;
  setAuthentctedUSer:(user:User , token:string) => void;
  startLoading:()=>void;
  stopLoading:()=>void;
  // Computed values

  isAdmin: () => boolean;
  getUserName: () => string;
  getUserInitials: () => string;
}

const InitialState={
// Initial state
user: null,
isAuthenticated: false,
isLoading: false,
error: null,
token: null,  // ← Add this line
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      // Initial state
      user: null,
      isAuthenticated: false,
      isLoading: false,
      error: null,
      token :null,

      // Actions
      setUser: (user, token) =>
        set({
          user,
          isAuthenticated: true,
          error: null,
          token:token,
        }),

      clearUser: () =>
        set({
          user: null,
          isAuthenticated: false,
          error: null,
        }),

      setLoading: (isLoading) => set({ isLoading }),

      setError: (error) => set({ error }),

      clearError: () => set({ error: null }),

      setAuthentctedUSer: (user, token) =>
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
          isAuthenticated: false,
          isLoading: false,
          error: null,
        }),
        startLoading() {
          set({ isLoading: true , error:null });  
        },
        stopLoading() {
set({ isLoading: false  });  

        },

      // Computed values
      isAdmin: () => get().user?.role === "ADMIN",

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
      storage: createJSONStorage(() => localStorage), // Uncomment this
      partialize: (state) => ({
        user: state.user,
        isAuthenticated: state.isAuthenticated,
      }),
      // Add this to prevent hydration issues
      skipHydration: true,
    }
  )
);
