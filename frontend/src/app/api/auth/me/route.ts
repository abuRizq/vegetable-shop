import { NextResponse } from "next/server";
import { cookies } from "next/headers";
import type { User } from "@/app/types/auth";

type ApiResponse<T> = {
    success: boolean;
    data: T | null;
    error: string | null;
    meta: unknown | null;
};

export const runtime = "nodejs";
export const dynamic = "force-dynamic";

export async function GET(req: Request) {
    const token = (await cookies()).get("at")?.value;

    if (!token) {
        return NextResponse.json<ApiResponse<User>>(
            { success: false, data: null, error: "Not logged in", meta: null },
            { status: 401 }
        );
    }
    const upstream = await fetch("http://localhost:8080/api/auth/me", {
        headers: { Authorization: `Bearer ${token}` },
        cache: "no-store",
    });

    const body = await upstream.json().catch(() => null);

    if (!upstream.ok || (body && body.success === false)) {
        const msg =
            (body && (body.error || body.message)) || upstream.statusText || "Unauthorized";
        return NextResponse.json<ApiResponse<User>>(
            { success: false, data: null, error: msg, meta: null },
            { status: upstream.status || 401 }
        );
    }
    const userResp = body as ApiResponse<User>;
    return NextResponse.json<ApiResponse<User>>(
        {
            success: true,
            data: userResp.data, // 
            error: null,
            meta: userResp.meta ?? null,
        },
        { status: 200 }
    );
}
