// import { NextResponse } from "next/server";
import { cookies } from "next/headers";
import type { User } from "@/app/types/auth";
import { baseURL } from "@/app/constants";



async function GET() {
    try {
        const token = (await cookies()).get("at")?.value;
        if (!token) {
            return null
        }
        const res = await fetch(`${baseURL}/auth/me`, {
            method: "GET"
        });
        if (!res.ok) {
            return
        }
        const data = await res.json();
        const user = data.user;
        return user as User;
    } catch (error) {

    }

}
export { GET }