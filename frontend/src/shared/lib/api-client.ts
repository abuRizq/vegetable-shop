/* eslint-disable @typescript-eslint/no-explicit-any */
/**
 * API Client with automatic token injection
 * Handles all HTTP requests to the backend with authentication
 */
import { getAuthToken } from "./cookies";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";

export interface ApiRequestOptions extends RequestInit {
  requiresAuth?: boolean;
}
/**
 * Enhanced fetch with automatic token injection
 */

export async function apiClient(
  endpoint: string,
  options: ApiRequestOptions = {}
): Promise<Response> {
  const { requiresAuth = true, headers = {}, ...restOptions } = options;

  // Build headers
  const requestHeaders: Record<string, string> = {
    "Content-Type": "application/json",
    ...(headers as Record<string, string>),
  };
  
  // Add authorization token if required
  if (requiresAuth) {
    const token = getAuthToken();
    if (token) {
      requestHeaders["Authorization"] = `Bearer ${token}`;
    }
  }

  // Build full URL
  const url = endpoint.startsWith("http")
    ? endpoint
    : `${API_BASE_URL}${endpoint}`;
  // Make request
  const response = await fetch(url, {
    ...restOptions,
    headers: requestHeaders,
  });

  return response;
}

/**
 * Typed API methods
 */
export const api = {
  get: async <T = any>(
    endpoint: string,
    options?: ApiRequestOptions
  ): Promise<T> => {
    const response = await apiClient(endpoint, { ...options, method: "GET" });
    if (!response.ok) {
      const error = await response
        .json()
        .catch(() => ({ message: "Request failed" }));
      throw new Error(error.message || `GET ${endpoint} failed`);
    }

    return response.json();
  
  },
  post: async (
    endpoint: string,
    data?: any,
    options?: ApiRequestOptions
  ): Promise<any> => {
    const response = await apiClient(endpoint, {
      ...options,
      method: "POST",
      body: data ? JSON.stringify(data) : undefined,
    });

    if (!response.ok) {
      const error = await response
        .json()
        .catch(() => ({ message: "Request failed" }));
      throw new Error(error.message);
    }
    if (response) {
    }
    const text = await response.text();
    return text ? JSON.parse(text) : null;
  },

  /**
   * PUT request
   */

  put: async (
    endpoint: string,
    data?: any,
    options?: ApiRequestOptions
  ): Promise<any> => {
    const response = await apiClient(endpoint, {
      ...options,
      method: "PUT",
      body: data ? JSON.stringify(data) : undefined,
    });

    if (!response.ok) {
      const error = await response
        .json()
        .catch(() => ({ message: "Request failed" }));
      throw new Error(error.message || `PUT ${endpoint} failed`);
    }

    const text = await response.text();
    return text ? JSON.parse(text) : null;  },

  /**
   * PATCH request
   */
  patch: async (
    endpoint: string,
    data?: any,
    options?: ApiRequestOptions
  ): Promise<any> => {
    const response = await apiClient(endpoint, {
      ...options,
      method: "PATCH",
      body: data ? JSON.stringify(data) : undefined,
    });

    if (!response.ok) {
      const error = await response
        .json()
        .catch(() => ({ message: "Request failed" }));
      throw new Error(error.message || `PATCH ${endpoint} failed`);
    }

    const text = await response.text();
    return text ? JSON.parse(text) : null;  },

  /**
   * DELETE request
   */
  delete: async(
    endpoint: string,
    options?: ApiRequestOptions
  ): Promise<any> => {
    const response = await apiClient(endpoint, {
      ...options,
      method: "DELETE",
    });

    if (!response.ok) {
      const error = await response
        .json()
        .catch(() => ({ message: "Request failed" }));
      throw new Error(error.message || `DELETE ${endpoint} failed`);
    }

    const text = await response.text();
    return text ? JSON.parse(text) : null;  },
};
