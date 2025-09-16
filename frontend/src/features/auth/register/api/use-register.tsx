import { RegisterCredentials } from "@/shared/types/auth";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { unknown } from "zod";


type TregistrMution = {
    onSuccess: (data: void, variables: RegisterCredentials, ctx: unknown) => unknown,
    onError: (data: Error, variables: unknown, ctx: unknown) => unknown
}

const useRegisterMution = ({ onSuccess, onError }: TregistrMution) => {
    const queryClinet = useQueryClient();
    return useMutation({
        mutationFn: async (creditels: RegisterCredentials) => {
            const res = await fetch("api/auth/register", {
                method: 'POST',
                body: JSON.stringify(creditels)
            })
            if (!res.ok) {
                const error = await res.json().catch(() => ({ error: 'Failed to parse error response' }))
                throw new Error(error.message || 'Failed to create user');
            }
            return res.json();
        },
        onSuccess: (data, variables, ctx) => {
            queryClinet.invalidateQueries({ queryKey: ['user'] });
            queryClinet.setQueryData(['user'], data.user)
            if (!!onSuccess) {
                onSuccess(data, variables, ctx)
            };
        },
        onError: (data, variables, ctx) => {
            if (!!onError) {
                onError(data, variables, ctx)
            }
        }
    })
}

export { useRegisterMution };