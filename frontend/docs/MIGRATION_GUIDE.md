# 🚀 Authentication Migration Guide: Zustand → React Query

## 📋 Overview

This guide walks you through migrating from Zustand to React Query for authentication state management.

---

## ✅ What's Been Created

### New Files:
1. **`src/entities/user/api/auth-hooks.ts`** - Main React Query auth hooks
2. **`src/features/auth/login/api/use-login-v2.tsx`** - New login mutation
3. **`src/features/auth/logout/api/use-logout-v2.tsx`** - New logout mutation
4. **`src/features/auth/register/api/use-register-v2.tsx`** - New register mutation

### Updated Files:
1. **`src/entities/user/index.ts`** - Exports new hooks

---

## 🔄 Migration Steps

### **STEP 1: Update Components to Use New Hooks**

#### Before (Zustand):
```typescript
import { useAuthStore } from "@/entities/user/model/store";

const Component = () => {
  const { user, isAuthenticated, isLoading } = useAuthStore();
  const logout = useAuthStore(s => s.logout);
  const isAdmin = useAuthStore(s => s.isAdmin());
  const userName = useAuthStore(s => s.getUserName());
  
  // ...
};
```

#### After (React Query):
```typescript
import { 
  useUser, 
  useIsAuthenticated, 
  useIsAdmin,
  useUserName,
  useAuthLoading 
} from "@/entities/user";
import { useLogout } from "@/entities/user";

const Component = () => {
  const { data: user } = useUser();
  const isAuthenticated = useIsAuthenticated();
  const isLoading = useAuthLoading();
  const isAdmin = useIsAdmin();
  const userName = useUserName();
  const logout = useLogout();
  
  const handleLogout = () => {
    logout.mutate();
  };
  
  // ...
};
```

---

### **STEP 2: Update Login Components**

#### Before:
```typescript
import { useLoginMution } from "@/features/auth/login/api/use-login";

const { mutate: login } = useLoginMution({
  onSuccess: (data) => {
    console.log("Logged in");
  }
});
```

#### After:
```typescript
import { useLoginMutation } from "@/features/auth/login/api/use-login-v2";
import { useRouter } from "next/navigation";

const router = useRouter();
const { mutate: login, isPending } = useLoginMutation({
  onSuccess: () => {
    router.push("/dashboard");
  },
  onError: (error) => {
    console.error(error.message);
  }
});
```

---

### **STEP 3: Update Logout Components**

#### Before:
```typescript
import { useLogoutMution } from "@/features/auth/logout/api/use-logout";

const { mutate: logout } = useLogoutMution({
  onSuccess: () => {
    window.location.reload();
  }
});
```

#### After:
```typescript
import { useLogoutMutation } from "@/features/auth/logout/api/use-logout-v2";
import { useRouter } from "next/navigation";

const router = useRouter();
const { mutate: logout, isPending } = useLogoutMutation({
  onSuccess: () => {
    router.push("/login");
  }
});
```

---

### **STEP 4: Update Protected Routes**

#### Before:
```typescript
"use client";
import { useAuthStore } from "@/entities/user/model/store";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

export default function ProtectedPage() {
  const { isAuthenticated, isLoading } = useAuthStore();
  const router = useRouter();
  
  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.push("/login");
    }
  }, [isAuthenticated, isLoading, router]);
  
  if (isLoading) return <div>Loading...</div>;
  
  return <div>Protected Content</div>;
}
```

#### After:
```typescript
"use client";
import { useUser } from "@/entities/user";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

export default function ProtectedPage() {
  const { data: user, isLoading } = useUser();
  const router = useRouter();
  
  useEffect(() => {
    if (!isLoading && !user) {
      router.push("/login");
    }
  }, [user, isLoading, router]);
  
  if (isLoading) {
    return <div>Loading...</div>;
  }
  
  if (!user) {
    return null; // Will redirect
  }
  
  return <div>Protected Content</div>;
}
```

---

### **STEP 5: Create Protected Route HOC (Optional)**

```typescript
// src/shared/hoc/with-auth.tsx
"use client";

import { useUser } from "@/entities/user";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

export function withAuth<P extends object>(
  Component: React.ComponentType<P>,
  options?: { redirectTo?: string; requireAdmin?: boolean }
) {
  return function ProtectedComponent(props: P) {
    const { data: user, isLoading } = useUser();
    const router = useRouter();
    const redirectTo = options?.redirectTo || "/login";
    const requireAdmin = options?.requireAdmin || false;

    useEffect(() => {
      if (!isLoading) {
        if (!user) {
          router.push(redirectTo);
        } else if (requireAdmin && user.role !== "ADMIN") {
          router.push("/unauthorized");
        }
      }
    }, [user, isLoading, router]);

    if (isLoading) {
      return (
        <div className="flex items-center justify-center min-h-screen">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
        </div>
      );
    }

    if (!user || (requireAdmin && user.role !== "ADMIN")) {
      return null;
    }

    return <Component {...props} />;
  };
}

// Usage:
// export default withAuth(DashboardPage);
// export default withAuth(AdminPage, { requireAdmin: true });
```

---

### **STEP 6: Update Header/Sidebar Components**

See the example files:
- `src/widgets/header/ui/header.tsx` 
- `src/widgets/sidebar/ui/sidebar.tsx`

Replace all `useAuthStore()` calls with the new hooks.

---

### **STEP 7: Optional - Add React Query Persistence**

If you want to persist the user cache across page refreshes (like Zustand did):

```bash
npm install @tanstack/react-query-persist-client @tanstack/query-sync-storage-persister
```

Update `src/app/provider/query-client-provider.tsx`:

```typescript
'use client';

import { QueryClient } from '@tanstack/react-query';
import { PersistQueryClientProvider } from '@tanstack/react-query-persist-client';
import { createSyncStoragePersister } from '@tanstack/query-sync-storage-persister';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { useState } from 'react';

export function QueryProviders({ children }: { children: React.ReactNode }) {
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: {
            staleTime: 5 * 60 * 1000,
            gcTime: 10 * 60 * 1000,
          },
        },
      })
  );

  const [persister] = useState(() =>
    createSyncStoragePersister({
      storage: typeof window !== 'undefined' ? window.localStorage : undefined,
    })
  );

  return (
    <PersistQueryClientProvider
      client={queryClient}
      persistOptions={{ persister }}
    >
      {children}
      <ReactQueryDevtools initialIsOpen={false} />
    </PersistQueryClientProvider>
  );
}
```

---

### **STEP 8: Remove Zustand (After Testing)**

Once everything works:

1. Delete `src/entities/user/model/store.ts`
2. Delete `src/entities/user/api/queries.ts` (old version)
3. Rename `-v2` files to remove the suffix
4. Uninstall Zustand: `npm uninstall zustand`

---

## 🎯 Key Differences

| Feature | Zustand | React Query |
|---------|---------|-------------|
| **State Location** | localStorage + memory | React Query cache (+ optional persistence) |
| **Loading State** | Manual (`isLoading`) | Automatic (`isLoading`, `isFetching`) |
| **Error Handling** | Manual (`error` state) | Automatic (`error` from query) |
| **Refetching** | Manual | Automatic (on focus, reconnect, interval) |
| **Cache Invalidation** | Manual sync | Automatic with `invalidateQueries` |
| **Token Storage** | ❌ localStorage (insecure) | ✅ HTTP-only cookie only |

---

## ⚠️ Potential Pitfalls

### 1. **Loading State Timing**
- React Query's `isLoading` is only `true` on first fetch
- Use `isFetching` if you need to show loading on refetch
- Use `isAuthLoading()` helper for combined state

### 2. **Logout Behavior**
- Old: `logout()` was synchronous
- New: `logout.mutate()` is async
- Use `logout.mutateAsync()` if you need to await

### 3. **Protected Routes**
- Check both `isLoading` AND `user` before redirecting
- Don't redirect while loading (causes flash)

### 4. **Optimistic Updates**
- Login/Register automatically update cache
- No need for manual `setUser()` calls

---

## 🧪 Testing Checklist

- [ ] Login flow works
- [ ] Logout flow works
- [ ] Registration flow works
- [ ] Protected routes redirect when not authenticated
- [ ] Admin routes check role correctly
- [ ] Page refresh maintains auth state
- [ ] Token expiration clears auth state
- [ ] Network errors don't break auth
- [ ] Multiple tabs sync auth state (if using persistence)

---

## 📚 Additional Resources

- [React Query Docs](https://tanstack.com/query/latest)
- [React Query Persistence](https://tanstack.com/query/latest/docs/react/plugins/persistQueryClient)
- [Your AUTH_ANALYSIS.md](./AUTH_ANALYSIS.md)

---

## 🆘 Need Help?

If you encounter issues during migration:
1. Check the console for errors
2. Use React Query DevTools to inspect cache
3. Compare with examples in this guide
4. Check that cookies are being sent (`credentials: "include"`)

