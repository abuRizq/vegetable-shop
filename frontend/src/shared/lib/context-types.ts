import { LoginCredentials } from '@/features/auth/login/lib/type'
import { RegisterCredentials } from '@/features/auth/register/lib/type'
import { AuthConfig } from './auth'
import { User } from '@/entities/user'

export interface AuthContextValue {
    // State (from entities/user)
    user: User | null
    isAuthenticated: boolean
    isLoading: boolean
    error: string | null

    // Actions (delegated to features)
    login: (credentials: LoginCredentials) => Promise<void>
    register: (credentials: RegisterCredentials) => Promise<void>
    forgotPassword: (email: string) => Promise<void>
    resetPassword: (token: string, newPassword: string, confirmPassword: string) => Promise<void>
    logout: () => Promise<void>
    clearError: () => void

    // Token management
    getToken: () => string | null
    refreshToken: () => Promise<void>
}

// Auth provider props
export interface AuthProviderProps {
    children: React.ReactNode
    config?: Partial<AuthConfig>
}

