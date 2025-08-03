import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { authService } from '../service/auth.service';


export const authKeys = {
    all: ['auth'] as const,
    user: () => [...authKeys.all, 'user'] as const,
};


export const useAuth = () => {
    const queryClient = useQueryClient();

    const {
        data: user,
        isLoading: isCheckingAuth,
        error,
        isError,
    } = useQuery({
        queryKey: authKeys.user(),
        queryFn: authService.validateTokenAndGetUser,

        // Only run if we have a token
        enabled: typeof window !== 'undefined' && !!localStorage.getItem('auth_token'),

        // Don't retry on auth errors
        retry: (failureCount, error: any) => {
            if (error.message.includes('Authentication expired') ||
                error.message.includes('No authentication token')) {
                return false;
            }
            return failureCount < 2;
        },

        // BEST PRACTICE 7: Regular token validation
        staleTime: 5 * 60 * 1000, // 5 minutes
        refetchInterval: 15 * 60 * 1000, // Revalidate every 15 minutes
        refetchOnWindowFocus: true, // Check auth when user returns
        refetchOnReconnect: true, // Check auth on network reconnect
    });

    // BEST PRACTICE 8: Login mutation with proper state updates
    const loginMutation = useMutation({
        mutationFn: authService.login,
        onSuccess: (data) => {
            // Update user cache immediately
            queryClient.setQueryData(authKeys.user(), data.user);

            // Invalidate to ensure fresh data
            queryClient.invalidateQueries({ queryKey: authKeys.all });
        },
        onError: (error) => {
            // Clear any stale user data on login failure
            queryClient.removeQueries({ queryKey: authKeys.user() });
        },
    });

    // BEST PRACTICE 9: Logout mutation with complete cleanup
    const logoutMutation = useMutation({
        mutationFn: authService.logout,
        onSuccess: () => {
            // Clear ALL cached data on logout
            queryClient.clear();
        },
        onError: () => {
            // Even if logout fails, clear local state
            queryClient.clear();
        },
    });

    // BEST PRACTICE 10: Computed authentication status
    const isAuthenticated = !!(user && !isError);
    const isLoading = isCheckingAuth || loginMutation.isPending || logoutMutation.isPending;

    return {
        // State
        user,
        isAuthenticated,
        isLoading,
        error: error?.message || loginMutation.error?.message,

        // Actions
        login: loginMutation.mutateAsync,
        logout: logoutMutation.mutate,

        // Utilities
        refetchUser: () => queryClient.invalidateQueries({ queryKey: authKeys.user() }),
        clearError: () => queryClient.resetQueries({ queryKey: authKeys.user() }),
    };
};