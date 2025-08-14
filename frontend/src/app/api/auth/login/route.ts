import { cookies } from 'next/headers'
import { NextRequest, NextResponse } from 'next/server';
const Base_url = process.env.NEXT_PUBLIC_API_URL;

const POST = async (req: NextRequest) => {
    const body = await req.json();
    const res = await fetch(`${Base_url}/api/auth/login`, {
        method: "POST",
        headers: { "Content-type": "application/json" },
        body: JSON.stringify(body),
        cache: 'no-store',

    });
    const data = await res.json();
    if (!res.ok) {
        return new Response(JSON.stringify(data), {
            status: res.status,
        });
    }
    const token = data.data?.token ?? data.token
    if (!token) {
        return new NextResponse(JSON.stringify({ error: 'No token' }), { status: 500 })
    }
    (await cookies()).set({
        name: 'at',
        value: token,
        httpOnly: true,
        secure: process.env.NODE_ENV === 'development',
        sameSite: 'lax',
        path: '/',
        maxAge: 60 * 15, // 15 min
    })
    return new Response(JSON.stringify(data), { status: 200 });
}

export { POST };