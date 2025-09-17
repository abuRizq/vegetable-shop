import { User } from "@/entities/user"

export interface LoginCredentials {
    email: string
    password: string
    rememberMe?: boolean
}

export interface LoginResponse {
    token: string
    refreshToken?: string
    user: User  // Import from entities/user
    expiresIn?: number
}

export interface LoginRequest {
    email: string
    password: string
    rememberMe?: boolean
}

// Login form validation
export interface LoginFormData {
    email: string
    password: string
    rememberMe: boolean
}

// Login error types
export interface LoginError {
    field?: 'email' | 'password' | 'general'
    message: string
    code?: string
}