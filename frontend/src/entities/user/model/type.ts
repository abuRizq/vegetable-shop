export interface User {
    id: string
    name: string
    email: string
    role: 'USER' | 'ADMIN'
    isEmailVerified?: boolean
    avatar?: string
    createdAt?: string
    updatedAt?: string
}

// User role enum for better type safety
export enum UserRole {
    USER = 'USER',
    ADMIN = 'ADMIN'
}

// Auth state for user entity
export interface AuthState {
    user: User | null
    isAuthenticated: boolean
    isLoading: boolean
    error: string | null
}