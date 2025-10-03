import { cookies } from "next/headers";
import { NextResponse } from "next/server";

const GET = async () => {
    try {
        const cookieStore = await cookies();
        const token = cookieStore.get("at")?.value;

        // Scenario 1: No token exists
        if (!token) {
            return NextResponse.json({
                error: 'No authentication token',
                code: 'NO_TOKEN'
            }, { status: 401 });
        }
        const res = await fetch(`http://localhost:8080/api/users/me`, {
            method: "GET",
            headers: {
                "Content-type": "application/json",
                "Authorization": `Bearer ${token}`
            }
        });

        // Scenario 2: Token exists but is invalid/expired
        if (!res.ok) {
            // Clear the invalid token cookie
            const response = NextResponse.json({
                error: res.status === 401 ? 'Authentication expired' : 'Authentication failed',
                code: res.status === 401 ? 'TOKEN_EXPIRED' : 'TOKEN_INVALID'
            }, { status: 401 });
            
            // Remove the invalid cookie
            response.cookies.set({
                name: 'at',
                value: '',
                httpOnly: true,
                secure: process.env.NODE_ENV === 'production',
                sameSite: 'lax',
                path: '/',
                maxAge: 0 // This deletes the cookie
            });

            return response;
        }
        // Scenario 3: Valid token
        const data = await res.json();
        console.log('Backend response:', JSON.stringify(data, null, 2));
        const user = data.data?.user || data.user || data;
        // console.log('Extracted user:', JSON.stringify(user, null, 2));

        return NextResponse.json({
            user,
            code: 'SUCCESS'
        });

    } catch (error) {
        console.error('Auth verification error:', error);
        return NextResponse.json({
            error: 'Internal server error',
            code: 'SERVER_ERROR'
        }, { status: 500 });
    }
}
export { GET }