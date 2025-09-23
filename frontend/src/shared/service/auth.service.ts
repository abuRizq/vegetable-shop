import { User } from "@/entities/user";
import { VerifyResetTokenResponse, ResetPasswordRequest, ResetPasswordResponse } from "@/features/auth/foreget-password/lib/type";
import { LoginCredentials, LoginResponse } from "@/features/auth/login/lib/type";
import { RegisterCredentials } from "@/features/auth/register/lib/type";

const baseURL = process.env.NEXT_PUBLIC_API_URL;
// import { LoginCredentials, LoginResponse, RegisterCredentials, ResetPasswordRequest, ResetPasswordResponse, User, VerifyResetTokenResponse } from "../lib/auth";
function setTokenToLoacalStorage(token: string): void {
    if (typeof window !== 'undefined') {
        localStorage.setItem('auth_token', token);
    }
}
function getTokenFromLocalStorage(): string | null {
    if (typeof window !== 'undefined') {
        return localStorage.getItem('auth_token');
    }
    return null;
}
function removeTokenFromLocalStorage(): void {
    localStorage.removeItem('auth_token');
}

class AuthService {
    async sendResetPasswordLink(eamil: string): Promise<void> {
        try {
            const response = await fetch(`${baseURL}/`, {
                method: "POST",
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    email: eamil,
                }),
            });
            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || 'Failed to send OTP');
            }
            const data = await response.json()
        } catch (error) {
            console.error('Send OTP error:', error);
            throw error;
        }
    }
    async verifyResetToken(token: string): Promise<VerifyResetTokenResponse> {
        try {
            const response = await fetch(`${baseURL}/api/auth/verify-reset-token/${token}`, {
                method: "GET",
            });
            if (!response.ok) {
                return {
                    valid: false,
                    message: 'Invalid or expired reset link'
                };
            }
            return await response.json();
        } catch (error) {
            return {
                valid: false,
                message: 'An error occurred while verifying the reset token'
            };
        }
    }
    async resetPasswordWithLink(data: ResetPasswordRequest): Promise<ResetPasswordResponse> {
        try {
            const response = await fetch(`${baseURL}/api/auth/reset-password`, {
                method: "POST",
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(data),
            })
            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || 'Failed to reset password');
            }
            return await response.json();
        } catch (error) {
            console.error('Reset password error:', error);
            throw error;
        }
    }
    async virfyToken(): Promise<LoginResponse | null> {
        const token = getTokenFromLocalStorage();
        if (!token) {
            return null;
        }
        try {
            const response = await fetch(`${baseURL}/auth/me`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
            });
            if (!response.ok) {
                removeTokenFromLocalStorage();
                return null;
            }
            const user: LoginResponse = await response.json();
            return user;
        } catch (error) {
            console.error('Error during token verification:', error);
            removeTokenFromLocalStorage();
            return null;
        }
    }
    async logout(): Promise<void> {
        try {
            const token = getTokenFromLocalStorage();
            if (!token) return;
            await fetch(`${baseURL}/auth/logout`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token} `,
                },
            });
            removeTokenFromLocalStorage();
        } catch (error) {
            console.error('Error during logout:', error);
        }
    }
    // getAuthHeader(): Record<string, string> {
    //     // const token = getTokenFromLocalStorage();
    //     return token ? { 'Authorization': `Bearer ${token} ` } : {};
    // }
}   
export const authService = new AuthService();
