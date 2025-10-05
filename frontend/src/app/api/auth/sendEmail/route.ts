import { NextRequest, NextResponse } from "next/server";

const POST = async (request: NextRequest) => {
  try {
    const body = await request.json();
    const response = await fetch(
      "http://localhost:8080/api/auth/forgot-password",
      {
        method: "POST",
        headers: { "Content-type": "application/json" },
        body: JSON.stringify(body),
        credentials: "include",
      }
    );
    if (!response.ok) {
      return NextResponse.json({ status: response.status });
    }
    return NextResponse.json( { status: 200 });
  } catch (error) {
    console.error("Forgot password API error:", error);
    return NextResponse.json(
      { error: "Failed to process forgot password request" },
      { status: 500 }
    );
  }
};
export { POST };
