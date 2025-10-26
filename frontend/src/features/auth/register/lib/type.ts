import { User, UserRole } from "@/entities/user"

export interface RegisterCredentials {
    name: string
    email: string
    password: string
    // confirmPassword: boolean
    role?: UserRole
    // acceptTerms: boolean
}

export interface RegisterRequest {
    name: string
    email: string
    password: string
    role: UserRole
}

export interface RegisterResponse {
    token: string
    user: User  // Import from entities/user
    message: string
    requiresVerification?: boolean
}

// Registration form validation
export interface RegisterFormData {
    name: string
    email: string
    password: string
    confirmPassword: string
    role: UserRole
    acceptTerms: boolean
}

export interface RegisterError {
    field?: 'name' | 'email' | 'password' | 'confirmPassword' | 'general'
    message: string
    code?: string
}
