"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { RegisterCredentials } from "../lib/type";
import { userQueryKeys } from "@/entities/user/api/auth-hooks";

type RegisterMutationOptions = {
  onSuccess?: (data: unknown, variables: RegisterCredentials, context: unknown) => void;
  onError?: (error: Error, variables: RegisterCredentials, context: unknown) => void;
};

/**
 * Register mutation hook - React Query only version
 * No Zustand dependency - all state managed by React Query
 */
export const useRegisterMutation = ({ onSuccess, onError }: RegisterMutationOptions = {}) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (credentials: RegisterCredentials) => {
      const response = await fetch("/api/auth/register", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(credentials),
        credentials: "include",
      });

      if (!response.ok) {
        const errorData = await response
          .json()
          .catch(() => ({ error: "Failed to parse error response" }));
        throw new Error(errorData.message || errorData.error || "Registration failed");
      }

      return response.json();
    },
    onSuccess: (data, variables, ctx) => {
      // Extract user from response
      const user = data.data?.user || data.user;

      if (user) {
        // Update React Query cache with user data
        queryClient.setQueryData(userQueryKeys.me(), user);
      }

      // Invalidate to trigger refetch (ensures fresh data)
      queryClient.invalidateQueries({ queryKey: userQueryKeys.all });

      // Call custom success handler
      if (onSuccess) {
        onSuccess(data, variables, ctx);
      }
    },
    onError: (error, variables, ctx) => {
      console.error("Registration error:", error);

      // Call custom error handler
      if (onError) {
        onError(error, variables, ctx);
      }
    },
  });
};

