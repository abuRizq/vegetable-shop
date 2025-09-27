import { useAuthStore } from "@/entities/user/model/store";
import { useMutation } from "@tanstack/react-query";

type TLogoutMution = {
  onSuccess?: (data: void, variables: void, context: unknown) => unknown;
  onError?: (error: Error, variables: void, context: unknown) => unknown;
};

const useLogoutMution = ({ onSuccess, onError }: TLogoutMution) => {
  const { logout, clearUser } = useAuthStore();

  return useMutation({
    mutationFn: async () => {
      try {
        const response = await fetch(`/api/auth/logout`, {
          method: "POST",
        });
        if (!response.ok) {
          const errordata = await response
            .json()
            .catch(() => ({ error: "Failed to parse error response" }));
          throw new Error(errordata.error || "try agian");
        }
        return response.json();
      } catch (error) {
        return console.error(error);
      }
    },
    onSuccess: (data, variables, ctx) => {
      clearUser();
      logout();
      if (!!onSuccess) {
        onSuccess(data, variables, ctx);
      }
    },
    onError: (error, variables, ctx) => {
      clearUser();
      logout();
      if (!!onError) {
        onError(error, variables, ctx);
      }
    },
  });
};
export { useLogoutMution };
