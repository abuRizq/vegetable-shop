"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { LoginCredentials } from "../lib/type";
import { userQueryKeys } from "@/entities/user/api/auth-hooks";
import { User } from "@/entities/user";

type LoginMutationOptions = {
  onSuccess?: (data: unknown, variables: LoginCredentials, context: unknown) => void;
  onError?: (error: Error, variables: LoginCredentials, context: unknown) => void;
};

export const useLoginMutation = ({ onSuccess, onError }: LoginMutationOptions = {}) => {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: async (credentials: LoginCredentials) => {
      const response = await fetch(`/api/auth/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
        body: JSON.stringify(credentials),
        credentials: "include",
      });
      if (!response.ok) {
        const errorData = await response
          .json()
          .catch(() => ({ error: "Failed to parse error response" }));
        throw new Error(errorData.message || errorData.error || "Login failed");
      }
      return response.json();
    },
    onSuccess: (data, variables, ctx) => {
      // Extract user from response
      const user = data.data.user;
      if (user) {
        queryClient.setQueryData(userQueryKeys.me(), user);
      }

      // Invalidate to trigger refetch (ensures fresh data)
      queryClient.invalidateQueries({ queryKey: userQueryKeys.all });
      // Call custom success handler
      if (onSuccess) {
        onSuccess(data.data, variables, ctx);
      }
    },
    onError: (error, variables, ctx) => {
      console.error("Login error:", error);
      // Call custom error handler
      if (onError) {
        onError(error, variables, ctx);
      }
    },
  });
};

