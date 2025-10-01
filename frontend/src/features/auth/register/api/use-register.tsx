import { useMutation, useQueryClient } from "@tanstack/react-query";
import { RegisterCredentials } from "../lib/type";
import { userQueryKeys } from "@/entities/user/api/auth-hooks";


type TregistrMution = {
    onSuccess: (data: void, variables: RegisterCredentials, ctx: unknown) => unknown,
    onError: (data: Error, variables: unknown, ctx: unknown) => unknown
}

/**
 * @deprecated Use useRegisterMutation from use-register-v2.tsx instead
 * This version is kept for backward compatibility during migration
 */
const useRegisterMution = ({ onSuccess, onError }: TregistrMution) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async (credentials: RegisterCredentials) => {
            const response = await fetch("/api/auth/register", {
                method: 'POST',
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(credentials),
                credentials: "include",
            })
            if (!response.ok) {
                const errorData = await response.json().catch(() => ({ error: 'Failed to parse error response' }))
                throw new Error(errorData.message || errorData.error || 'Registration failed');
            }
            return response.json();
        },
        onSuccess: (data, variables, ctx) => {
            const user = data.data?.user || data.user;
            if (user) {
                // Update React Query cache with user data
                queryClient.setQueryData(userQueryKeys.me(), user);
            }
            // Invalidate to trigger refetch (ensures fresh data)
            queryClient.invalidateQueries({ queryKey: userQueryKeys.all });

            if (!!onSuccess) {
                onSuccess(data, variables, ctx)
            };
        },
        onError: (error, variables, ctx) => {
            console.error("Registration error:", error);
            if (!!onError) {
                onError(error, variables, ctx)
            }
        }
    })
}

export { useRegisterMution };