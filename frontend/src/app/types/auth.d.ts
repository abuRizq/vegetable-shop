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
