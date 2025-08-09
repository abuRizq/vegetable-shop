"use client"

import EnterEmail from "@/app/_Components/Email/EnterEmail"
import { useRouter } from "next/navigation"

export default function EnterEmailPage() {
    const router = useRouter()

    return (
        <main
            className="min-h-screen flex items-center justify-center px-4 py-10 w-full"
            style={{ backgroundColor: "hsl(var(--background))" }}
        >
            <EnterEmail
                title="Verify your email"
                subtitle="Enter your email and we’ll send a 6‑digit code to continue."
                buttonText="Send code"
                onSuccessNavigate={(email) => {
                    // Navigate to your code entry / reset flow
                    router.push(`/forgot-password?email=${encodeURIComponent(email)}`)
                }}
            />
        </main>
    )
}
