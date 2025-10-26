"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { LoginCredentials } from "../lib/type";
import { userQueryKeys } from "@/entities/user/api/auth-hooks";
import { api } from "@/shared/lib/api-client";
import { setAuthToken } from "@/shared/lib/cookies";

type LoginMutationOptions = {
  onSuccess?: (data: unknown, variables: LoginCredentials, context: unknown) => void;
  onError?: (error: Error, variables: LoginCredentials, context: unknown) => void;
};

export const useLoginMutation = ({ onSuccess, onError }: LoginMutationOptions = {}) => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: async (credentials: LoginCredentials & { rememberMe?: boolean }) => {
      const response = await api.post('/auth/login', credentials, { requiresAuth: false });
      // Extract token from response and store in cookie
      const token = response.data?.token || response.token;
      if (token) {
        setAuthToken(token, credentials.rememberMe);
      }
      return response;
    },
    onSuccess: (data, variables, ctx) => {
      // Extract user from response
      const user = data.data?.user || data.user;
      if (user) {
        queryClient.setQueryData(userQueryKeys.me(), user);
      }
      queryClient.invalidateQueries({ queryKey: userQueryKeys.all });
      // Invalidate to trigger refetch (ensures fresh data)
      // Call custom success handler
      if (onSuccess) {
        onSuccess(data, variables, ctx);
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

