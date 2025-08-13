import { cookies } from 'next/headers'
import { NextRequest } from 'next/server';
const Base_url = process.env.NEXT_PUBLIC_API_URL;

const POST = async (req: NextRequest) => {
    const body = await req.json();
    // const { email, password } = body;
    try {
        const res = fetch(`${Base_url}/api/auth/login`, {
            method: "POST",
            headers: { "Content-type": "application/json" },
            body: JSON.stringify(body),
            cache: 'no-store'
        });
        const data = (await res).json();
        if (!req.ok) {
            return new Response(JSON.stringify(data), {
                status: req.status,
            });
        }
        return new Response(JSON.stringify(data), { status: 200 });
    } catch (error) {

    }
}