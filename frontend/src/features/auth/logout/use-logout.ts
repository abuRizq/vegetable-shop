"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { api } from "@/shared/lib/api-client";
import { removeAuthToken } from "@/shared/lib/cookies";
import { userQueryKeys } from "@/entities/user/api/auth-hooks";

type LogoutMutationOptions = {
  onSuccess?: () => void;
  onError?: (error: Error) => void;
};

export const useLogoutMutation = ({ onSuccess, onError }: LogoutMutationOptions = {}) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async () => {
      // Call backend logout endpoint (optional - if backend needs to invalidate session)
      try {
        await api.post('/auth/logout');
      } catch (error) {
        // If backend logout fails, we still want to logout client-side
        console.warn("Backend logout failed:", error);
      }
    },
    onSuccess: () => {
      // Remove token from cookie
      removeAuthToken();

      // Clear user data from React Query cache
      queryClient.setQueryData(userQueryKeys.me(), null);
      queryClient.invalidateQueries({ queryKey: userQueryKeys.all });

      // Call custom success handler
      if (onSuccess) {
        onSuccess();
      }
    },
    onError: (error) => {
      console.error("Logout error:", error);

      // Even if logout fails, remove token
      removeAuthToken();
      queryClient.setQueryData(userQueryKeys.me(), null);

      // Call custom error handler
      if (onError) {
        onError(error);
      }
    },
  });
};
