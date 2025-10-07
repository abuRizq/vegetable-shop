import { useMutation } from "@tanstack/react-query";
import { resetPassword } from "../lib/type";

type TestPassword = {
  onSuccess?: (data: unknown, variables: resetPassword, ctx: unknown) => void;
  onError?: (data: Error, variables: resetPassword, ctx: unknown) => void;
};

const useRestPassword = ({ onSuccess, onError }: TestPassword ={}) => {
  return useMutation({
    mutationFn: async (credentials: resetPassword) => {
      const res = await fetch("http://localhost:8080/api/auth/reset-password", {
        method: "POST",
        headers: { "Content-type": "application/json" },
        body: JSON.stringify(credentials),
      });
      console.log(res);
      // const data = await res.json();
      if (!res.ok) {
        throw new Error(res.status + "Failed to reset password");
      }
      // return data;
    },
    onSuccess: (data, variables, ctx) => {
      if (onSuccess) {
        onSuccess(data, variables, ctx);
      }
    },
    onError(error, variables, ctx) {
      console.error("Forgot password error: ", error);
      if (onError) {
        onError(error, variables, ctx);
      }
    },
  });
};

export { useRestPassword };
