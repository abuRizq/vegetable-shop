// import { NextResponse } from "next/server";
import { cookies } from "next/headers";
import { baseURL } from "@/shared/constants";
import { NextResponse } from "next/server";
import { User } from "@/entities/user";

const GET = async () => {
    const token = (await cookies()).get("at")?.value;
    if (!token) {
        return new NextResponse(JSON.stringify({ error: 'No token' }), { status: 500 })
    }
    const res = await fetch(`${baseURL}/users/me`, {
        method: "GET",
        headers: {
            "Content-type": "application/json",
            "Authorization": `Bearer ${token}`
        }
    });
    if (!res.ok) {
        return new NextResponse(JSON.stringify({ error: 'No token' }), { status: 500 })
    }
    const data = await res.json();
    const user = data.user;
    return user as User;
}
export { GET }