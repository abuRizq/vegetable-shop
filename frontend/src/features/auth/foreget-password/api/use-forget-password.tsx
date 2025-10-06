import { useMutation } from "@tanstack/react-query";

type Tforgetpass = {
  onSuccess?: (
    data: unknown,
    variables: string,
    ctx: unknown
  ) => void;
  onError?: (
    error: Error,
    variables: string,
    ctx: unknown
  ) => void;
};
const useSendRestPasswordEmail = ({ onError, onSuccess }: Tforgetpass={}) => {
  return useMutation({
    mutationFn: async (credentials: string) => {
      const response = await fetch("api/auth/sendEmail", {
        method:"POST",
        body:JSON.stringify({ email: credentials }),
        headers: { "Content-type": "application/json" },
      });
      const data = await response.json();
      if (!response.ok) {
        throw new Error(data.error || "Failed to send reset email");
      }
      return data;
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
