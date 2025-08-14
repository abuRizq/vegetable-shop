import { User } from '@/app/types/auth';
import { cookies } from 'next/headers'

type ApiResponse<T> = { success: boolean; data: T; error: string | null; meta: unknown | null }

const GET = async () => {
    const token = (await cookies()).get('at')?.value
    if (!token)
        return { success: false, data: null, error: "Not logged in", meta: null };
    const res = await fetch(`https://localhost:3000/api/auth/me`, {
        headers: {
            Authorization: `Bearer ${token}`
        }
    })
    const data = (await res.json()) as ApiResponse<User>;
    if (!res.ok || !data.success) {
        return new Response(JSON.stringify(
            { error: data.error ?? 'Unauthorized' }
        )
            , { status: res.status })
    }
    return { success: true, data: data, error: null, meta: null }
}
