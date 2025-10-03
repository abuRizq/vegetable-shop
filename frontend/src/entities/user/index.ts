// Export types
export * from './model/type'

// Export React Query auth hooks (new - replaces Zustand)
export {
  useUser,
  useIsAuthenticated,
  // useUserInitials,
  // useLogout, 
  useRefetchUser,
  useAuthLoading,
  useAuthError,
  userQueryKeys,
} from './api/auth-hooks'

// Legacy exports (for backward compatibility during migration)
// TODO: Remove after migration is complete
// export { useUserProfile } from './api/auth-hooks'