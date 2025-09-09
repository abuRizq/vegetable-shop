import { useQuery} from "@tanstack/react-query"

const useAuthQuery = () => {
    return useQuery({
        queryKey: ['user'],
        queryFn: async () => {
            const response = await fetch('api/auth/me', {
                method: "GET"
            })
            if (!response.ok) {
                const errorData = await response
                    .json()
                    .catch(() => ({ error: "Something went wrong" }));
                throw new Error(errorData.message || 'Failed to create user');
            }
            return await response.json()
        },
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        retry: (failureCount, error: any) => {
            if (error.massage.includes('Authentication expired') || error.massage.includes('No authentication token')) {
                return false;
            }
            return failureCount < 2;
        },
        staleTime: 5 * 60 * 1000,
        refetchInterval: 15 * 100 * 60,
        refetchOnWindowFocus: true,
        refetchOnReconnect: true
    })
}
export {
    useAuthQuery
}