import { NextRequest, NextResponse } from "next/server";

const POST = async (request: NextRequest) => {
  try {
    const body = await request.json();
    const res = await fetch("http://localhost:8080/api/auth/reset-password", {
      method: "POST",
      headers: { "Content-type": "application/json" },
      body: JSON.stringify(body),
      credentials: "include",
    }); 

    const data = await res.json();
      
    if (!res.ok) {
      return NextResponse.json(
        { error: data.message || "Failed to reset password" },
        { status: res.status }
      );
    } 
    return NextResponse.json(data, { status: 200 });
  } catch (error) {
    console.error("Reset password API error:", error);
    return NextResponse.json(
      { error: "Failed to process password reset request" },
      { status: 500 }
    );
  }
};

export { POST };
