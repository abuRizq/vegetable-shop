import { useMutation } from "@tanstack/react-query";

type TLogoutMution = {
    onSuccess?: (data: void, variables: void, context: unknown) => unknown;
    onError?: (error: Error, variables: void, context: unknown) => unknown;
}

const useLogoutMution = ({ onSuccess, onError }: TLogoutMution) => {
    return useMutation({
        mutationFn: async () => {
            try {
                const response = await fetch(`/api/auth/logout`, {
                    method: 'POST',
                });
                return response.json();
            } catch (error) {
                return console.error(error)
            }
        },
        onSuccess: (data, variables, ctx) => {
            if(!!onSuccess) {
                onSuccess(data, variables, ctx)
            };
        },
        onError: (error, variables, ctx) => {
            if(!!onError) {
                onError(error, variables, ctx)
            };
        }
    });
}
export { useLogoutMution };