"use client";

import { useSearchParams, useRouter } from "next/navigation";
import React, { useState } from "react";
import { Lock, Leaf, Loader2, KeyRound } from "lucide-react";
import { useRestPassword } from "../api/reset-password";
import { Toast } from "@/shared/ui/toast";

function ResetForm() {
  const parm = useSearchParams();
  const router = useRouter();
  const token = parm.get("token");
  console.log(token);

  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState<string>();
  const [success, setSuccess] = useState<string>();
  const [loading, setLoading] = useState(false);

  const { mutate: restPassword } = useRestPassword({
    onSuccess: (data) => {
      console.log("Password reset successful", data);
      setSuccess("Password reset successfully! Redirecting to login...");
      Toast.success("please login again ", {
        position: "top-right",
      });
      router.push("/login");
    },
    onError: (error) => {
      console.error("Reset password error:", error);
      Toast.error(error.message, {
        position: "top-center",
      });
      setLoading(false);
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError(undefined);
    setSuccess(undefined);

    // Validate token exists
    if (!token) {
      setError("Invalid or missing reset token");
      return;
    }

    // Validate passwords match
    if (newPassword !== confirmPassword) {
      setError("Passwords don't match");
      return;
    }

    // Validate password length
    if (newPassword.length < 8) {
      setError("Password must be at least 8 characters");
      return;
    }
    setLoading(true);
    restPassword({ token: token, newPassword: newPassword });
  };

  return (
    <div
      className="w-full max-w-lg rounded-2xl border shadow-2xl overflow-hidden"
      style={{
        backgroundColor: "hsl(var(--paper))",
        borderColor: "hsl(var(--divider))",
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
            Reset Your Password
          </h1>
          <p
            className="text-sm"
            style={{ color: "hsl(var(--text-secondary))" }}
          >
            Enter your new password below
          </p>
        </div>
      </div>

      {/* Body */}
      <form onSubmit={handleSubmit} className="px-6 sm:px-8 py-8 space-y-6">
        <div className="space-y-2">
          <label
            htmlFor="first"
            className="text-sm font-medium"
            style={{ color: "hsl(var(--text-primary))" }}
          >
            New Password
          </label>
          <div className="relative">
            <Lock
              className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5"
              style={{ color: "hsl(var(--text-disabled))" }}
            />
            <input
              type="password"
              name="newPassword"
              id="first"
              placeholder="Enter new password"
              value={newPassword}
              onChange={(e) => {
                setNewPassword(e.target.value);
                if (error) setError(undefined);
                if (success) setSuccess(undefined);
              }}
              required
              className="input w-full pl-10 pr-4 py-3 rounded-lg focus:outline-none"
            />
          </div>
        </div>

        <div className="space-y-2">
          <label
            htmlFor="second"
            className="text-sm font-medium"
            style={{ color: "hsl(var(--text-primary))" }}
          >
            Confirm Password
          </label>
          <div className="relative">
            <Lock
              className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5"
              style={{ color: "hsl(var(--text-disabled))" }}
            />
            <input
              type="password"
              name="confirmPassword"
              id="second"
              placeholder="Confirm new password"
              value={confirmPassword}
              onChange={(e) => {
                setConfirmPassword(e.target.value);
                if (error) setError(undefined);
                if (success) setSuccess(undefined);
              }}
              required
              className="input w-full pl-10 pr-4 py-3 rounded-lg focus:outline-none"
            />
          </div>
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

        <button
          type="submit"
          disabled={loading || !newPassword || !confirmPassword}
          className="btn-primary w-full py-3 rounded-lg font-semibold flex items-center justify-center gap-2 disabled:opacity-60"
        >
          {loading ? (
            <>
              <Loader2 className="w-5 h-5 animate-spin" />
              Resetting password...
            </>
          ) : (
            <>
              <KeyRound className="w-5 h-5" />
              Reset Password
            </>
          )}
        </button>

        <p
          className="text-xs text-center"
          style={{ color: "hsl(var(--text-disabled))" }}
        >
          Password must be at least 8 characters long
        </p>
      </form>
    </div>
  );
}

export default ResetForm;
