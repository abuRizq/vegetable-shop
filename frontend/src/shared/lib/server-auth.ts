import { cookies } from "next/headers";

export async function getServerUser() {
  try {
    const cookieStore = await cookies();
    const token = cookieStore.get("auth_token")?.value;
    // No token = not authenticated
    if (!token) {
      return null;
    }
    const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/users/me`, {
      method: "GET",
      headers: {
        "Content-type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      cache: "no-store", // Always fetch fresh data
    });

    // Invalid or expired token
    if (!res.ok) {
      return null;
    }

    const data = await res.json();
    // Backend returns: { data: { user: {...} } }
    // Extract user to match the same structure as client-side
    const user = data?.data ;

    return user;
  } catch (error) {
    console.error("Server-side auth error:", error);
    return null;
  }
}
