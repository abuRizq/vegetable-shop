import { useMutation, useQueryClient } from "@tanstack/react-query";


type VerifyMution = {
    onSuccess?: (data: void, variables: unknown, context: unknown) => void;
    onError?: (error: Error, variables: unknown, context: unknown) => void;
}
const MeMution = ({ onError, onSuccess }: VerifyMution) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async () => {
            const response = await fetch("/api/auth/me", {
                method: "GET",
            }).then((res) => res.json());
            if (!response.ok) {
                const errorData = await response.error
                    .json()
                    .catch(() => ({ error: "Failed to parse error response" }));
                throw new Error(errorData.message || "Failed to create user");
            }
            return response.data.json();
        },        
        onSuccess: (data, variables, ctx) => {
            queryClient.setQueryData(["user"], data.user);
            if (!!onSuccess) {
                onSuccess(data, variables, ctx)
            };
        },
        onError: (error, variables, ctx) => {
            console.error(error)
            if (!!onError) {
                onError(error, variables, ctx)
            };
        },
    });
}
export {
    MeMution
}
