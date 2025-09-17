/* eslint-disable @typescript-eslint/no-explicit-any */
import { AuthApiError } from "@/shared/lib/auth"

export interface AuthApiResponse<T = any> {
    success: boolean
    data?: T
    message?: string
    errors?: AuthApiError[]
}

export interface AuthApiRequest {
    headers?: Record<string, string>
    params?: Record<string, any>
}

// Auth endpoints configuration
export interface AuthEndpoints {
    login: string
    register: string
    logout: string
    forgotPassword: string
    resetPassword: string
    verifyResetToken: string
    refreshToken: string
    profile: string
}