"use client"

import { useMutation, useQueryClient } from "@tanstack/react-query";
import { LoginCredentials } from "../lib/type";
import { useAuthStore } from "@/entities/user/model/store";

type TLoginMution = {
  onSuccess?: (
    data: void,
    variables: LoginCredentials,
    context: unknown
  ) => unknown;
  onError?: (
    error: Error,
    variables: LoginCredentials,
    context: unknown
  ) => unknown;
};
const useLoginMution = ({ onSuccess, onError }: TLoginMution) => {
  const queryClient = useQueryClient();
  const { setAuthentctedUSer, setError, clearError,setLoading,stopLoading } = useAuthStore(); // Get store actions
  return useMutation({
    mutationFn: async (credentials: LoginCredentials) => {
      setLoading(true);
      const response = await fetch(`/api/auth/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Accept: "application/json",
        },
        body: JSON.stringify(credentials),
      });
      if (!response.ok) {
        const errorData = await response
          .json()
          .catch(() => ({ error: "Failed to parse error response" }));
        throw new Error(errorData.message || "Failed to create user");
      }
      return response.json();
    },
    onSuccess: (data, variables, ctx) => {
      const user = data.data?.user || data.user;
      if (user) {
        setAuthentctedUSer(user, data.token);
        clearError();
        queryClient.invalidateQueries({ queryKey: ["user"] });
      }
      queryClient.setQueryData(["user"], data.user);
      if (!!onSuccess) {
        stopLoading();
        onSuccess(data, variables, ctx);
      }
    },
    onError: (error, variables, ctx) => {
      console.error(error);
      stopLoading();
      if (!!onError) {
        onError(error, variables, ctx);
        setError(error.message);
      } 
    },
  });
};

export { useLoginMution };
