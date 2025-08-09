"use client"

import type React from "react"

import { useEffect, useRef, useState } from "react"

interface OtpInputProps {
    length?: number
    onComplete?: (code: string) => void
    isError?: boolean
    disabled?: boolean
}

export function OtpInput({ length = 6, onComplete, isError = false, disabled = false }: OtpInputProps) {
    const [values, setValues] = useState<string[]>(Array.from({ length }, () => ""))
    const inputsRef = useRef<Array<HTMLInputElement | null>>([])

    useEffect(() => {
        inputsRef.current[0]?.focus()
    }, [])

    const handleChange = (index: number, val: string) => {
        if (disabled) return
        const v = val.replace(/\D/g, "").slice(0, 1)
        const next = [...values]
        next[index] = v
        setValues(next)

        if (v && index < length - 1) {
            inputsRef.current[index + 1]?.focus()
        }

        const code = next.join("")
        if (code.length === length && onComplete) {
            onComplete(code)
        }
    }

    const handleKeyDown = (index: number, e: React.KeyboardEvent<HTMLInputElement>) => {
        if (disabled) return
        if (e.key === "Backspace" && !values[index] && index > 0) {
            inputsRef.current[index - 1]?.focus()
        }
        if (e.key === "ArrowLeft" && index > 0) {
            inputsRef.current[index - 1]?.focus()
        }
        if (e.key === "ArrowRight" && index < length - 1) {
            inputsRef.current[index + 1]?.focus()
        }
    }

    const handlePaste = (e: React.ClipboardEvent<HTMLInputElement>) => {
        if (disabled) return
        e.preventDefault()
        const pasted = e.clipboardData.getData("text").replace(/\D/g, "").slice(0, length)
        if (!pasted) return

        const next = Array.from({ length }, (_, i) => pasted[i] || "")
        setValues(next)

        const code = next.join("")
        if (code.length === length && onComplete) {
            onComplete(code)
        } else {
            // focus the next empty
            const firstEmpty = next.findIndex((v) => !v)
            if (firstEmpty >= 0) inputsRef.current[firstEmpty]?.focus()
        }
    }

    return (
        <div className="flex items-center justify-center gap-2 sm:gap-3">
            {values.map((v, i) => (
                <input
                    key={i}
                    ref={(el) => { inputsRef.current[i] = el; }}
                    value={v}
                    onChange={(e) => handleChange(i, e.target.value)}
                    onKeyDown={(e) => handleKeyDown(i, e)}
                    onPaste={handlePaste}
                    disabled={disabled}
                    aria-label={`Digit ${i + 1}`}
                    inputMode="numeric"
                    pattern="[0-9]*"
                    maxLength={1}
                    className={[
                        "w-10 h-12 sm:w-12 sm:h-14 text-center text-lg sm:text-xl font-semibold rounded-lg",
                        "focus:outline-none transition-all duration-200",
                        "bg-[hsl(var(--paper))] border",
                        isError ? "border-[hsl(var(--error))]" : "border-[hsl(var(--divider))]",
                        disabled
                            ? "opacity-60 cursor-not-allowed"
                            : "focus:border-[hsl(var(--primary))] focus:shadow-[0_0_0_3px_hsl(var(--primary)/0.15)]",
                    ].join(" ")}
                />
            ))}
        </div>
    )
}
