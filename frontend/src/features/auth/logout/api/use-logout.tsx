"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { userQueryKeys } from "@/entities/user/api/auth-hooks";

type LogoutMutationOptions = {
  onSuccess?: (data: void, variables: void, context: unknown) => void;
  onError?: (error: Error, variables: void, context: unknown) => void;
};

/**
 * Logout mutation hook - React Query only version
 * No Zustand dependency - all state managed by React Query
 */
export const useLogoutMutation = ({ onSuccess, onError }: LogoutMutationOptions = {}) => {
  const queryClient = useQueryClient();

  return useMutation<void, Error, void>({
    mutationFn: async () => {
      const response = await fetch(`/api/auth/logout`, {
        method: "POST",
        credentials: "include",
      });

      if (!response.ok) {
        const errorData = await response
          .json()
          .catch(() => ({ error: "Failed to parse error response" }));
        throw new Error(errorData.error || "Logout failed");
      }

      return response.json();
    },

    onSuccess: (data, variables, ctx) => {
      // Clear user from React Query cache
      queryClient.setQueryData(userQueryKeys.me(), null);
      // Invalidate all user queries to ensure clean state
      queryClient.invalidateQueries({ queryKey: userQueryKeys.all });

      // Call custom success handler
      if (onSuccess) {
        onSuccess(data, variables, ctx);
      }
    },
    onError: (error, variables, ctx) => {
      console.error("Logout error:", error);

      // Even on error, clear the cache (user intended to logout)
      queryClient.setQueryData(userQueryKeys.me(), null);

      // Call custom error handler
      if (onError) {
        onError(error, variables, ctx);
      }
    },
  });
};

