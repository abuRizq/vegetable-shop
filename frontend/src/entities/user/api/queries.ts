/* eslint-disable @typescript-eslint/no-explicit-any */
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { useEffect } from "react";
import { useAuthStore } from "../model/store";
import { User } from "../model/type";
// import { validateTokenAndGetUser } from "../lib/get-user";

// Edit the valadtion procsess

export const userQueryKeys = {
  all: ["user"] as const,
  // profile: () => [...userQueryKeys.all, "profile"] as const,
  me: () => [...userQueryKeys.all, "me"] as const,
  // resetToken: (token: string) =>
  //   [...userQueryKeys.all, "resetToken", token] as const,
};
// Main user validation query (from your original useAuth)
export const useUserProfile = () => {
  // method to manage the user ans set and delete the user
  const { setUser, clearUser, setLoading, setError } = useAuthStore();
  //to control in the query form the cleint
  const queryClient = useQueryClient();
  // define the query
  const query = useQuery({
    queryKey: userQueryKeys.me(),
    queryFn: async () => {
      try {
        const response = await fetch(`http://localhost:8080/api/users/me`, {
          method: "GET",
          credentials: "include",
        });
        if (!response.ok) {
          if (response.status === 401) {
            throw new Error("No authentication token");
          }
          if (response.status === 403) {
            throw new Error("Authentication expired");
          }
          throw new Error("Authentication failed");
        }
        const data = await response.json();
        const User = data.data;
        console.log("form the vrfiy fun :" + data.token);
        return User;
      } catch (error) {
        console.error(error);
        throw error;
      }
    },
    retry: (failureCount, error: any) => {
      if (
        error?.message?.includes("Authentication expired") ||
        error?.message?.includes("No authentication token")
      ) {
        return false;
      }
      return failureCount < 2;
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
    refetchInterval: 15 * 60 * 1000, // 15 minutes
    refetchOnWindowFocus: true,
    refetchOnReconnect: true,
  });
  // Integrate with Zustand store (React Query v5: use side effects instead of callbacks)
  useEffect(() => {
    if (query.isSuccess) {
      setUser(query.data as User);
      setError(null);
    }
  }, [query.isSuccess, query.data, setUser, setError]);
  useEffect(() => {
    if (query.isError) {
      const err: any = query.error as any;
      clearUser();
      setError(err?.message || "Authentication failed");
      const msg = err?.message || "";
      if (
        msg.includes("Authentication expired") ||
        msg.includes("No authentication token")
      ) {
        queryClient.removeQueries({ queryKey: userQueryKeys.all });
      }
    }
  }, [query.isError, query.error, clearUser, setError, queryClient]);

  useEffect(() => {
    setLoading(query.isLoading);
  }, [query.isLoading, setLoading]);

  return query;
};
// export const useVerifyResetToken = (token: string) => {
//   return useQuery({
//     queryKey: userQueryKeys.resetToken(token),
//     // queryFn: () => verifyResetToken(token),
//     enabled: !!token,
//     retry: false,
//     staleTime: 0, // Always fresh check
//   });
// };
// Utility functions
// export const useAuthHelpers = () => {
//   const queryClient = useQueryClient();
//   return {
//     // Refresh user data
//     refetchUser: () => {
//       return queryClient.invalidateQueries({ queryKey: userQueryKeys.me() });
//     },

//     // Clear auth errors and refetch
//     clearAuthError: () => {
//       queryClient.resetQueries({ queryKey: userQueryKeys.me() });
//       useAuthStore.getState().clearError();
//     },

//     // Manually trigger auth check
//     checkAuth: () => {
//       return queryClient.refetchQueries({ queryKey: userQueryKeys.me() });
//     },

//     // Clear all auth data
//     clearAuthData: () => {
//       queryClient.removeQueries({ queryKey: userQueryKeys.all });
//       useAuthStore.getState().logout();
//     },
//   };
// };
