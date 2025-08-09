import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { authService } from "../service/auth.service";

const Authkey = {
    all: ['user'] as const,
    user: () => [...Authkey.all, 'user'] as const
}

export const useAuth = () => {
    const quryClinet = useQueryClient();
    const {
        data: user,
        isLoading: isCheckingAuth,
        error,
        isError,
    } = useQuery({
        queryKey: Authkey.user(),
        queryFn: authService.validateTokenAndGetUser,
        enabled: typeof window !== 'undefined' && !!localStorage.getItem('auth_token'),
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        retry: (failureCount, error: any) => {
            if (error?.message?.includes('Authentication expired') ||
                error?.message?.includes('No authentication token')) {
                return false;
            }
            return failureCount < 2;
        },
        staleTime: 5 * 60 * 1000,
        refetchInterval: 15 * 60 * 1000,
        refetchOnWindowFocus: true,
        refetchOnReconnect: true
    });
    const LoginMution = useMutation({
        mutationFn: authService.login,  
        onSuccess: (data) => {
            quryClinet.setQueryData(Authkey.user(), data.user)
            quryClinet.invalidateQueries({ queryKey: Authkey.user() })
        },
        onError: (error) => {
            quryClinet.removeQueries({ queryKey: Authkey.user() });
        }
    });
    const LogoutMution = useMutation({
        mutationFn: authService.logout,
        onSuccess: () => {
            quryClinet.removeQueries({ queryKey: Authkey.user() });
        },
        onError: () => {
            quryClinet.removeQueries({ queryKey: Authkey.user() });
        }
    })
    const RegisterMution = useMutation({
        mutationFn: authService.Register,
        onSuccess: (data) => {
            quryClinet.setQueryData(Authkey.user(), data.user)
        },
        onError: (error) => {
            quryClinet.removeQueries({ queryKey: Authkey.user() })
        }
    })
    const isAuthenticated = !!(user && !isError);
    const isLoading = isCheckingAuth || LoginMution.isPending || LogoutMution.isPending;
    return {
        user,
        isAuthenticated,
        isLoading,
        error: error?.massage || LoginMution.error?.message,
        login: LoginMution.mutate,
        logout: LogoutMution.mutate,
        Register: RegisterMution.mutate,
        refetchUser: () =>
            quryClinet.invalidateQueries({ queryKey: Authkey.user() }),
        clearError: () => {
            quryClinet.resetQueries({ queryKey: Authkey.user() });
        },
    };
}
