"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { userQueryKeys } from "@/entities/user/api/auth-hooks";
import { removeAuthToken } from "@/shared/lib/cookies";
// import { api } from "@/shared/lib/api-client";

type LogoutMutationOptions = {
  onSuccess?: (data: void, variables: void, context: unknown) => void;
  onError?: (error: Error, variables: void, context: unknown) => void;
};

export const useLogoutMutation = ({
  onSuccess,
  onError,
}: LogoutMutationOptions = {}) => {
  const queryClient = useQueryClient();

  return useMutation<void, Error, void>({
    mutationFn: async () => {
         removeAuthToken();
    },
    onSuccess: (data, variables, ctx) => {
      // Clear user from React Query cache
      removeAuthToken();
      queryClient.setQueryData(userQueryKeys.me(), null);
      queryClient.invalidateQueries({ queryKey: userQueryKeys.all });
      // Invalidate all user queries to ensure clean state
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
