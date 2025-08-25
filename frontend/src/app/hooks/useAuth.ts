import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query"
import { authService } from "../../shared/service/auth.service";
// import { USER_QK } from "../types/auth";

const Authkey = {
    all: ['user'] as const,
    user: () => [...Authkey.all, 'user'] as const,
    resetToken: (token: string) => [...Authkey.all, 'resetToken', token] as const
}

/**
   * EnterEmail
   * Input Field: Email Address --> Form on submit(It will send request to the backend having the email address).
   * Back-End: Will validate Email address: It will send email with link to reset password. /froget-password?token=3454-gdf545-gdfg-3445
   * The back-end will send 
   * /froget-password --> UI
   */
export const useAuth = () => {
    const quryClinet = useQueryClient();
    const {
        data: user,
        isLoading: isCheckingAuth,
        error,
        isError,
    } = useQuery({
        queryKey: Authkey.all,
        queryFn: authService.validateTokenAndGetUser,
        // enabled: typeof window !== 'undefined' && !!localStorage.getItem('auth_token'),
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
            quryClinet.invalidateQueries({ queryKey: ["user"] })
            quryClinet.setQueryData(["user"], data.user)
        },
        onError: (error) => {
            // TODO: Error handling and toast notification if excits.
        },
    });
    const RegisterMution = useMutation({
        mutationFn: authService.Register,
        onSuccess: (data) => {
            quryClinet.setQueryData(Authkey.user(), data.user)
        },
        onError: (error) => {
            // TODO: Error handling and toast notification if excits.
        }
    })
    const forgotPasswordMutation = useMutation({
        mutationFn: authService.sendResetPasswordLink
    })
    const resetPasswordMutation = useMutation({
        mutationFn: authService.resetPasswordWithLink,
        onSuccess: (data) => {
            // quryClinet.removeQueries({ queryKey: Authkey.user() })
        },
    })
    const useVerifyResetToken = (token: string) => {
        return useQuery({
            queryKey: Authkey.resetToken(token),
            queryFn: () => authService.verifyResetToken(token),
            enabled: !!token,
            retry: false,
            staleTime: 0, // Always fresh check
        });
    };
    const isAuthenticated = !!(user && !isError);
    const isLoading = isCheckingAuth || LoginMution.isPending;
    return {
        user,
        isAuthenticated,
        isLoading,
        error: error?.massage || LoginMution.error?.message,
        login: LoginMution.mutate,
        Register: RegisterMution.mutate,
        forgotPassword: forgotPasswordMutation.mutate,
        resetPassword: resetPasswordMutation.mutate,
        useVerifyResetToken,
        forgotPasswordState: {
            isPending: forgotPasswordMutation.isPending,
            isSuccess: forgotPasswordMutation.isSuccess,
            isError: forgotPasswordMutation.isError,
            error: forgotPasswordMutation.error?.message,
            data: forgotPasswordMutation.data,
        },
        resetPasswordState: {
            isPending: resetPasswordMutation.isPending,
            isSuccess: resetPasswordMutation.isSuccess,
            isError: resetPasswordMutation.isError,
            error: resetPasswordMutation.error?.message,
            data: resetPasswordMutation.data,
        },
        refetchUser: () =>
            quryClinet.invalidateQueries({ queryKey: Authkey.user() }),
        clearError: () => {
            quryClinet.resetQueries({ queryKey: Authkey.user() });
        },
    };
}
