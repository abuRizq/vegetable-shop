import { useMutation, useQueryClient } from "@tanstack/react-query";


type MvirfyMution = {
    onSuccess?: (data: void, variables: unknown, context: unknown) => void;
    onError?: (data: void, variables: unknown, context: unknown) => void;
}

const MeMution = ({ onError, onSuccess }: MvirfyMution) => {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: async () => {
            const respons = await fetch("http://localhost:3000/api/me", {
                method: "GET",
            }).then((res) => res.json());
            if (!respons.ok) {
                const errorData = await respons.error
                    .json()
                    .catch(() => ({ error: "Failed to parse error response" }));
                throw new Error(errorData.message || "Failed to create user");
            }
            return respons.data.josn();
        },
        onSuccess: (data, variables, context) => {
            queryClient.setQueriesData(["user"], data.user);
        },
        onError: (data, variables, context) => {
            onError && onError(error);
        },
    });

}

