"use client"

import { useRouter, useSearchParams } from "next/navigation"
import { useState } from "react"
import { Eye, EyeOff, Leaf, CheckCircle2, ShieldCheck } from "lucide-react"

export default function ResetPasswordPage() {
    const router = useRouter()
    const params = useSearchParams()
    const email = params.get("email") || ""

    const [pw1, setPw1] = useState("")
    const [pw2, setPw2] = useState("")
    const [show1, setShow1] = useState(false)
    const [show2, setShow2] = useState(false)
    const [error, setError] = useState<string | null>(null)
    const [saving, setSaving] = useState(false)

    const validate = () => {
        if (pw1.length < 8) return "Password must be at least 8 characters."
        if (!/[A-Z]/.test(pw1) || !/[0-9]/.test(pw1)) return "Include at least one uppercase letter and a number."
        if (pw1 !== pw2) return "Passwords do not match."
        return null
    }

    const submit = async () => {
        setError(null)
        const v = validate()
        if (v) {
            setError(v)
            return
        }
        setSaving(true)
        await new Promise((r) => setTimeout(r, 1200))
        // Clear stored code for this email if any
        if (email) sessionStorage.removeItem(`reset-code:${email.toLowerCase()}`)
        setSaving(false)
        router.push("/login?reset=success")
    }

    return (
        <main
            className="min-h-[calc(100vh-0px)] flex items-center justify-center px-4 py-10 w-full"
            style={{ backgroundColor: "hsl(var(--background))" }}
        >
            <div
                className="w-full max-w-lg rounded-2xl border shadow-2xl overflow-hidden"
                style={{ backgroundColor: "hsl(var(--paper))", borderColor: "hsl(var(--divider))" }}
            >
                {/* Header */}
                <div
                    className="px-6 sm:px-8 py-6 border-b flex items-center gap-3"
                    style={{ borderColor: "hsl(var(--divider))", backgroundColor: "hsl(var(--action-hover))" }}
                >
                    <div className="w-12 h-12 rounded-xl bg-primary flex items-center justify-center shadow-green">
                        <Leaf className="w-7 h-7 text-white" />
                    </div>
                    <div>
                        <h1 className="text-xl sm:text-2xl font-bold" style={{ color: "hsl(var(--text-primary))" }}>
                            Reset your password
                        </h1>
                        <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                            {email ? `For ${email}` : "Create a new secure password"}
                        </p>
                    </div>
                </div>

                {/* Body */}
                <div className="px-6 sm:px-8 py-8 space-y-6">
                    <div className="space-y-2">
                        <label className="text-sm font-medium" htmlFor="pw1" style={{ color: "hsl(var(--text-primary))" }}>
                            New password
                        </label>
                        <div className="relative">
                            <input
                                id="pw1"
                                type={show1 ? "text" : "password"}
                                value={pw1}
                                onChange={(e) => setPw1(e.target.value)}
                                placeholder="Enter new password"
                                className="input w-full px-4 py-3 pr-12 rounded-lg focus:outline-none"
                            />
                            <button
                                type="button"
                                onClick={() => setShow1((s) => !s)}
                                className="absolute right-3 top-1/2 -translate-y-1/2"
                                style={{ color: "hsl(var(--text-disabled))" }}
                            >
                                {show1 ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                            </button>
                        </div>
                        <p className="text-xs" style={{ color: "hsl(var(--text-secondary))" }}>
                            Use 8+ characters with a mix of uppercase and numbers.
                        </p>
                    </div>

                    <div className="space-y-2">
                        <label className="text-sm font-medium" htmlFor="pw2" style={{ color: "hsl(var(--text-primary))" }}>
                            Confirm password
                        </label>
                        <div className="relative">
                            <input
                                id="pw2"
                                type={show2 ? "text" : "password"}
                                value={pw2}
                                onChange={(e) => setPw2(e.target.value)}
                                placeholder="Re-enter new password"
                                className="input w-full px-4 py-3 pr-12 rounded-lg focus:outline-none"
                            />
                            <button
                                type="button"
                                onClick={() => setShow2((s) => !s)}
                                className="absolute right-3 top-1/2 -translate-y-1/2"
                                style={{ color: "hsl(var(--text-disabled))" }}
                            >
                                {show2 ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                            </button>
                        </div>
                    </div>

                    {error && (
                        <div
                            className="flex items-center gap-3 p-3 rounded-lg border"
                            style={{
                                borderColor: "hsl(var(--error)/0.4)",
                                backgroundColor: "hsl(var(--error)/0.08)",
                                color: "hsl(var(--error))",
                            }}
                        >
                            <ShieldCheck className="w-5 h-5" />
                            <span className="text-sm">{error}</span>
                        </div>
                    )}

                    <button
                        onClick={submit}
                        disabled={saving}
                        className="btn-primary w-full py-3 rounded-lg font-semibold flex items-center justify-center gap-2 disabled:opacity-60"
                    >
                        {saving ? (
                            <>
                                <CheckCircle2 className="w-5 h-5 animate-pulse" />
                                Updating...
                            </>
                        ) : (
                            <>
                                <CheckCircle2 className="w-5 h-5" />
                                Update Password
                            </>
                        )}
                    </button>
                </div>
            </div>
        </main>
    )
}
