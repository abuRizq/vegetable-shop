import { UserRole } from "@/entities/user"

export interface AuthTokens {
    token: string
    refreshToken?: string
    expiresIn?: number
    tokenType?: 'Bearer'
}

export interface RefreshTokenRequest {
    refreshToken: string
}

export interface RefreshTokenResponse {
    token: string
    refreshToken?: string
    expiresIn?: number
}

// API Error types
export interface AuthApiError {
    message: string
    code?: string
    field?: string
    statusCode?: number
}

// JWT Token payload structure
export interface JWTPayload {
    sub: string  // user id
    email: string
    role: UserRole
    iat: number
    exp: number
}

// Auth configuration
export interface AuthConfig {
    tokenKey: string
    refreshTokenKey: string
    apiEndpoint: string
    tokenExpirationBuffer: number // in minutes
}
