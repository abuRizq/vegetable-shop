type User = {
    id: string;
    name: string;
    email: string;
    role: 'USER' | 'ADMIN';
}

export interface LoginCredentials {
    email: string;
    password: string;
}
export interface RegisterCredentials {
    name: string;
    email: string;
    password: string;
    role: "ADMIN" | "USER";
}
export const USER_QK = ['user'] as const;
export interface LoginResponse {
    token: string;
    user: User;
}

export interface AuthContextType {
    user: User | null;
    token: string | null;
    isAuthenticated: boolean;
    isLoaing: boolean;
    Error: string | null;
    login: (email: string, password: string) => Promise<void>;
    logout: () => void;
    register: (name: string, email: string, password: string, role: "ADMIN" | "USER") => Promise<void>;
    clearError: () => void;
};
export interface ForgotPasswordRequest {
    email: string;
}

export interface ForgotPasswordResponse {
    success: boolean;
    message: string;
}

export interface ResetPasswordRequest {
    token: string; // From URL parameter
    newPassword: string;
    confirmPassword: string;
}
export interface ResetPasswordResponse {
    success: boolean;
    message: string;
}
export interface VerifyResetTokenResponse {
    valid: boolean;
    message?: string;
    email?: string; // Optional: show which email the token belongs to
}

