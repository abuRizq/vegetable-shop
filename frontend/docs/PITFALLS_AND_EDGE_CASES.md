# Pitfalls and Edge Cases - React Query Auth Migration

This document covers potential issues you might encounter and how to handle them.

---

## ⚠️ Common Pitfalls

### 1. **Not Checking `isLoading` Before Redirecting**

#### ❌ Problem:
```typescript
const { data: user } = useUser();

useEffect(() => {
  if (!user) {
    router.push("/login"); // Redirects even while loading!
  }
}, [user, router]);
```

**Issue**: While React Query is fetching, `user` is `undefined`, causing immediate redirect.

#### ✅ Solution:
```typescript
const { data: user, isLoading } = useUser();

useEffect(() => {
  if (!isLoading && !user) {
    router.push("/login"); // Only redirects after loading completes
  }
}, [user, isLoading, router]);
```

---

### 2. **Forgetting to Destructure `data`**

#### ❌ Problem:
```typescript
const user = useUser(); // Wrong! Returns query object, not user
console.log(user.name); // Error: user.name is undefined
```

#### ✅ Solution:
```typescript
const { data: user } = useUser(); // Correct!
console.log(user?.name); // Works
```

---

### 3. **Using Old Mutation Names**

#### ❌ Problem:
```typescript
import { useLoginMution } from "@/features/auth/login/api/use-login";
// Typo: "Mution" instead of "Mutation"
```

**Issue**: Old files still use Zustand. You'll get unexpected behavior.

#### ✅ Solution:
```typescript
import { useLoginMutation } from "@/features/auth/login/api/use-login-v2";
// Correct: "Mutation" and "-v2" file
```

---

### 4. **Calling Logout Synchronously**

#### ❌ Problem:
```typescript
const { logout } = useAuthStore(); // Old way
logout(); // Synchronous
router.push("/login");
```

#### ✅ Solution:
```typescript
const { mutate: logout } = useLogoutMutation({
  onSuccess: () => {
    router.push("/login"); // Navigate in callback
  }
});
logout(); // Async
```

---

### 5. **Using `window.location.reload()` After Logout**

#### ❌ Problem:
```typescript
logout();
window.location.reload(); // Harsh, loses state, bad UX
```

**Issue**: Full page reload is slow and loses all React state.

#### ✅ Solution:
```typescript
const { mutate: logout } = useLogoutMutation({
  onSuccess: () => {
    router.push("/login"); // Smooth navigation
  }
});
```

---

### 6. **Not Handling Network Errors**

#### ❌ Problem:
```typescript
async function fetchUserProfile() {
  const response = await fetch("/api/auth/me");
  if (!response.ok) throw new Error("Failed");
  return response.json();
}
```

**Issue**: Network errors (no internet) will throw and break the app.

#### ✅ Solution:
```typescript
async function fetchUserProfile() {
  try {
    const response = await fetch("/api/auth/me");
    if (!response.ok) {
      if (response.status === 401) return null;
      throw new Error("Failed");
    }
    return response.json();
  } catch (error) {
    if (error instanceof TypeError) {
      // Network error - return null instead of throwing
      return null;
    }
    throw error;
  }
}
```

---

### 7. **Infinite Refetch Loops**

#### ❌ Problem:
```typescript
const { data: user, refetch } = useUser();

useEffect(() => {
  refetch(); // Runs on every render!
}, [refetch]);
```

**Issue**: `refetch` causes re-render, which triggers `useEffect`, which calls `refetch`...

#### ✅ Solution:
```typescript
const { data: user } = useUser();
// React Query handles refetching automatically
// No manual refetch needed in most cases
```

---

### 8. **Not Using Query Keys Consistently**

#### ❌ Problem:
```typescript
// In one file:
queryClient.invalidateQueries({ queryKey: ["user"] });

// In another file:
queryClient.invalidateQueries({ queryKey: ["user", "me"] });
```

**Issue**: Inconsistent keys mean cache won't invalidate properly.

#### ✅ Solution:
```typescript
import { userQueryKeys } from "@/entities/user";

// Always use centralized keys:
queryClient.invalidateQueries({ queryKey: userQueryKeys.me() });
queryClient.invalidateQueries({ queryKey: userQueryKeys.all });
```

---

## 🐛 Edge Cases

### 1. **Token Expires While User is Active**

**Scenario**: User is on the page, token expires, next API call fails.

#### Solution:
React Query automatically handles this:
```typescript
export const useUser = () => {
  return useQuery({
    queryKey: userQueryKeys.me(),
    queryFn: fetchUserProfile,
    retry: (failureCount, error) => {
      // Don't retry auth failures
      if (error?.message?.includes("Authentication expired")) {
        return false;
      }
      return failureCount < 2;
    },
    // Automatically refetch every 15 minutes
    refetchInterval: 15 * 60 * 1000,
  });
};
```

When token expires:
1. Next refetch returns 401
2. `fetchUserProfile` returns `null`
3. `user` becomes `null`
4. Protected route redirects to login

---

### 2. **User Opens Multiple Tabs**

**Scenario**: User logs out in Tab A, Tab B should also logout.

#### Solution with Persistence:
```typescript
// React Query persistence syncs across tabs automatically
import { PersistQueryClientProvider } from '@tanstack/react-query-persist-client';
import { createSyncStoragePersister } from '@tanstack/query-sync-storage-persister';

const persister = createSyncStoragePersister({
  storage: window.localStorage,
});

// Tabs will sync automatically
```

#### Manual Solution (without persistence):
```typescript
// Listen for storage events
useEffect(() => {
  const handleStorageChange = (e: StorageEvent) => {
    if (e.key === 'auth-logout') {
      queryClient.setQueryData(userQueryKeys.me(), null);
      router.push('/login');
    }
  };
  
  window.addEventListener('storage', handleStorageChange);
  return () => window.removeEventListener('storage', handleStorageChange);
}, []);

// On logout, trigger event:
localStorage.setItem('auth-logout', Date.now().toString());
```

---

### 3. **Backend Returns Different Response Formats**

**Scenario**: Sometimes `data.user`, sometimes `data.data.user`.

#### Solution:
```typescript
async function fetchUserProfile() {
  const response = await fetch("/api/auth/me");
  if (!response.ok) {
    if (response.status === 401) return null;
    throw new Error("Failed");
  }
  
  const data = await response.json();
  
  // Handle multiple formats
  return data.user || data.data?.user || data || null;
}
```

---

### 4. **User Refreshes Page During Login**

**Scenario**: User submits login, page refreshes before mutation completes.

#### Solution:
React Query handles this automatically:
- Mutation is lost (expected behavior)
- On page load, `useUser()` fetches current auth state
- If login succeeded on backend, user will be authenticated
- If login failed, user stays on login page

No special handling needed!

---

### 5. **Slow Network / Timeout**

**Scenario**: `/api/auth/me` takes too long to respond.

#### Solution:
```typescript
async function fetchUserProfile() {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), 10000); // 10s timeout
  
  try {
    const response = await fetch("/api/auth/me", {
      credentials: "include",
      signal: controller.signal,
    });
    
    clearTimeout(timeoutId);
    
    if (!response.ok) {
      if (response.status === 401) return null;
      throw new Error("Failed");
    }
    
    return response.json();
  } catch (error) {
    clearTimeout(timeoutId);
    if (error.name === 'AbortError') {
      console.error('Request timeout');
      return null;
    }
    throw error;
  }
}
```

---

### 6. **Backend Clears Cookie But Frontend Still Has Cache**

**Scenario**: Backend invalidates session, but React Query cache still has user.

#### Solution:
Backend should return 401, which triggers cache clear:
```typescript
// In fetchUserProfile:
if (response.status === 401) {
  return null; // Cache is updated to null
}
```

Or manually clear on specific errors:
```typescript
const { mutate: someAction } = useMutation({
  mutationFn: someApiCall,
  onError: (error) => {
    if (error.message.includes('Unauthorized')) {
      // Clear user cache
      queryClient.setQueryData(userQueryKeys.me(), null);
      router.push('/login');
    }
  }
});
```

---

### 7. **User Has Old Cache After Code Update**

**Scenario**: You deploy new code, user has old cached data.

#### Solution:
Add version to query keys:
```typescript
export const userQueryKeys = {
  all: ["user", "v2"] as const, // Increment version on breaking changes
  me: () => [...userQueryKeys.all, "me"] as const,
};
```

Or clear cache on app version change:
```typescript
const APP_VERSION = "1.0.0";

useEffect(() => {
  const cachedVersion = localStorage.getItem("app-version");
  if (cachedVersion !== APP_VERSION) {
    queryClient.clear(); // Clear all caches
    localStorage.setItem("app-version", APP_VERSION);
  }
}, []);
```

---

### 8. **Protected Route Flashes Before Redirect**

**Scenario**: User sees protected content for a split second before redirect.

#### Solution:
Show loading state while checking auth:
```typescript
const { data: user, isLoading } = useUser();

if (isLoading) {
  return <LoadingSpinner />; // Don't show content while loading
}

if (!user) {
  router.push("/login");
  return null; // Don't show content while redirecting
}

return <ProtectedContent />;
```

---

### 9. **Login Succeeds But User is Still Null**

**Scenario**: Login mutation succeeds, but `useUser()` still returns null.

#### Possible Causes:
1. Cookie not being set correctly
2. Cookie not being sent with requests
3. Backend not returning user in response

#### Debug Steps:
```typescript
// 1. Check mutation response
const { mutate: login } = useLoginMutation({
  onSuccess: (data) => {
    console.log("Login response:", data); // Check structure
    console.log("User:", data.user || data.data?.user);
  }
});

// 2. Check cookie in DevTools
// Application → Cookies → Check for "at" cookie

// 3. Check network request
// Network → /api/auth/me → Check if cookie is sent

// 4. Manually refetch after login
const refetchUser = useRefetchUser();
const { mutate: login } = useLoginMutation({
  onSuccess: async () => {
    await refetchUser(); // Force refetch
  }
});
```

---

### 10. **TypeScript Errors After Migration**

**Scenario**: TypeScript complains about types after removing Zustand.

#### Solution:
```typescript
// If you get "Property 'user' does not exist on type 'UseQueryResult'"
// Make sure you're destructuring correctly:

// ❌ Wrong
const user = useUser();

// ✅ Correct
const { data: user } = useUser();

// If you get "Type 'User | undefined' is not assignable to type 'User'"
// Add null check:
if (!user) return null;
// Now TypeScript knows user is not undefined
```

---

## 🧪 Testing Checklist

After migration, test these scenarios:

- [ ] **Happy Path**
  - [ ] Login works
  - [ ] Logout works
  - [ ] Registration works
  - [ ] Protected routes work
  - [ ] Admin routes work

- [ ] **Edge Cases**
  - [ ] Page refresh maintains auth
  - [ ] Token expiration logs out user
  - [ ] Network error doesn't break app
  - [ ] Multiple tabs sync (if using persistence)
  - [ ] Slow network doesn't cause issues

- [ ] **Error Cases**
  - [ ] Invalid credentials show error
  - [ ] Network error shows error
  - [ ] 401 redirects to login
  - [ ] 403 shows unauthorized page

- [ ] **Performance**
  - [ ] No infinite loops
  - [ ] No unnecessary refetches
  - [ ] Loading states work correctly

---

## 🆘 Debugging Tips

### 1. **Use React Query DevTools**
```typescript
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';

<ReactQueryDevtools initialIsOpen={false} />
```

Check:
- Query status (loading, success, error)
- Cache data
- Refetch behavior

### 2. **Check Browser DevTools**
- **Network tab**: See API calls and responses
- **Application tab**: Check cookies and localStorage
- **Console**: Check for errors

### 3. **Add Debug Logging**
```typescript
const { data: user, isLoading, error } = useUser();

useEffect(() => {
  console.log("Auth state:", { user, isLoading, error });
}, [user, isLoading, error]);
```

### 4. **Test in Incognito Mode**
- No cached data
- Fresh start
- Easier to reproduce issues

---

## 📞 Need More Help?

If you encounter an issue not covered here:
1. Check React Query docs: https://tanstack.com/query/latest
2. Check your `AUTH_ANALYSIS.md` for backend details
3. Use React Query DevTools to inspect state
4. Add console.logs to trace data flow
5. Test in isolation (one component at a time)

