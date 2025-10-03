# Component Migration Examples

This file shows real examples from your codebase migrated to React Query.

---

## Example 1: Header Component

### Before (Zustand):
```typescript
// src/widgets/header/ui/header.tsx
"use client"

import { useEffect, useState } from "react"
import { Bell, Settings, ChevronDown, User, ShoppingCart } from "lucide-react"
import Searchbar from "./search-bar";
import Link from "next/link"
import ThemeToggle from "../../../shared/ui/theme-toggle"
import { useRouter } from "next/navigation";
import { useLogoutMution } from "@/features/auth/logout/api/use-logout";
import { useAuthStore } from "@/entities/user/model/store";

function Header() {
  const [showProfileMenu, setShowProfileMenu] = useState(false)
  const { user, isAuthenticated } = useAuthStore();
  const router = useRouter();
  const { mutate: logout } = useLogoutMution({
    onSuccess: (data) => {
      console.log("Data is here!!: ", data)
    },
    onError: (error) => {
      console.error(error.message)
    }
  })
  const onSubmit = async () => {
    setShowProfileMenu(false);
    logout()
    window.location.reload()
  }
  useEffect(() => {
    if (!isAuthenticated) {
      router.push("/");
    }
  }, [isAuthenticated, router])
  
  // ... rest of component
}
```

### After (React Query):
```typescript
// src/widgets/header/ui/header.tsx
"use client"

import { useEffect, useState } from "react"
import { Bell, Settings, ChevronDown, User, ShoppingCart } from "lucide-react"
import Searchbar from "./search-bar";
import Link from "next/link"
import ThemeToggle from "../../../shared/ui/theme-toggle"
import { useRouter } from "next/navigation";
import { useLogoutMutation } from "@/features/auth/logout/api/use-logout-v2";
import { useUser, useIsAuthenticated, useUserName, useUserInitials } from "@/entities/user";

function Header() {
  const [showProfileMenu, setShowProfileMenu] = useState(false)
  const { data: user } = useUser();
  const isAuthenticated = useIsAuthenticated();
  const userName = useUserName();
  const userInitials = useUserInitials();
  const router = useRouter();
  
  const { mutate: logout, isPending: isLoggingOut } = useLogoutMutation({
    onSuccess: () => {
      console.log("Logged out successfully");
      router.push("/login");
    },
    onError: (error) => {
      console.error("Logout error:", error.message);
    }
  });
  
  const onSubmit = () => {
    setShowProfileMenu(false);
    logout();
  };
  
  useEffect(() => {
    if (!isAuthenticated) {
      router.push("/");
    }
  }, [isAuthenticated, router]);
  
  // ... rest of component
  // Use: userName, userInitials, isLoggingOut as needed
}
```

---

## Example 2: Sidebar Component

### Before (Zustand):
```typescript
// src/widgets/sidebar/ui/sidebar.tsx
"use client"

import { useState } from "react"
import { Home, Heart, Clock, ShoppingCart, LogOut, Star, ChevronLeft, ChevronRight, Leaf } from "lucide-react"
import NavItem from "./nav-item"
import { useRouter } from "next/navigation"
import { useAuthStore } from "@/entities/user/model/store"

function Sidebar() {
    const [isCollapsed, setIsCollapsed] = useState(false)
    const { user } = useAuthStore();
    const route = useRouter();
    
    const onSubmit = async () => {
        if (user) {
            // await logout();
        } else {
            route.push('/login')
        }
    }

    const toggleSidebar = () => {
        setIsCollapsed(!isCollapsed)
    }
    
    // ... rest of component
}
```

### After (React Query):
```typescript
// src/widgets/sidebar/ui/sidebar.tsx
"use client"

import { useState } from "react"
import { Home, Heart, Clock, ShoppingCart, LogOut, Star, ChevronLeft, ChevronRight, Leaf } from "lucide-react"
import NavItem from "./nav-item"
import { useRouter } from "next/navigation"
import { useUser } from "@/entities/user"
import { useLogoutMutation } from "@/features/auth/logout/api/use-logout-v2"

function Sidebar() {
    const [isCollapsed, setIsCollapsed] = useState(false)
    const { data: user } = useUser();
    const router = useRouter();
    const { mutate: logout } = useLogoutMutation({
        onSuccess: () => {
            router.push('/login');
        }
    });
    
    const onSubmit = () => {
        if (user) {
            logout();
        } else {
            router.push('/login');
        }
    }

    const toggleSidebar = () => {
        setIsCollapsed(!isCollapsed)
    }
    
    // ... rest of component
}
```

---

## Example 3: Login Form

### Before (Zustand):
```typescript
// src/features/auth/login/ui/login-form.tsx
"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Eye, EyeOff, Mail, Loader2, AlertCircle, Leaf, ShoppingCart} from "lucide-react"
import Link from "next/link"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod";
import { LoginFormData, LoginSchema } from "../lib/validation"
import { useLoginMution } from "../api/use-login"
import { useAuthStore } from "@/entities/user/model/store"

export const LoginForm = () => {
  const router = useRouter()
  const { isAuthenticated } = useAuthStore()
  const [showPassword, setShowPassword] = useState(false)
  const [rememberMe, setRememberMe] = useState(false)
  const [loginError, setLoginError] = useState<string | null>(null)
  
  const { mutate: login } = useLoginMution({
    onSuccess: (data , Variable,ctx ) => {
      console.log(data);
      console.log(Variable);
      console.log(ctx);
    },
    onError: (error, Varibles, ctx) => {
      console.error(error);
      console.log(Varibles);
      console.log(ctx);
    }
  })
  
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormData>({
    resolver: zodResolver(LoginSchema),
  })

  const onSubmit = async (data: LoginFormData) => {
    try {
      login(data)
    } catch (error: any) {
      setLoginError(error.message)
    }
  }
  
  useEffect(() => {
    if (isAuthenticated) {
      router.push("/home")
    }
  }, [isAuthenticated, router])
  
  // ... rest of component
}
```

### After (React Query):
```typescript
// src/features/auth/login/ui/login-form.tsx
"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { Eye, EyeOff, Mail, Loader2, AlertCircle, Leaf, ShoppingCart} from "lucide-react"
import Link from "next/link"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod";
import { LoginFormData, LoginSchema } from "../lib/validation"
import { useLoginMutation } from "../api/use-login-v2"
import { useIsAuthenticated } from "@/entities/user"

export const LoginForm = () => {
  const router = useRouter()
  const isAuthenticated = useIsAuthenticated()
  const [showPassword, setShowPassword] = useState(false)
  const [rememberMe, setRememberMe] = useState(false)
  const [loginError, setLoginError] = useState<string | null>(null)
  
  const { mutate: login, isPending } = useLoginMutation({
    onSuccess: () => {
      console.log("Login successful");
      router.push("/home");
    },
    onError: (error) => {
      console.error("Login error:", error);
      setLoginError(error.message);
    }
  })
  
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(LoginSchema),
  })

  const onSubmit = (data: LoginFormData) => {
    setLoginError(null);
    login(data);
  }
  
  useEffect(() => {
    if (isAuthenticated) {
      router.push("/home")
    }
  }, [isAuthenticated, router])
  
  // Use isPending instead of isSubmitting for loading state
  const isSubmitting = isPending;
  
  // ... rest of component
}
```

---

## Example 4: Protected Page Component

### New Pattern (React Query):
```typescript
// src/app/(main)/dashboard/page.tsx
"use client"

import { useUser, useIsAdmin } from "@/entities/user"
import { useRouter } from "next/navigation"
import { useEffect } from "react"

export default function DashboardPage() {
  const { data: user, isLoading } = useUser()
  const isAdmin = useIsAdmin()
  const router = useRouter()
  
  useEffect(() => {
    if (!isLoading && !user) {
      router.push("/login")
    }
  }, [user, isLoading, router])
  
  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary"></div>
      </div>
    )
  }
  
  if (!user) {
    return null // Will redirect
  }
  
  return (
    <div>
      <h1>Welcome, {user.name}!</h1>
      {isAdmin && <p>You have admin privileges</p>}
      {/* Dashboard content */}
    </div>
  )
}
```

---

## Example 5: Admin-Only Page

```typescript
// src/app/(main)/admin/page.tsx
"use client"

import { useUser, useIsAdmin } from "@/entities/user"
import { useRouter } from "next/navigation"
import { useEffect } from "react"

export default function AdminPage() {
  const { data: user, isLoading } = useUser()
  const isAdmin = useIsAdmin()
  const router = useRouter()
  
  useEffect(() => {
    if (!isLoading) {
      if (!user) {
        router.push("/login")
      } else if (!isAdmin) {
        router.push("/unauthorized")
      }
    }
  }, [user, isAdmin, isLoading, router])
  
  if (isLoading) {
    return <div>Loading...</div>
  }
  
  if (!user || !isAdmin) {
    return null // Will redirect
  }
  
  return (
    <div>
      <h1>Admin Panel</h1>
      {/* Admin content */}
    </div>
  )
}
```

---

## Example 6: User Profile Display

```typescript
// src/components/user-profile.tsx
"use client"

import { useUser, useUserName, useUserInitials } from "@/entities/user"

export function UserProfile() {
  const { data: user, isLoading } = useUser()
  const userName = useUserName()
  const userInitials = useUserInitials()
  
  if (isLoading) {
    return <div>Loading profile...</div>
  }
  
  if (!user) {
    return <div>Not logged in</div>
  }
  
  return (
    <div className="flex items-center space-x-3">
      <div className="w-10 h-10 rounded-full bg-primary text-white flex items-center justify-center">
        {userInitials}
      </div>
      <div>
        <p className="font-medium">{userName}</p>
        <p className="text-sm text-gray-500">{user.email}</p>
        <p className="text-xs text-gray-400">{user.role}</p>
      </div>
    </div>
  )
}
```

---

## Key Patterns to Remember

### 1. **Always destructure `data` from `useUser()`**
```typescript
// ✅ Good
const { data: user } = useUser();

// ❌ Bad
const user = useUser();
```

### 2. **Check loading state before redirecting**
```typescript
// ✅ Good
if (!isLoading && !user) {
  router.push("/login");
}

// ❌ Bad - will redirect even while loading
if (!user) {
  router.push("/login");
}
```

### 3. **Use mutation's isPending for loading states**
```typescript
// ✅ Good
const { mutate: login, isPending } = useLoginMutation();
// Use isPending in UI

// ❌ Bad - managing loading state manually
const [isLoading, setIsLoading] = useState(false);
```

### 4. **Logout should navigate after success**
```typescript
// ✅ Good
const { mutate: logout } = useLogoutMutation({
  onSuccess: () => {
    router.push("/login");
  }
});

// ❌ Bad - window.location.reload() is harsh
logout();
window.location.reload();
```

### 5. **Use helper hooks for computed values**
```typescript
// ✅ Good
const isAdmin = useIsAdmin();
const userName = useUserName();

// ❌ Bad - computing manually
const { data: user } = useUser();
const isAdmin = user?.role === "ADMIN";
const userName = user?.name || "Guest";
```

