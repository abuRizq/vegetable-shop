"use client";

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { RegisterCredentials } from "../lib/type";
import { userQueryKeys } from "@/entities/user/api/auth-hooks";
import { api } from "@/shared/lib/api-client";

type RegisterMutationOptions = {
  onSuccess?: (
    data: unknown,
    variables: RegisterCredentials,
    context: unknown
  ) => void;
  onError?: (
    error: Error,
    variables: RegisterCredentials,
    context: unknown
  ) => void;
};

export const useRegisterMutation = ({
  onSuccess,
  onError,
}: RegisterMutationOptions = {}) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (credentials: RegisterCredentials) => {
      return await api.post("/auth/register", credentials, {
        requiresAuth: true,
      });
    },
    onSuccess: (data, variables, ctx) => {
      const user = data.data?.user || data.user;
      if (user) {
        queryClient.setQueryData(userQueryKeys.me(), user);
      }
      queryClient.invalidateQueries({ queryKey: userQueryKeys.all });
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
