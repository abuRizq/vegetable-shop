export interface ForgotPasswordRequest {
    email: string
}

export interface ForgotPasswordResponse {
    success: boolean
    message: string
    email?: string
}

export interface ForgotPasswordFormData {
    email: string
}

export interface ForgotPasswordError {
    field?: 'email' | 'general'
    message: string
    code?: string
}

export interface ResetPasswordRequest {
    token: string
    newPassword: string
    confirmPassword: string
}

export interface ResetPasswordResponse {
    success: boolean
    message: string
}

export interface ResetPasswordFormData {
    newPassword: string
    confirmPassword: string
}

export interface VerifyResetTokenRequest {
    token: string
}

export interface VerifyResetTokenResponse {
    valid: boolean
    message?: string
    email?: string
    expiresAt?: string
}

export interface ResetPasswordError {
    field?: 'newPassword' | 'confirmPassword' | 'token' | 'general'
    message: string
    code?: string
}