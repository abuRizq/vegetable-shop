# Architecture Comparison: Before vs After

## 🏗️ OLD ARCHITECTURE (Zustand + React Query)

```
┌─────────────────────────────────────────────────────────────┐
│                        FRONTEND                              │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                    Components                         │  │
│  │  (Header, Sidebar, LoginForm, etc.)                  │  │
│  └────────┬─────────────────────────────────┬───────────┘  │
│           │                                  │               │
│           │ Read State                       │ Actions       │
│           ▼                                  ▼               │
│  ┌─────────────────┐              ┌──────────────────┐     │
│  │  Zustand Store  │◄─────────────│ React Query      │     │
│  │  (localStorage) │  Manual Sync │ (cache)          │     │
│  │                 │              │                  │     │
│  │ • user          │              │ • useUserProfile │     │
│  │ • isAuth        │              │ • useLogin       │     │
│  │ • isLoading     │              │ • useLogout      │     │
│  │ • error         │              │                  │     │
│  │ • token ❌      │              │                  │     │
│  └─────────────────┘              └────────┬─────────┘     │
│                                             │                │
└─────────────────────────────────────────────┼───────────────┘
                                              │
                                              │ HTTP Requests
                                              ▼
                                    ┌──────────────────┐
                                    │  Next.js API     │
                                    │  Routes          │
                                    │  /api/auth/*     │
                                    └────────┬─────────┘
                                             │
                                             │ Proxy
                                             ▼
                                    ┌──────────────────┐
                                    │  Backend API     │
                                    │  (Spring Boot)   │
                                    │                  │
                                    │  • /auth/login   │
                                    │  • /auth/logout  │
                                    │  • /users/me     │
                                    └──────────────────┘
```

### **Problems:**
1. ❌ **Redundant State**: User data stored in both Zustand and React Query
2. ❌ **Manual Syncing**: Need to manually sync Zustand ↔ React Query
3. ❌ **Hydration Issues**: Zustand localStorage hydration timing problems
4. ❌ **Security Risk**: Token stored in localStorage
5. ❌ **Complex Logic**: `useAuthInit` hook with manual orchestration
6. ❌ **More Code**: More boilerplate for state management

---

## 🎯 NEW ARCHITECTURE (React Query Only)

```
┌─────────────────────────────────────────────────────────────┐
│                        FRONTEND                              │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                    Components                         │  │
│  │  (Header, Sidebar, LoginForm, etc.)                  │  │
│  └────────┬─────────────────────────────────────────────┘  │
│           │                                                  │
│           │ Read State & Actions                            │
│           ▼                                                  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │              React Query (Single Source)             │  │
│  │                                                       │  │
│  │  Hooks:                        Mutations:            │  │
│  │  • useUser()                   • useLoginMutation    │  │
│  │  • useIsAuthenticated()        • useLogoutMutation   │  │
│  │  • useIsAdmin()                • useRegisterMutation │  │
│  │  • useUserName()                                     │  │
│  │  • useUserInitials()                                 │  │
│  │                                                       │  │
│  │  Cache: { user: {...} }                              │  │
│  │  Optional: Persist to localStorage                   │  │
│  └────────────────────────┬─────────────────────────────┘  │
│                            │                                 │
└────────────────────────────┼─────────────────────────────────┘
                             │
                             │ HTTP Requests
                             ▼
                   ┌──────────────────┐
                   │  Next.js API     │
                   │  Routes          │
                   │  /api/auth/*     │
                   │                  │
                   │  • Sets cookie   │
                   │  • Clears cookie │
                   └────────┬─────────┘
                            │
                            │ Proxy
                            ▼
                   ┌──────────────────┐
                   │  Backend API     │
                   │  (Spring Boot)   │
                   │                  │
                   │  • /auth/login   │
                   │  • /auth/logout  │
                   │  • /users/me     │
                   └──────────────────┘

                   ┌──────────────────┐
                   │  HTTP-only       │
                   │  Cookie          │
                   │  (Secure) ✅     │
                   └──────────────────┘
```

### **Benefits:**
1. ✅ **Single Source of Truth**: Only React Query manages state
2. ✅ **No Manual Syncing**: React Query handles everything
3. ✅ **No Hydration Issues**: React Query handles persistence properly
4. ✅ **Secure**: Token only in HTTP-only cookie
5. ✅ **Simpler Logic**: No complex initialization hooks
6. ✅ **Less Code**: Less boilerplate, cleaner components

---

## 📊 Data Flow Comparison

### **OLD: Login Flow**

```
User submits form
    ↓
useLoginMution (React Query)
    ↓
POST /api/auth/login
    ↓
Backend returns { user, token }
    ↓
Next.js sets cookie
    ↓
Mutation onSuccess:
    ├─ setUser(user, token) → Zustand ❌ Manual
    ├─ clearError() → Zustand ❌ Manual
    ├─ queryClient.setQueryData() → React Query ❌ Manual
    └─ queryClient.invalidateQueries() → React Query ❌ Manual
    ↓
Zustand stores to localStorage ❌ Insecure
    ↓
Component re-renders
```

### **NEW: Login Flow**

```
User submits form
    ↓
useLoginMutation (React Query)
    ↓
POST /api/auth/login
    ↓
Backend returns { user, token }
    ↓
Next.js sets cookie ✅ Secure
    ↓
Mutation onSuccess:
    └─ queryClient.setQueryData(user) → React Query ✅ Automatic
    ↓
React Query cache updated ✅ Automatic
    ↓
Optional: Persist to localStorage ✅ Secure (no token)
    ↓
Component re-renders ✅ Automatic
```

**Difference**: 6 manual steps → 1 automatic step

---

### **OLD: Logout Flow**

```
User clicks logout
    ↓
useLogoutMution (React Query)
    ↓
POST /api/auth/logout
    ↓
Mutation onSuccess:
    ├─ clearUser() → Zustand ❌ Manual
    ├─ logout() → Zustand ❌ Manual (redundant)
    └─ stopLoading() → Zustand ❌ Manual
    ↓
window.location.reload() ❌ Harsh
```

### **NEW: Logout Flow**

```
User clicks logout
    ↓
useLogoutMutation (React Query)
    ↓
POST /api/auth/logout
    ↓
Mutation onSuccess:
    └─ queryClient.setQueryData(null) → React Query ✅ Automatic
    ↓
router.push("/login") ✅ Smooth
```

**Difference**: 4 manual steps + reload → 1 automatic step + navigation

---

### **OLD: Page Refresh**

```
Page loads
    ↓
Zustand tries to hydrate from localStorage ⏱️ Async
    ↓
useAuthInit hook runs
    ↓
shouldCheckAuth = false ❌ (hydration not done yet)
    ↓
useUserProfile doesn't run ❌
    ↓
Manual refetch in useEffect ❌
    ↓
Race condition possible ❌
```

### **NEW: Page Refresh**

```
Page loads
    ↓
React Query checks cache ✅ Automatic
    ↓
If persisted: Loads from localStorage ✅ Automatic
    ↓
useUser() hook runs ✅ Automatic
    ↓
Fetches from /api/auth/me ✅ Automatic
    ↓
Updates cache ✅ Automatic
    ↓
Component re-renders ✅ Automatic
```

**Difference**: Complex orchestration → Fully automatic

---

## 🔐 Security Comparison

### **OLD: Token Storage**

```
┌─────────────────────────────────────┐
│  localStorage                       │
│  {                                  │
│    user: {...},                     │
│    token: "eyJhbGc..." ❌ EXPOSED  │
│    isAuthenticated: true            │
│  }                                  │
└─────────────────────────────────────┘
         ↑
         │ Accessible via JavaScript
         │ Vulnerable to XSS attacks
```

### **NEW: Token Storage**

```
┌─────────────────────────────────────┐
│  HTTP-only Cookie ✅                │
│  {                                  │
│    name: "at",                      │
│    value: "eyJhbGc...",             │
│    httpOnly: true,                  │
│    secure: true,                    │
│    sameSite: "lax"                  │
│  }                                  │
└─────────────────────────────────────┘
         ↑
         │ NOT accessible via JavaScript
         │ Protected from XSS attacks

┌─────────────────────────────────────┐
│  localStorage (Optional)            │
│  {                                  │
│    user: {...} ✅ Safe              │
│    isAuthenticated: true            │
│  }                                  │
│  NO TOKEN ✅                        │
└─────────────────────────────────────┘
```

---

## 📈 Code Complexity Comparison

### **OLD: Component Code**

```typescript
// 15+ lines just for auth state
import { useAuthStore } from "@/entities/user/model/store";
import { useLogoutMution } from "@/features/auth/logout/api/use-logout";

const Component = () => {
  const { 
    user, 
    isAuthenticated, 
    isLoading,
    logout: zustandLogout,
    startLoading,
    stopLoading,
    setError 
  } = useAuthStore();
  
  const { mutate: logout } = useLogoutMution({
    onSuccess: () => {
      zustandLogout();
      stopLoading();
      window.location.reload();
    },
    onError: (error) => {
      setError(error.message);
      stopLoading();
    }
  });
  
  // ... component logic
};
```

### **NEW: Component Code**

```typescript
// 5 lines for auth state
import { useUser, useIsAuthenticated } from "@/entities/user";
import { useLogoutMutation } from "@/features/auth/logout/api/use-logout-v2";

const Component = () => {
  const { data: user, isLoading } = useUser();
  const isAuthenticated = useIsAuthenticated();
  const { mutate: logout } = useLogoutMutation({
    onSuccess: () => router.push("/login")
  });
  
  // ... component logic
};
```

**Difference**: 15+ lines → 5 lines (67% reduction)

---

## 🎯 Summary

| Aspect | OLD (Zustand + RQ) | NEW (RQ Only) |
|--------|-------------------|---------------|
| **State Management** | 2 systems | 1 system |
| **Lines of Code** | ~500+ | ~300 |
| **Manual Syncing** | Required | None |
| **Token Security** | ❌ localStorage | ✅ HTTP-only cookie |
| **Hydration Issues** | Yes | No |
| **Boilerplate** | High | Low |
| **Complexity** | High | Low |
| **Maintainability** | Medium | High |
| **Performance** | Good | Better |
| **Developer Experience** | Medium | Excellent |

---

## 🚀 Migration Impact

- **Files to Create**: 4 new files
- **Files to Update**: ~10-15 components
- **Files to Delete**: 2 old files
- **Dependencies to Remove**: 1 (zustand)
- **Dependencies to Add**: 2 (optional persistence)
- **Estimated Time**: 2-4 hours
- **Risk Level**: Low (can migrate incrementally)
- **Rollback**: Easy (keep old files until tested)

