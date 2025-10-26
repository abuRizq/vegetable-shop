"use client";

import type React from "react";

import { useState } from "react";
import { Mail, Leaf, Loader2, Send } from "lucide-react";
import { useSendRestPasswordEmail } from "../api/use-forget-password";
// import { ToastContainer, toast } from 'react-toastify';

type EnterEmailProps = {
  title?: string;
  subtitle?: string;
  buttonText?: string;
  onSend?: (email: string, code: string) => Promise<void> | void;
  onSuccessNavigate?: (email: string) => void;
  defaultEmail?: string;
};

function EnterEmail({
  title = "Forgot your password?",
  subtitle = "Enter your email and we'll send you a 6‑digit code to verify it’s you.",
  buttonText = "Send verification code",
  defaultEmail = "",
}: EnterEmailProps) {
  const [email, setEmail] = useState(defaultEmail);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const validateEmail = (value: string) => /\S+@\S+\.\S+/.test(value);
    

    const {mutate:send }= useSendRestPasswordEmail({
    onSuccess: (data) => {
      console.log("check your email and don't refresh the page successful");
      console.log(data);
      setSuccess("A verification code has been sent to your email address.");
      setLoading(false);
    },
    onError: (error) => {
      console.error("Login error:", error);
      setError(error.message);
      setLoading(false);
    }
  })

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    if (!email || !validateEmail(email)) {
      setError("Please enter a valid email address.");
      return;
    }
    setLoading(true);
    send(email);
  };

  return (
    <div
      className="w-full  max-w-lg rounded-2xl border shadow-2xl overflow-hidden"
      style={{
        backgroundColor: "hsl(var(--paper))",
        borderColor: "hsl(var   (--divider))",
      }}
    >
      {/* Header */}
      <div
        className="px-6 sm:px-8 py-6 border-b flex items-center gap-3"
        style={{
          borderColor: "hsl(var(--divider))",
          backgroundColor: "hsl(var(--action-hover))",
        }}
      >
        <div className="w-12 h-12 rounded-xl bg-primary flex items-center justify-center shadow-green">
          <Leaf className="w-7 h-7 text-white" />
        </div>
        <div>
          <h1
            className="text-xl sm:text-2xl font-bold"
            style={{ color: "hsl(var(--text-primary))" }}
          >
            {title}
          </h1>
          <p
            className="text-sm"
            style={{ color: "hsl(var(--text-secondary))" }}
          >
            {subtitle}
          </p>
        </div>
      </div>
      {/* Body */}
      <form onSubmit={handleSubmit} className="px-6 sm:px-8 py-8 space-y-6">
        <div className="space-y-2">
          <label
            htmlFor="email"
            className="text-sm font-medium"
            style={{ color: "hsl(var(--text-primary))" }}
          >
            Email address
          </label>
          <div className="relative">
            <Mail
              className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5"
              style={{ color: "hsl(var(--text-disabled))" }}
            />
            <input
              id="email"
              type="email"
              placeholder="you@example.com"
              value={email}
              onChange={(e) => {
                setEmail(e.target.value);
                if (error) setError(null);
                if (success) setSuccess(null);
              }}
              className="input w-full pl-10 pr-4 py-3 rounded-lg focus:outline-none"
            />
          </div>
          {error && (
            <p
              className="text-sm mt-1"
              style={{ color: "hsl(var(--error))" }}
              aria-live="polite"
            >
              {error}
            </p>
          )}
          {success && (
            <p
              className="text-sm mt-1"
              style={{ color: "hsl(var(--success))" }}
              aria-live="polite"
            >
              {success}
            </p>
          )}
        </div>

        <button
          type="submit"
          disabled={loading || !email}
          className="btn-primary w-full py-3 rounded-lg font-semibold flex items-center justify-center gap-2 disabled:opacity-60"
        >
          {loading ? (
            <>
              <Loader2 className="w-5 h-5 animate-spin" />
              Sending code...
            </>
          ) : (
            <>
              <Send className="w-5 h-5" />
              {buttonText}
            </>
          )}
        </button>

        <p
          className="text-xs text-center"
          style={{ color: "hsl(var(--text-disabled))" }}
        >
          We’ll send a 6‑digit code to your email. It expires in 10 minutes.
        </p>
      </form>
    </div>
  );
}

export { EnterEmail };
