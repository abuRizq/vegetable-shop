import { LoginCredentials } from "@/app/types/auth";
import { useMutation, useQueryClient } from "@tanstack/react-query";

type TLoginMution = {
    onSuccess?: (data: void, variables: LoginCredentials, context: unknown) => unknown;
    onError?: (error: Error, variables: LoginCredentials, context: unknown) => unknown;
}
const useLoginMution = ({ onSuccess, onError }: TLoginMution) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async (credentials: LoginCredentials) => {
                const response = await fetch(`/api/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(credentials),
            });            
            if (!response.ok) {
                const errorData = await response
                .json()
                .catch(() => ({ error: 'Failed to parse error response' }));
                throw new Error(errorData.message || 'Failed to create user');
            }
            
            return response.json();
        },
        onSuccess: (data, variables, ctx) => {
            queryClient.invalidateQueries({ queryKey: ["user"] });
            queryClient.setQueryData(["user"], data.user);
            if(!!onSuccess) {
                onSuccess(data, variables, ctx)
            };
        },
        onError: (error, variables, ctx) => {
            console.error(error)
            if(!!onError) {
                onError(error, variables, ctx)
            };
        }
    });
}

export { useLoginMution };