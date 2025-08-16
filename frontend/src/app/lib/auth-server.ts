// src/app/lib/auth-server.ts (server-only)
import { headers, cookies } from "next/headers";
import type { User } from "../types/auth";

export async function getCurrentUser(): Promise<User | null> {
    // if you use cookies for auth
    const token = (await cookies()).get("at")?.value;
    const host = (await headers()).get("host")!;
    const protocol = process.env.NODE_ENV === "development" ? "http" : "https";

    const res = await fetch(`${protocol}://${host}/api/users/me`, {
        cache: "no-store",
        headers: token ? { Authorization: `Bearer ${token}` } : {},
    });

    if (!res.ok) return null;
    return (await res.json()) as User;
}
