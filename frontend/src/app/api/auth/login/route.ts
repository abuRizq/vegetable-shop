import { cookies } from 'next/headers'
import { NextRequest, NextResponse } from 'next/server';

const POST = async (req: NextRequest) => {
    try {
        const body = await req.json();
        
        const res = await fetch(`http://localhost:8080/api/auth/login`, {
            method: "POST",
            headers: { "Content-type": "application/json" },
            body: JSON.stringify(body),
            cache: 'no-store',
            credentials: 'include',
        });
        
        const data = await res.json();
        
        if (!res.ok) {
            return NextResponse.json(data, { status: res.status });
        }
        
        const token = data.data?.token ?? data.token;
        
        if (!token) {
            return NextResponse.json({ error: 'No token received' }, { status: 500 });
        }
        (await cookies()).set({
            name: 'at',
            value: token,
            httpOnly: true,
            secure: process.env.NODE_ENV === 'production', // Fixed
            sameSite: 'lax',
            path: '/',
            maxAge: 60 * 60 * 24 * 7, // 7 days
        });
        
        return NextResponse.json(data, { status: 200 });
        
    } catch (error) {
        console.error('Login API error:', error);
        return NextResponse.json(
            { error: 'Internal server error' }, 
            { status: 500 }
        );
    }
}

export { POST };