import { baseURL } from '@/shared/constants';
import { User } from '@/shared/types/auth';
import { cookies } from 'next/headers';
import { NextRequest, NextResponse } from 'next/server';

const POST = async (NextRequest: NextRequest) => {
    const body = await NextRequest.json();
    const response = await fetch(`${baseURL}/auth/register`, {
        method: "POST",
        body: JSON.stringify(body),
    });
    if (!response.ok) {
        return new Response(JSON.stringify(response), {
            status: response.status,
        });
    }
    const data = await response.json();
    const token = data.token;
    if (!token) {
        return new NextResponse("No token", { status: 401 });
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
    const user: User = data.user;
    return user;
};
export { POST }

