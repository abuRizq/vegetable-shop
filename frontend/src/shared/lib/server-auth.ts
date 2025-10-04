import { cookies } from "next/headers";

/**
 * Server-side user fetching utility
 * This runs on the server and provides initial user data before hydration
 */
export async function getServerUser() {
  try {
    const cookieStore = await cookies();
    const token = cookieStore.get("at")?.value;

    // No token = not authenticated
    if (!token) {
      return null;
    }

    const res = await fetch(`http://localhost:8080/api/users/me`, {
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
