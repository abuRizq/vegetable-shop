"use client";

import { useAuthInit } from "@/entities/user/lib/use-auth-init";
import { ReactNode } from "react";

interface AuthProviderProps {
  children: ReactNode;
}

/**
 * Authentication Provider Component
 * This component initializes authentication state on app startup
 * and provides loading states during initialization
 */
export function AuthProvider({ children }: AuthProviderProps) {
  const { isInitialized, isLoading } = useAuthInit();

  // Show loading spinner while checking authentication
  if (!isInitialized || isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="flex flex-col items-center space-y-4">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
          <p className="text-sm text-gray-600 dark:text-gray-400">
            Checking authentication...
          </p>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}