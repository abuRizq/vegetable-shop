import { User } from "../types/auth"

export async function getCurrentUser(): Promise<User | null> {
    const res = await fetch(`${process.env.NEXT_PUBLIC_SITE_URL ?? ''}/api/users/me`, { cache: 'no-store' })
    if (!res.ok) return null
    return (await res.json()) as User
}