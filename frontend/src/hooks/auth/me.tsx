import { useMutation, useQueryClient } from "@tanstack/react-query";


type MvirfyMution = {
    onSuccess?: (data: void, variables: unknown, context: unknown) => void;
    onError?: (error: Error, variables: unknown, context: unknown) => void;
}
const MeMution = ({ onError, onSuccess }: MvirfyMution) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async () => {
            const respons = await fetch("/api/auth/me", {
                method: "GET",
            }).then((res) => res.json());
            if (!respons.ok) {
                const errorData = await respons.error
                    .json()
                    .catch(() => ({ error: "Failed to parse error response" }));
                throw new Error(errorData.message || "Failed to create user");
            }
            return respons.data.json();
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
