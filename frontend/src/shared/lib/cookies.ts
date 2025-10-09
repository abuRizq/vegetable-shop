/**
 * Cookie utilities for token management
 * Handles secure storage and retrieval of authentication tokens
 */

const TOKEN_COOKIE_NAME = "auth_token";

/**
 * Set authentication token in cookie
 */

export function setAuthToken(token: string, rememberMe: boolean = false): void {
  const maxAge = rememberMe
    ? 60 * 60 * 24 * 30 // 30 days if remember me
    : 60 * 60 * 24 * 7; // 7 days default

  const secure = process.env.NODE_ENV === "production"   ? "; Secure" : "";
  document.cookie = `${TOKEN_COOKIE_NAME}=${token}; path=/; max-age=${maxAge}; SameSite=Lax${secure}`;
    // window.location.reload();

}

/**
 * Get authentication token from cookie
 */
export function getAuthToken(): string | null {
  const cookies = document.cookie.split(";");

  for (const cookie of cookies) {
    const [name, value] = cookie.trim().split("=");
    if (name === TOKEN_COOKIE_NAME) {
      return value;
    }
  }

  return null;
}

/**
 * Remove authentication token from cookie
 */
export function removeAuthToken(): void {
  document.cookie = `${TOKEN_COOKIE_NAME}=; path=/; max-age=0`;
   window.location.reload();
 
}

/**
 * Check if user has authentication token
 */
export function hasAuthToken(): boolean {
  return getAuthToken() !== null;
}
