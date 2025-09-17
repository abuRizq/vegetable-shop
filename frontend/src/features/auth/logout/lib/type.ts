export interface LogoutRequest {
    token?: string
    refreshToken?: string
}

export interface LogoutResponse {
    success: boolean
    message?: string
}
