

import { api } from "@/shared/lib/api-client";
import { useQuery, useQueryClient } from "@tanstack/react-query";


export const userQueryKeys = {
  all: ["user"] as const,
  me: () => [...userQueryKeys.all, "me"] as const,
};


async function fetchUserProfile() {
  try {
    const response = await api.get(`/auth/me`, {
      requiresAuth: true,
    });

    // 401 = Not authenticated (normal case, not an error)
    if (response.status === 401) {
      return null;
    }

    if (!response.ok) {
      const errorData = await response.json().catch(() => ({}));
      throw new Error(
        errorData.error || "your session is end , need to login again"
      );
    }
    console.log(response);
    const data = await response.json();
    console.log(data.user.data, "from the ts");
    return data.user.data;
  } catch (error) {
    console.error("User profile fetch error:", error);
    if (error instanceof TypeError) {
      return null;
    }
    throw error;
  }
}

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

export const useIsAuthenticated = (): boolean => {
  const { data: user, isLoading } = useUser();
  if (isLoading) return false;
  return !!user;
};

export const useIsAdmin = (): boolean => {
  const { data: user } = useUser();
  return user?.role === "ADMIN";
};
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
// export const useUserInitials = (): string => {
//   const { data: user } = useUser();
//   if (!user) return "G";

//   return user.name
//     .split(" ")
//     .map((word) => word[0])
//     .join("")
//     .toUpperCase()
//     .slice(0, 2);
// };

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
