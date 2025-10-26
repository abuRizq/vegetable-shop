import { api } from "@/shared/lib/api-client";
import { useMutation } from "@tanstack/react-query";

type Tforgetpass = {
  onSuccess?: (data: unknown, variables: string, ctx: unknown) => void;
  onError?: (error: Error, variables: string, ctx: unknown) => void;
};
const useSendRestPasswordEmail = ({ onError, onSuccess }: Tforgetpass = {}) => {
  return useMutation({
    mutationFn: async (credentials: string) => {
      return await api.post("/auth/forgot-password", {email:credentials}, {
        requiresAuth: false
      });
    },
    onSuccess,
    onError: (error, variables, ctx) => {
      console.error("Forgot password error: ", error);
      if (onError) {
        onError(error, variables, ctx);
      }
    },
  });
};

export { useSendRestPasswordEmail };
