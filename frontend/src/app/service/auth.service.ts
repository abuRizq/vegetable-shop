import { LoginCredentials, LoginResponse, RegisterCredentials, User } from "../types/auth";

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
    async login(credentials: LoginCredentials): Promise<LoginResponse> {
        try {
            const response = await fetch(`${this.baseURL}/auth/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(credentials),
            });

            if (!response.ok) {
                const errorData = await (await response).json().catch(() => ({}));
                throw new Error(errorData.message || `HTTP ${response.status}: Login failed`);
            }
            const data: LoginResponse = await response.json();
            if (!data.token || !data.user) {
                throw new Error('Invalid response format from server');
            }
            this.setTokenToLoacalStorage(data.token);
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
    async validateTokenAndGetUser(): Promise<User | null> {
        const token = this.getTokenFromLocalStorage();
        if (!token) {
            return null;
        }
        try {
            const response = await fetch(`${this.baseURL}/api/auth/me`, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json',
                }
            })
            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    this.removeTokenFromLocalStorage()
                    throw new Error('Authentication expired');
                } else {
                    throw new Error('Invalid token or user not found');
                }
            }
            const user: User = await response.json();
            return user;
        }
        catch (error) {
            this.removeTokenFromLocalStorage()
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
    async logout(credentials: RegisterCredentials): Promise<void> {
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
