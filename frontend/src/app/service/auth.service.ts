import { LoginCredentials, LoginResponse, RegisterCredentials, ResetPasswordRequest, ResetPasswordResponse, User, VerifyResetTokenResponse } from "../types/auth";

class AuthService {
    private baseURL = process.env.NEXT_PUBLIC_API_URL;
    private getTokenFromLocalStorage(): string | null {
        if (typeof window !== 'undefined') {
            return localStorage.getItem('token');
        }
        return null;
    }
    private setTokenToLoacalStorage(token: string): void {
        if (typeof window !== 'undefined') {
            localStorage.setItem('token', token);
        }
    }
    private removeTokenFromLocalStorage(): void {
        localStorage.removeItem('token');
    }
    async validateTokenAndGetUser(): Promise<LoginResponse> {
        const token = this.getTokenFromLocalStorage();
        try {
            const response = await fetch(`http://localhost:8080/api/auth/validate-token`, {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`,
                },
            });
            if (!response.ok) {
                const errorData = await (response).json().catch(() => ({}));
                throw new Error(errorData.message || `HTTP ${response.status}: Login failed`);
            }
            const data = await response.json();
            return data.data as LoginResponse;
        } catch (error) {
            console.error(error);
            throw error;
        }
    }
    async login(credentials: LoginCredentials): Promise<LoginResponse> {
        try {
            const response = await fetch(`http://localhost:8080/api/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(credentials),
            });
            if (!response.ok) {
                console.log("this is the response " + response);
                const errorData = await (response).json().catch(() => ({}));
                throw new Error(errorData.message || `HTTP ${response.status}: Login failed`);
            }
            const body = await response.json();
            const data = body.data;
            const token =
                body?.token ??
                body?.accessToken ??
                body?.data?.token ??
                body?.data?.accessToken;
            const user =
                body?.user ??
                body?.data?.user;
            if (!token || !user) {
                console.log("this is the response " + response.json());
                throw new Error('Invalid response format from server');
            }
            console.log("this is the response " + data);

            return data;
        } catch (error) {
            console.error('Error during login:', error);
            this.removeTokenFromLocalStorage();
            throw error;
        }
    }
    async Register(credentials: RegisterCredentials): Promise<LoginResponse> {
        try {
            const response = await fetch(`${this.baseURL}/users/register`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(credentials),
            });
            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.message || `HTTP ${response.status}: Registration failed`);
            }
            const data: LoginResponse = await response.json();
            if (!data.token || !data.user) {
                throw new Error('Invalid response format from server');
            }
            this.setTokenToLoacalStorage(data.token);
            return data;
        } catch (error) {
            this.removeTokenFromLocalStorage();
            throw error;
        }
    }
    async sendResetPasswordLink(eamil: string): Promise<void> {
        try {
            const response = await fetch(`${this.baseURL}/`, {
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
            const response = await fetch(`${this.baseURL}/api/auth/verify-reset-token/${token}`, {
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
            const response = await fetch(`${this.baseURL}/api/auth/reset-password`, {
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
        const token = this.getTokenFromLocalStorage();
        if (!token) {
            return null;
        }
        try {
            const response = await fetch(`${this.baseURL}/auth/me`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`,
                },
            });
            if (!response.ok) {
                this.removeTokenFromLocalStorage();
                return null;
            }
            const user: LoginResponse = await response.json();
            return user;
        } catch (error) {
            console.error('Error during token verification:', error);
            this.removeTokenFromLocalStorage();
            return null;
        }
    }
    async logout(): Promise<void> {
        try {
            const token = this.getTokenFromLocalStorage();
            if (!token) return;
            await fetch(`${this.baseURL
                } / auth / logout`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token} `,
                },
            });
            this.removeTokenFromLocalStorage();
        } catch (error) {
            console.error('Error during logout:', error);
        }
    }
    getAuthHeader(): Record<string, string> {
        const token = this.getTokenFromLocalStorage();
        return token ? { 'Authorization': `Bearer ${token} ` } : {};
    }
}
export const authService = new AuthService();
