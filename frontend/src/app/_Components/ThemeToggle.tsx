"use client"

import { IconButton } from "@mui/material"
import { Moon, Sun } from "lucide-react"
import { useTheme } from "next-themes"
import { useEffect, useState } from "react"

export default function ThemeToggle() {
  const [mounted, setMounted] = useState(false)
  const { theme, setTheme } = useTheme()

  useEffect(() => {
    setMounted(true)
  }, [])

  if (!mounted) {
    return (
      <IconButton className="w-10 h-10">
        <div className="w-4 h-4" />
      </IconButton>
    )
  }

  return (
    <IconButton
      onClick={() => setTheme(theme === "dark" ? "light" : "dark")}
      className="w-10 h-10 hover:bg-gray-100 dark:hover:bg-gray-700"
    >
      {theme === "dark" ? <Sun size={18} className="text-yellow-500" /> : <Moon size={18} className="text-gray-600" />}
    </IconButton>
  )
}
