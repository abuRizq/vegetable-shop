/**
 * React Query Auth Hooks - Single Source of Truth for Authentication
 * This file replaces Zustand store for auth state management.
 * All auth state is now managed by React Query's cache.
 */

import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { User } from "../model/type";

// ============================================================================
// QUERY KEYS - Centralized key management
// ============================================================================

export const userQueryKeys = {
  all: ["user"] as const,
  me: () => [...userQueryKeys.all, "me"] as const,
} as const;

// ============================================================================
// API FUNCTIONS - Pure functions for API calls
// ============================================================================


async function fetchUserProfile(): Promise<User | null> {
  try {
    const response = await fetch("/api/auth/me", {
      method: "GET",
      credentials: "include", // Send HTTP-only cookie
    });

    // 401 = Not authenticated (normal case, not an error)
    if (response.status === 401) {
      return null;
    }
    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(errorData.error || "Failed to fetch user profile");
    }
    const data = await response.json();
    return data.user || data.data?.user || null;
  } catch (error) {
    console.error("User profile fetch error:", error);
    // Network errors should return null instead of throwing
    if (error instanceof TypeError) {
      return null;
    }
    throw error;
  }
}


async function logoutUser(): Promise<void> {
  const response = await fetch("/api/auth/logout", {
    method: "POST",
    credentials: "include",
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.error || "Logout failed");
  }
}

// ============================================================================
// MAIN HOOKS - Public API
// ============================================================================

/**
 * Main hook to get current user
 * This is the primary hook that replaces useAuthStore()
 */

export const useUser = () => {
  return useQuery({
    queryKey: userQueryKeys.me(),
    queryFn: fetchUserProfile,
    staleTime: 5 * 60 * 1000, // 5 minutes - data stays fresh
    gcTime: 10 * 60 * 1000, // 10 minutes - cache lifetime
    retry: (failureCount, error: Error) => {
      // Don't retry auth failures
      if (
        error?.message?.includes("Authentication expired") ||
        error?.message?.includes("No authentication token")
      ) {
        return false;
      }
      return failureCount < 2;
    },
    refetchOnWindowFocus: true,
    refetchOnReconnect: true,
    refetchInterval: 15 * 60 * 1000, // Refresh every 15 minutes
  });
};

// ============================================================================
// COMPUTED VALUE HOOKS - Derived state from user query
// ============================================================================

/**
 * Check if user is authenticated
 * @example
 * const isAuthenticated = useIsAuthenticated();
 * if (!isAuthenticated) return <LoginPrompt />;
 */

export const useIsAuthenticated = (): boolean => {
  const { data: user, isLoading } = useUser();
  // While loading, we don't know auth status yet
  // Return false to be safe (components can check isLoading separately)
  if (isLoading) return false;
  return !!user;
};

/**
 * Check if user is admin
 * @example
 * const isAdmin = useIsAdmin();
 * if (isAdmin) return <AdminPanel />;
 */
export const useIsAdmin = (): boolean => {
  const { data: user } = useUser();
  return user?.role === "ADMIN";
};

/**
 * Get user's display name
 * @example
 * const userName = useUserName();
 * return <div>Welcome, {userName}</div>;
 */
export const useUserName = (): string => {
  const { data: user } = useUser();
  return user?.name || "Guest";
};

/**
 * Get user's initials for avatar
 * @example
 * const initials = useUserInitials();
 * return <Avatar>{initials}</Avatar>;
 */
export const useUserInitials = (): string => {
  const { data: user } = useUser();
  if (!user) return "G";
  
  return user.name
    .split(" ")
    .map((word) => word[0])
    .join("")
    .toUpperCase()
    .slice(0, 2);
};

// ============================================================================
// MUTATION HOOKS - Actions that modify auth state
// ============================================================================

/**
 * Logout mutation
 * Clears user from cache and calls logout endpoint
 * 
 * @example
 * const logout = useLogout();
 * <button onClick={() => logout.mutate()}>Logout</button>
 */
export const useLogout = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: logoutUser,
    onSuccess: () => {
      // Clear user from cache
      queryClient.setQueryData(userQueryKeys.me(), null);
      // Invalidate all user-related queries
      queryClient.invalidateQueries({ queryKey: userQueryKeys.all });
    },
    onError: (error) => {
      console.error("Logout error:", error);
      // Even if logout fails, clear local cache
      queryClient.setQueryData(userQueryKeys.me(), null);
    },
  });
};

// ============================================================================
// UTILITY HOOKS - Helper functions
// ============================================================================

/**
 * Manually refetch user profile
 * Useful after login or when you need to force refresh
 * 
 * @example
 * const refetchUser = useRefetchUser();
 * await refetchUser();
 */
export const useRefetchUser = () => {
  const queryClient = useQueryClient();
  return () => {
    return queryClient.invalidateQueries({ queryKey: userQueryKeys.me() });
  };
};

/**
 * Get loading state for auth
 * Useful for showing loading spinners during initial auth check
 * 
 * @example
 * const isAuthLoading = useAuthLoading();
 * if (isAuthLoading) return <Spinner />;
 */
export const useAuthLoading = (): boolean => {
  const { isLoading, isFetching } = useUser();
  return isLoading || isFetching;
};

/**
 * Get auth error
 * @example
 * const authError = useAuthError();
 * if (authError) return <ErrorMessage>{authError.message}</ErrorMessage>;
 */
export const useAuthError = (): Error | null => {
  const { error } = useUser();
  return error;
};

