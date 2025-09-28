"use client";

import { useEffect } from "react";
import { useAuthStore } from "../model/store";
import { useUserProfile } from "../api/queries";

/**
 * Authentication initialization hook
 * This hook handles:
 * 1. Zustand store hydration from localStorage
 * 2. Automatic authentication check on app startup
 * 3. Syncing localStorage state with server state
 */
export const useAuthInit = () => {
  const { user, isAuthenticated, token } = useAuthStore();
  
  // Manually hydrate Zustand store from localStorage
  useEffect(() => {
    useAuthStore.persist.rehydrate();
  }, []);

  // Check if we have stored auth data that suggests user should be authenticated
  const shouldCheckAuth = isAuthenticated && (user || token);

  // Use the user profile query to verify authentication with server
  const userQuery = useUserProfile();

  // Enable the query only if we think user should be authenticated
  // This prevents unnecessary API calls for non-authenticated users
  useEffect(() => {
    if (shouldCheckAuth && !userQuery.data && !userQuery.isLoading) {
      userQuery.refetch();
    }
  }, [shouldCheckAuth, userQuery]);

  return {
    isInitialized: !userQuery.isLoading,
    isAuthenticated: isAuthenticated && !!user,
    user,
    isLoading: userQuery.isLoading,
    error: userQuery.error,
  };
};