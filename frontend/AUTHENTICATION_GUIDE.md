# Authentication Implementation Guide

## Overview
This project uses **cookie-based authentication** with automatic token injection for all API requests.

## How It Works

### 1. **Login Flow**
```tsx
import { useLoginMutation } from "@/features/auth/login/api/use-login";

const { mutate: login } = useLoginMutation({
  onSuccess: () => {
    router.push("/dashboard");
  }
});

// Login with credentials
login({
  email: "user@example.com",
  password: "password123",
  rememberMe: true // Optional: extends token lifetime to 30 days
});
```

**What happens:**
1. Credentials sent to backend `/auth/login`
2. Backend returns token in response
3. Token automatically stored in cookie
4. User data cached in React Query

### 2. **Automatic Token Injection**
All API requests automatically include the token from cookies:

```tsx
import { api } from "@/shared/lib/api-client";

// Token automatically added to Authorization header
const products = await api.get('/products');
const user = await api.get('/users/me');
```

### 3. **Logout Flow**
```tsx
import { useLogoutMutation } from "@/features/auth/logout/use-logout";

const { mutate: logout } = useLogoutMutation({
  onSuccess: () => {
    router.push("/login");
  }
});

// Logout
logout();
```

**What happens:**
1. Calls backend `/auth/logout` (optional)
2. Removes token from cookie
3. Clears user data from cache
4. Redirects to login

### 4. **Check Authentication Status**
```tsx
import { useIsAuthenticated, useUser } from "@/entities/user";

function MyComponent() {
  const isAuthenticated = useIsAuthenticated();
  const { data: user, isLoading } = useUser();

  if (isLoading) return <Spinner />;
  if (!isAuthenticated) return <LoginPrompt />;

  return <div>Welcome {user?.name}!</div>;
}
```

## API Client Usage

### Making Authenticated Requests
```tsx
import { api } from "@/shared/lib/api-client";

// GET request (token auto-included)
const data = await api.get('/users/profile');

// POST request
const newProduct = await api.post('/products', { name: "Carrot", price: 2.99 });

// PUT request
const updated = await api.put('/products/1', { price: 3.49 });

// DELETE request
await api.delete('/products/1');
```

### Public Endpoints (No Token Required)
```tsx
// For login, register, etc.
const response = await api.post('/auth/login', credentials, {
  requiresAuth: false
});
```

## Cookie Configuration

### Security Settings
- **HttpOnly**: No (client-side cookie for flexibility)
- **Secure**: Yes (in production only)
- **SameSite**: Lax (CSRF protection)
- **Path**: / (available everywhere)
- **Max-Age**: 7 days (30 days with rememberMe)

### Cookie Name
- `auth_token` - stores the JWT token

## Backend Requirements

Your backend should:
1. Accept token in `Authorization: Bearer <token>` header
2. Return user data on `/users/me` endpoint
3. Return token in login response:
```json
{
  "data": {
    "token": "eyJhbGciOiJIUzI1...",
    "user": {
      "id": 1,
      "email": "user@example.com",
      "name": "John Doe"
    }
  }
}
```

## Environment Variables

Add to `.env.local`:
```bash
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

## Migration Notes

### Removed
- ❌ Next.js API proxy routes (`/api/auth/login`, etc.)
- ❌ HttpOnly cookies managed by Next.js server
- ❌ `server-auth.ts` server-side utilities

### Added
- ✅ Client-side cookie management
- ✅ Direct backend API calls
- ✅ Automatic token injection
- ✅ Cleaner architecture

## Error Handling

```tsx
import { api } from "@/shared/lib/api-client";

try {
  const data = await api.get('/protected-route');
} catch (error) {
  if (error.message.includes('401')) {
    // Token expired or invalid
    router.push('/login');
  } else {
    // Other error
    toast.error(error.message);
  }
}
```

## Complete Example: Protected Page

```tsx
"use client";

import { useIsAuthenticated, useUser } from "@/entities/user";
import { useLogoutMutation } from "@/features/auth/logout/use-logout";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

export default function DashboardPage() {
  const router = useRouter();
  const isAuthenticated = useIsAuthenticated();
  const { data: user, isLoading } = useUser();
  const { mutate: logout } = useLogoutMutation({
    onSuccess: () => router.push("/login")
  });

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.push("/login");
    }
  }, [isAuthenticated, isLoading, router]);

  if (isLoading) {
    return <div>Loading...</div>;
  }

  if (!isAuthenticated) {
    return null;
  }

  return (
    <div>
      <h1>Welcome {user?.name}!</h1>
      <button onClick={() => logout()}>Logout</button>
    </div>
  );
}
```
