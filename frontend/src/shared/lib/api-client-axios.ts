/* eslint-disable @typescript-eslint/no-explicit-any */
/**
 * API Client with axios - automatic token injection
 * Handles all HTTP requests to the backend with authentication
 */
import axios, { AxiosRequestConfig, AxiosResponse } from "axios";
import { getAuthToken } from "./cookies";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";

export interface ApiRequestOptions extends AxiosRequestConfig {
  requiresAuth?: boolean;
}

/**
 * Create axios instance with default config
 */
const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

/**
 * Request interceptor - automatically add auth token
 * 
 * Interceptors are middleware that run before/after every request or response.
 * This interceptor runs BEFORE every HTTP request is sent.
 *
 * How it works:
 * 1. axios.interceptors.request.use() - Registers a request interceptor
 * 2. First function (config) => {...} - Success handler, runs before request
 * 3. Second function (error) => {...} - Error handler, if request setup fails
 *
 * Line by line:
 * - const token = getAuthToken() - Retrieve JWT token from cookies
 * - if (token && config.headers) - Check if token exists and headers object is available
 * - config.headers.Authorization = `Bearer ${token}` - Add "Authorization: Bearer <token>" header
 * - return config - Return modified config so axios can proceed with the request
 * - Promise.reject(error) - If something fails during setup, propagate the error
 *
 * Why use this?
 * - DRY: No need to manually add Authorization header in every API call
 * - Centralized: All auth logic in one place
 * - Automatic: Works for all requests made through axiosInstance
 */
axiosInstance.interceptors.request.use(
  (config) => {
    const token = getAuthToken();
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

/**
 * Response interceptor - handle errors
 *
 * This interceptor runs AFTER every HTTP response is received.
 *
 * How it works:
 * 1. axios.interceptors.response.use() - Registers a response interceptor
 * 2. First function (response) => response - Success handler, runs on 2xx status codes
 * 3. Second function (error) => {...} - Error handler, runs on 4xx/5xx status codes
 *
 * Line by line:
 * - (response) => response - If request succeeded (200-299), just return the response unchanged
 * - (error) => {...} - If request failed (400-599 or network error), handle the error
 * - error.response?.data?.message - Try to get error message from backend response body
 * - error.message - If no backend message, use axios's default error message
 * - "Request failed" - Last resort fallback message
 * - Promise.reject(new Error(message)) - Reject with a clean Error object containing the message
 *
 * Why use this?
 * - Consistent error format: All errors are Error objects with user-friendly messages
 * - Backend integration: Extracts error messages from your API's response format
 * - Simplifies error handling: No need to check error.response?.data in every component
 */
axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    const message =
      error.response?.data?.message ||
      error.message ||
      "Request failed";
    return Promise.reject(new Error(message));
  }
);

/**
 * Typed API methods
 */
export const api = {
  get: async <T = any>(
    endpoint: string,
    options?: ApiRequestOptions
  ): Promise<T> => {
    const { requiresAuth = true, ...axiosConfig } = options || {};
    const response: AxiosResponse<T> = await axiosInstance.get(endpoint, {
      ...axiosConfig,
      headers: {
        ...axiosConfig.headers,
        ...(requiresAuth ? {} : { Authorization: "" }),
      },
    });

    return response.data;
  },

  /**
   * POST request
   */
  post: async <T = any>(
    endpoint: string,
    data?: any,
    options?: ApiRequestOptions
  ): Promise<T> => {
    const { requiresAuth = true, ...axiosConfig } = options || {};
    const response: AxiosResponse<T> = await axiosInstance.post(
      endpoint,
      data,
      {
        ...axiosConfig,
        headers: {
          ...axiosConfig.headers,
          ...(requiresAuth ? {} : { Authorization: "" }),
        },
      }
    );

    return response.data;
  },

  /**
   * PUT request
   */
  put: async <T = any>(
    endpoint: string,
    data?: any,
    options?: ApiRequestOptions
  ): Promise<T> => {
    const { requiresAuth = true, ...axiosConfig } = options || {};

    const response: AxiosResponse<T> = await axiosInstance.put(endpoint, data, {
      ...axiosConfig,
      headers: {
        ...axiosConfig.headers,
        ...(requiresAuth ? {} : { Authorization: "" }),
      },
    });

    return response.data;
  },

  /**
   * PATCH request
   */
  patch: async <T = any>(
    endpoint: string,
    data?: any,
    options?: ApiRequestOptions
  ): Promise<T> => {
    const { requiresAuth = true, ...axiosConfig } = options || {};

    const response: AxiosResponse<T> = await axiosInstance.patch(
      endpoint,
      data,
      {
        ...axiosConfig,
        headers: {
          ...axiosConfig.headers,
          ...(requiresAuth ? {} : { Authorization: "" }),
        },
      }
    );

    return response.data;
  },

  /**
   * DELETE request
   */
  delete: async <T = any>(
    endpoint: string,
    options?: ApiRequestOptions
  ): Promise<T> => {
    const { requiresAuth = true, ...axiosConfig } = options || {};

    const response: AxiosResponse<T> = await axiosInstance.delete(endpoint, {
      ...axiosConfig,
      headers: {
        ...axiosConfig.headers,
        ...(requiresAuth ? {} : { Authorization: "" }),
      },
    });

    return response.data;
  },
};

export default axiosInstance;
