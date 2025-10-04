# 🚀 React Query Authentication System

**Complete migration from Zustand to React Query for authentication state management.**

---

## 📚 Documentation Index

This migration includes comprehensive documentation. Start here:

### **🎯 Start Here**
1. **[REACT_QUERY_AUTH_SUMMARY.md](./REACT_QUERY_AUTH_SUMMARY.md)** - Overview and quick start
2. **[ARCHITECTURE_COMPARISON.md](./ARCHITECTURE_COMPARISON.md)** - Visual comparison of old vs new

### **📖 Migration Guides**
3. **[MIGRATION_GUIDE.md](./MIGRATION_GUIDE.md)** - Step-by-step migration instructions
4. **[SEARCH_AND_REPLACE_GUIDE.md](./SEARCH_AND_REPLACE_GUIDE.md)** - Quick find/replace patterns
5. **[COMPONENT_MIGRATION_EXAMPLES.md](./COMPONENT_MIGRATION_EXAMPLES.md)** - Real component examples

### **⚠️ Important**
6. **[PITFALLS_AND_EDGE_CASES.md](./PITFALLS_AND_EDGE_CASES.md)** - Common mistakes and how to avoid them

---

## ✨ What's New

### **New Files Created**

#### **Core Hooks**
- `src/entities/user/api/auth-hooks.ts` - Main React Query auth hooks

#### **Updated Mutations**
- `src/features/auth/login/api/use-login-v2.tsx` - Login without Zustand
- `src/features/auth/logout/api/use-logout-v2.tsx` - Logout without Zustand
- `src/features/auth/register/api/use-register-v2.tsx` - Register without Zustand

#### **Updated Exports**
- `src/entities/user/index.ts` - Exports new hooks

---

## 🎯 Quick Start

### **1. Review the Architecture**

Read [ARCHITECTURE_COMPARISON.md](./ARCHITECTURE_COMPARISON.md) to understand the changes.

**TL;DR:**
- **Before**: Zustand + React Query (redundant, complex)
- **After**: React Query only (simple, clean)

---

### **2. Install Optional Dependencies**

For cache persistence across page refreshes (recommended):

```bash
npm install @tanstack/react-query-persist-client @tanstack/query-sync-storage-persister
```

---

### **3. Update Query Provider**

Replace `src/app/provider/query-client-provider.tsx`:

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

### **4. Start Migrating Components**

Follow [MIGRATION_GUIDE.md](./MIGRATION_GUIDE.md) for detailed instructions.

**Quick example:**

```typescript
// ❌ Before (Zustand)
import { useAuthStore } from "@/entities/user/model/store";
const { user, isAuthenticated, isLoading } = useAuthStore();

// ✅ After (React Query)
import { useUser, useIsAuthenticated, useAuthLoading } from "@/entities/user";
const { data: user } = useUser();
const isAuthenticated = useIsAuthenticated();
const isLoading = useAuthLoading();
```

---

## 📦 Available Hooks

### **Query Hooks** (Read State)

```typescript
import {
  useUser,              // Get current user
  useIsAuthenticated,   // Check if authenticated
  useIsAdmin,           // Check if admin
  useUserName,          // Get user name
  useUserInitials,      // Get user initials
  useAuthLoading,       // Get loading state
  useAuthError,         // Get error state
} from "@/entities/user";
```

### **Mutation Hooks** (Modify State)

```typescript
import { useLoginMutation } from "@/features/auth/login/api/use-login-v2";
import { useLogoutMutation } from "@/features/auth/logout/api/use-logout-v2";
import { useRegisterMutation } from "@/features/auth/register/api/use-register-v2";
```

### **Utility Hooks**

```typescript
import { useRefetchUser, userQueryKeys } from "@/entities/user";
```

---

## 🔄 Migration Workflow

### **Phase 1: Preparation** ✅ DONE
- [x] New hooks created
- [x] Documentation written
- [x] Examples provided

### **Phase 2: Update Components** 👈 YOU ARE HERE
1. Read [MIGRATION_GUIDE.md](./MIGRATION_GUIDE.md)
2. Read [COMPONENT_MIGRATION_EXAMPLES.md](./COMPONENT_MIGRATION_EXAMPLES.md)
3. Use [SEARCH_AND_REPLACE_GUIDE.md](./SEARCH_AND_REPLACE_GUIDE.md) for quick updates
4. Update components one by one
5. Test each component after updating

### **Phase 3: Testing**
1. Test all auth flows (login, logout, register)
2. Test protected routes
3. Test admin routes
4. Test page refresh
5. Test token expiration
6. Test network errors

### **Phase 4: Cleanup**
1. Delete old files
2. Rename `-v2` files
3. Uninstall Zustand
4. Update documentation

---

## 🎓 Learning Path

### **For Beginners**
1. Start with [REACT_QUERY_AUTH_SUMMARY.md](./REACT_QUERY_AUTH_SUMMARY.md)
2. Read [ARCHITECTURE_COMPARISON.md](./ARCHITECTURE_COMPARISON.md)
3. Follow [MIGRATION_GUIDE.md](./MIGRATION_GUIDE.md) step by step
4. Use [COMPONENT_MIGRATION_EXAMPLES.md](./COMPONENT_MIGRATION_EXAMPLES.md) as reference

### **For Experienced Developers**
1. Skim [ARCHITECTURE_COMPARISON.md](./ARCHITECTURE_COMPARISON.md)
2. Use [SEARCH_AND_REPLACE_GUIDE.md](./SEARCH_AND_REPLACE_GUIDE.md) for bulk updates
3. Reference [PITFALLS_AND_EDGE_CASES.md](./PITFALLS_AND_EDGE_CASES.md) when stuck

---

## 🧪 Testing Checklist

Copy this checklist and track your progress:

### **Basic Flows**
- [ ] Login with valid credentials
- [ ] Login with invalid credentials (shows error)
- [ ] Logout (redirects to login)
- [ ] Register new user
- [ ] Access protected route when authenticated
- [ ] Redirect to login when not authenticated

### **Edge Cases**
- [ ] Page refresh maintains auth state
- [ ] Token expiration logs out user
- [ ] Network error doesn't break app
- [ ] Multiple tabs sync (if using persistence)
- [ ] Slow network shows loading state

### **Admin Features**
- [ ] Admin can access admin routes
- [ ] Non-admin cannot access admin routes
- [ ] Admin badge/indicator shows correctly

### **UI/UX**
- [ ] Loading spinners show during auth checks
- [ ] Error messages display correctly
- [ ] No flash of wrong content
- [ ] Smooth transitions (no page reloads)

---

## 🐛 Troubleshooting

### **Common Issues**

| Issue | Solution |
|-------|----------|
| User not persisting on refresh | Install persistence packages (see Quick Start #2) |
| Infinite redirects | Check `isLoading` before redirecting |
| "useAuthStore is not defined" | Update that component (see Migration Guide) |
| Login works but user is null | Check backend response format |
| TypeScript errors | Make sure to destructure `data` from `useUser()` |

**Full troubleshooting guide**: [PITFALLS_AND_EDGE_CASES.md](./PITFALLS_AND_EDGE_CASES.md)

---

## 📊 Migration Progress Tracker

Track which files you've updated:

### **Components**
- [ ] `src/widgets/header/ui/header.tsx`
- [ ] `src/widgets/sidebar/ui/sidebar.tsx`
- [ ] `src/features/auth/login/ui/login-form.tsx`
- [ ] Other components (list them as you find them)

### **Mutations**
- [ ] Replace `use-login.tsx` with `use-login-v2.tsx`
- [ ] Replace `use-logout.tsx` with `use-logout-v2.tsx`
- [ ] Replace `use-register.tsx` with `use-register-v2.tsx`

### **Cleanup**
- [ ] Delete `src/entities/user/model/store.ts`
- [ ] Delete `src/entities/user/api/queries.ts`
- [ ] Delete old mutation files
- [ ] Rename `-v2` files
- [ ] Uninstall Zustand

---

## 🎉 Benefits

After migration, you'll have:

✅ **Simpler Code** - 67% less boilerplate  
✅ **Better Security** - No token in localStorage  
✅ **Better Performance** - Automatic caching and deduplication  
✅ **Better DX** - React Query DevTools for debugging  
✅ **Automatic Updates** - Refetch on focus, reconnect, interval  
✅ **Type Safety** - Full TypeScript support  
✅ **Easier Testing** - Pure functions, no global state  

---

## 📞 Support

### **Documentation**
- [React Query Docs](https://tanstack.com/query/latest)
- [React Query Persistence](https://tanstack.com/query/latest/docs/react/plugins/persistQueryClient)

### **Your Docs**
- All markdown files in this directory
- Your existing `AUTH_ANALYSIS.md`

### **Debugging**
- Use React Query DevTools
- Check browser DevTools (Network, Application, Console)
- Add console.logs to trace data flow

---

## 🚀 Next Steps

1. **Read** [REACT_QUERY_AUTH_SUMMARY.md](./REACT_QUERY_AUTH_SUMMARY.md)
2. **Install** persistence packages (optional but recommended)
3. **Update** Query Provider
4. **Migrate** components one by one
5. **Test** thoroughly
6. **Clean up** old files
7. **Celebrate** 🎉

---

## 📝 Notes

- **Incremental Migration**: You can migrate one component at a time. Old and new can coexist temporarily.
- **Rollback**: Keep old files until you're confident everything works.
- **Testing**: Test each component after updating before moving to the next.
- **Questions**: Refer to [PITFALLS_AND_EDGE_CASES.md](./PITFALLS_AND_EDGE_CASES.md) for common issues.

---

## 🏆 Success Criteria

You'll know the migration is complete when:

- [ ] No `useAuthStore` imports in codebase
- [ ] All auth flows work correctly
- [ ] All tests pass
- [ ] No TypeScript errors
- [ ] Zustand is uninstalled
- [ ] Old files are deleted

---

**Good luck with your migration! 🚀**

If you get stuck, refer to the documentation files listed at the top of this README.

