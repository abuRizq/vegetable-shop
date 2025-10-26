# Search and Replace Guide for Migration

This guide helps you quickly find and replace Zustand patterns with React Query patterns.

---

## 🔍 Step 1: Find All Files Using Zustand

Run this command in your terminal:

```bash
# Find all files importing useAuthStore
grep -r "useAuthStore" src/ --include="*.tsx" --include="*.ts"
```

Expected files to update:
- `src/widgets/header/ui/header.tsx`
- `src/widgets/sidebar/ui/sidebar.tsx`
- `src/features/auth/login/ui/login-form.tsx`
- `src/features/auth/login/api/use-login.tsx`
- `src/features/auth/logout/api/use-logout.tsx`
- `src/entities/user/api/queries.ts`
- Any other components using auth

---

## 🔄 Step 2: Common Replacements

### **Import Statements**

#### Find:
```typescript
import { useAuthStore } from "@/entities/user/model/store";
```

#### Replace with:
```typescript
import { useUser, useIsAuthenticated, useAuthLoading } from "@/entities/user";
```

Add other hooks as needed:
- `useIsAdmin` - for admin checks
- `useUserName` - for displaying name
- `useUserInitials` - for avatar initials

---

### **Getting User Data**

#### Find:
```typescript
const { user, isAuthenticated, isLoading } = useAuthStore();
```

#### Replace with:
```typescript
const { data: user, isLoading } = useUser();
const isAuthenticated = useIsAuthenticated();
```

---

### **Getting User Name**

#### Find:
```typescript
const userName = useAuthStore(s => s.getUserName());
// or
const { getUserName } = useAuthStore();
const userName = getUserName();
```

#### Replace with:
```typescript
const userName = useUserName();
```

---

### **Getting User Initials**

#### Find:
```typescript
const initials = useAuthStore(s => s.getUserInitials());
// or
const { getUserInitials } = useAuthStore();
const initials = getUserInitials();
```

#### Replace with:
```typescript
const initials = useUserInitials();
```

---

### **Checking Admin Status**

#### Find:
```typescript
const isAdmin = useAuthStore(s => s.isAdmin());
// or
const { isAdmin } = useAuthStore();
const adminStatus = isAdmin();
```

#### Replace with:
```typescript
const isAdmin = useIsAdmin();
```

---

### **Logout Action**

#### Find:
```typescript
const { logout } = useAuthStore();
// Later in code:
logout();
```

#### Replace with:
```typescript
import { useLogoutMutation } from "@/features/auth/logout/api/use-logout-v2";

const { mutate: logout } = useLogoutMutation({
  onSuccess: () => {
    router.push("/login");
  }
});
// Later in code:
logout();
```

---

### **Login Mutation**

#### Find:
```typescript
import { useLoginMution } from "@/features/auth/login/api/use-login";

const { mutate: login } = useLoginMution({
  onSuccess: (data) => {
    console.log(data);
  }
});
```

#### Replace with:
```typescript
import { useLoginMutation } from "@/features/auth/login/api/use-login-v2";

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

### **Logout Mutation**

#### Find:
```typescript
import { useLogoutMution } from "@/features/auth/logout/api/use-logout";

const { mutate: logout } = useLogoutMution({
  onSuccess: () => {
    window.location.reload();
  }
});
```

#### Replace with:
```typescript
import { useLogoutMutation } from "@/features/auth/logout/api/use-logout-v2";

const { mutate: logout } = useLogoutMutation({
  onSuccess: () => {
    router.push("/login");
  }
});
```

---

### **Register Mutation**

#### Find:
```typescript
import { useRegisterMution } from "@/features/auth/register/api/use-register";

const { mutate: register } = useRegisterMution({
  onSuccess: (data) => {
    console.log(data);
  }
});
```

#### Replace with:
```typescript
import { useRegisterMutation } from "@/features/auth/register/api/use-register-v2";

const { mutate: register, isPending } = useRegisterMutation({
  onSuccess: () => {
    router.push("/dashboard");
  },
  onError: (error) => {
    console.error(error.message);
  }
});
```

---

### **Loading States in Mutations**

#### Find:
```typescript
const { startLoading, stopLoading } = useAuthStore();

// In mutation:
startLoading();
// ... do something
stopLoading();
```

#### Replace with:
```typescript
// React Query handles this automatically
const { mutate: login, isPending } = useLoginMutation();

// Use isPending in your UI:
{isPending && <Spinner />}
```

---

### **Error Handling**

#### Find:
```typescript
const { error, setError, clearError } = useAuthStore();

// Later:
setError("Something went wrong");
```

#### Replace with:
```typescript
// React Query handles this automatically
const { error } = useUser();

// Or in mutations:
const { mutate: login, error: loginError } = useLoginMutation();
```

---

### **Manual Refetch**

#### Find:
```typescript
const { refetch } = useUserProfile();
// or
queryClient.invalidateQueries({ queryKey: ["user"] });
```

#### Replace with:
```typescript
import { useRefetchUser, userQueryKeys } from "@/entities/user";

const refetchUser = useRefetchUser();
// Later:
refetchUser();

// Or with queryClient:
queryClient.invalidateQueries({ queryKey: userQueryKeys.me() });
```

---

## 🎯 Step 3: File-Specific Patterns

### **Header Component**

```bash
# Find the file
src/widgets/header/ui/header.tsx
```

**Changes needed:**
1. Replace `useAuthStore` import with `useUser`, `useIsAuthenticated`, `useUserName`, `useUserInitials`
2. Replace `useLogoutMution` with `useLogoutMutation` (v2)
3. Update logout handler to use `logout.mutate()`
4. Remove `window.location.reload()` - use `router.push()` instead

---

### **Sidebar Component**

```bash
# Find the file
src/widgets/sidebar/ui/sidebar.tsx
```

**Changes needed:**
1. Replace `useAuthStore` import with `useUser`
2. Replace `const { user } = useAuthStore()` with `const { data: user } = useUser()`
3. Update logout logic if present

---

### **Login Form**

```bash
# Find the file
src/features/auth/login/ui/login-form.tsx
```

**Changes needed:**
1. Replace `useAuthStore` import with `useIsAuthenticated`
2. Replace `useLoginMution` with `useLoginMutation` (v2)
3. Use `isPending` from mutation instead of `isSubmitting`
4. Update success handler to navigate with router

---

### **Mutation Files**

```bash
# Files to update:
src/features/auth/login/api/use-login.tsx
src/features/auth/logout/api/use-logout.tsx
src/features/auth/register/api/use-register.tsx
```

**Option 1: Replace with v2 versions**
- Delete old files
- Rename `-v2` files to remove suffix

**Option 2: Update in place**
- Remove all `useAuthStore` imports
- Remove all Zustand state management
- Keep only React Query cache updates

---

## 🧪 Step 4: Testing Each Change

After updating each file:

1. **Check TypeScript errors:**
   ```bash
   npm run type-check
   # or
   npx tsc --noEmit
   ```

2. **Check for remaining Zustand usage:**
   ```bash
   grep -n "useAuthStore" src/path/to/file.tsx
   ```

3. **Test in browser:**
   - Login flow
   - Logout flow
   - Protected routes
   - Page refresh

---

## 📋 Step 5: Final Cleanup Checklist

After all components are updated:

- [ ] No more `useAuthStore` imports:
  ```bash
  grep -r "useAuthStore" src/ --include="*.tsx" --include="*.ts"
  # Should return no results
  ```

- [ ] No more old mutation imports:
  ```bash
  grep -r "useLoginMution\|useLogoutMution\|useRegisterMution" src/
  # Should return no results (note: Mution not Mutation)
  ```

- [ ] Delete Zustand store:
  ```bash
  rm src/entities/user/model/store.ts
  ```

- [ ] Delete old queries file:
  ```bash
  rm src/entities/user/api/queries.ts
  ```

- [ ] Rename v2 files:
  ```bash
  mv src/features/auth/login/api/use-login-v2.tsx src/features/auth/login/api/use-login.tsx
  mv src/features/auth/logout/api/use-logout-v2.tsx src/features/auth/logout/api/use-logout.tsx
  mv src/features/auth/register/api/use-register-v2.tsx src/features/auth/register/api/use-register.tsx
  ```

- [ ] Uninstall Zustand:
  ```bash
  npm uninstall zustand
  ```

- [ ] Update entity exports (remove legacy):
  ```typescript
  // src/entities/user/index.ts
  // Remove this line:
  export { useUserProfile } from './api/queries'
  ```

---

## 🚨 Common Mistakes to Avoid

### ❌ Mistake 1: Not destructuring `data`
```typescript
// Wrong
const user = useUser();

// Correct
const { data: user } = useUser();
```

### ❌ Mistake 2: Redirecting while loading
```typescript
// Wrong - redirects immediately
if (!user) router.push("/login");

// Correct - waits for loading to finish
if (!isLoading && !user) router.push("/login");
```

### ❌ Mistake 3: Using old mutation names
```typescript
// Wrong - old name
import { useLoginMution } from "...";

// Correct - new name
import { useLoginMutation } from "...";
```

### ❌ Mistake 4: Calling logout synchronously
```typescript
// Wrong - logout is now async
const { logout } = useAuthStore();
logout();

// Correct
const { mutate: logout } = useLogoutMutation();
logout();
```

---

## 💡 Pro Tips

1. **Update one file at a time** - easier to debug
2. **Test after each change** - catch issues early
3. **Use TypeScript errors as a guide** - they'll show what needs updating
4. **Keep React Query DevTools open** - see cache updates in real-time
5. **Check Network tab** - verify API calls are working

---

## 🆘 Need Help?

If you get stuck:
1. Check `COMPONENT_MIGRATION_EXAMPLES.md` for real examples
2. Check `MIGRATION_GUIDE.md` for detailed explanations
3. Use React Query DevTools to inspect cache
4. Check browser console for errors

