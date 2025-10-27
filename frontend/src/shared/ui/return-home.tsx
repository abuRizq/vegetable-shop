"use client"

import Link from "next/link"
import { ArrowLeft, Home } from "lucide-react"

export default function ReturnHome() {
  return (
    <Link
      href="/"
      className="
        fixed top-6 right-6 z-50
        flex items-center space-x-2 px-4 py-3 rounded-xl
        backdrop-blur-xl border shadow-lg
        transition-all duration-300 ease-out
        hover:scale-105 hover:shadow-xl active:scale-95
        group
      "
      style={{
        backgroundColor: "hsl(var(--paper) / 0.95)",
        borderColor: "hsl(var(--divider))",
        boxShadow: "var(--shadow-md)",
      }}
      aria-label="Return to Home"
    >
      {/* Arrow Icon */}
      <div
        className="
          w-8 h-8 rounded-lg flex items-center justify-center
          transition-all duration-300 ease-out
          group-hover:scale-110 group-hover:rotate-[-5deg]
        "
        style={{ backgroundColor: "hsl(var(--primary))" }}
      >
        <ArrowLeft className="w-4 h-4 text-white" />
      </div>

      {/* Text */}
      <div className="hidden sm:flex flex-col">
        <span
          className="text-sm font-semibold transition-colors duration-200"
          style={{ color: "hsl(var(--text-primary))" }}
        >
          Back to Home
        </span>
        <span
          className="text-xs transition-colors duration-200"
          style={{ color: "hsl(var(--text-secondary))" }}
        >
          FreshVeggies
        </span>
      </div>

      {/* Home Icon (mobile) */}
      <div className="sm:hidden">
        <Home
          className="w-4 h-4 transition-colors duration-200 group-hover:text-primary-color"
          style={{ color: "hsl(var(--text-secondary))" }}
        />
      </div>

      {/* Hover Glow Effect */}
      <div
        className="
          absolute inset-0 rounded-xl opacity-0 group-hover:opacity-100
          transition-opacity duration-300 ease-out pointer-events-none
        "
        style={{
          background:
            "linear-gradient(135deg, hsl(var(--primary) / 0.1), hsl(var(--accent) / 0.1))",
        }}
      />

      {/* Ripple Effect */}
      <div
        className="
          absolute inset-0 rounded-xl opacity-0 group-active:opacity-100
          transition-opacity duration-150 ease-out pointer-events-none
          animate-ping
        "
        style={{ backgroundColor: "hsl(var(--primary) / 0.2)" }}
      />
    </Link>
  );
}
