// import { NextResponse } from "next/server";
import { cookies } from "next/headers";
import type { User } from "@/shared/types/auth";
import { baseURL } from "@/shared/constants";
import { NextResponse } from "next/server";

const GET = async () => {
    const token = (await cookies()).get("at")?.value;
    if (!token) {
        return null
    }
    const res = await fetch(`${baseURL}/users/me`, {
        method: "GET"
    });
    if (!res.ok) {
        return new NextResponse(JSON.stringify({ error: 'No token' }), { status: 500 })
    }
    const data = await res.json();
    const user = data.user;
    return user as User;
}
export { GET }