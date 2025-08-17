// src/app/lib/auth-server.ts (server-only)
import { cookies } from "next/headers";
import type { User } from "../types/auth";

export async function getCurrentUser(): Promise<User | null> {
    // if you use cookies for auth
    const token = (await cookies()).get("at")?.value;
    if (!token) return null;

    const res = await fetch(`/api/auth/me`, {
        cache: "no-store",
        headers: { Authorization: `Bearer ${token}` },
    });

    if (!res.ok) return null;

    const data = await res.json();
    const user: User = data.data;
    return user;
}
