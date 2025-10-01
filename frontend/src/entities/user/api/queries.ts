/* eslint-disable @typescript-eslint/no-explicit-any */
/**
 * @deprecated This file is deprecated. Use auth-hooks.ts instead.
 *
 * Migration guide:
 * - Replace useUserProfile() with useUser() from auth-hooks.ts
 * - Replace userQueryKeys with userQueryKeys from auth-hooks.ts
 *
 * This file is kept for backward compatibility during migration.
 */
import { useQuery } from "@tanstack/react-query";
import { User } from "../model/type";
import { userQueryKeys as newUserQueryKeys } from "./auth-hooks";


/**
 * @deprecated Use userQueryKeys from auth-hooks.ts instead
 */
export const userQueryKeys = {
  all: ["user"] as const,
  // Key for current user profile query
  // Spread operator creates ['user', 'me']
  // Function allows dynamic generation if needed in future
  me: () => [...userQueryKeys.all, "me"] as const,
} as const;

/**
 * @deprecated Use useUser() from auth-hooks.ts instead
 * This version syncs with Zustand which is being phased out
 */
export const useUserProfile = () => {
  // define the query
  const query = useQuery({
    queryKey: newUserQueryKeys.me(),
    queryFn: async function fetchUserProfile(): Promise<User | null> {
      try {
        // Call Next.js API route (which proxies to backend)
        const response = await fetch('/api/auth/me', {
          method: "GET",
          // CRITICAL: Include credentials to send HTTP-only cookie
          credentials: "include",
        });

        // If not OK, handle different error scenarios
        if (!response.ok) {
          // 401 = Not authenticated (normal case, not an error)
          if (response.status === 401) {
            return null; // Return null instead of throwing
          }
          // Other errors: try to parse error message
          const errorData = await response.json().catch(() => ({}));
          throw new Error(errorData.error || "Failed to fetch user profile");
        }
        // Parse successful response
        const data = await response.json();

        // Return user object
        return data.data?.user || data.user || null;

      } catch (error) {
        // Log for debugging
        console.error("User profile fetch error:", error);

        // Handle network errors gracefully
        if (error instanceof TypeError) {
          // Network error - return null instead of throwing
          return null;
        }

        // Re-throw to let React Query handle retry logic
        throw error;
      }
    },
    retry: (failureCount, error: any) => {
      if (
        error?.message?.includes("Authentication expired") ||
        error?.message?.includes("No authentication token")
      ) {
        return false;
      }
      return failureCount < 2;
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
    gcTime: 10 * 60 * 1000, // 10 minutes
    refetchInterval: 15 * 60 * 1000, // 15 minutes
    refetchOnWindowFocus: true,
    refetchOnReconnect: true,
  });

  // No more Zustand integration - React Query is the single source of truth
  return query;
};
// export const useVerifyResetToken = (token: string) => {
//   return useQuery({
//     queryKey: userQueryKeys.resetToken(token),
//     // queryFn: () => verifyResetToken(token),
//     enabled: !!token,
//     retry: false,
//     staleTime: 0, // Always fresh check
//   });
// };
// Utility functions
// export const useAuthHelpers = () => {
//   const queryClient = useQueryClient();
//   return {
//     // Refresh user data
//     refetchUser: () => {
//       return queryClient.invalidateQueries({ queryKey: userQueryKeys.me() });
//     },

//     // Clear auth errors and refetch
//     clearAuthError: () => {
//       queryClient.resetQueries({ queryKey: userQueryKeys.me() });
//       useAuthStore.getState().clearError();
//     },

//     // Manually trigger auth check
//     checkAuth: () => {
//       return queryClient.refetchQueries({ queryKey: userQueryKeys.me() });
//     },

//     // Clear all auth data
//     clearAuthData: () => {
//       queryClient.removeQueries({ queryKey: userQueryKeys.all });
//       useAuthStore.getState().logout();
//     },
//   };
// };
