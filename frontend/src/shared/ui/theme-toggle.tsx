"use client"

import React from "react"
import { Moon, Sun } from "lucide-react"

function ThemeToggle() {
  const [isDark, setIsDark] = React.useState(false)

  const toggleTheme = () => {
    setIsDark(!isDark)
    document.documentElement.classList.toggle("dark")
  }

  return (
    <button
      onClick={toggleTheme}
      className="w-10 h-10 p-2 rounded-xl flex items-center justify-center
                 bg-gradient-to-br from-gray-100 to-gray-200 dark:from-gray-700 dark:to-gray-800 
                 hover:from-gray-200 hover:to-gray-300 dark:hover:from-gray-600 dark:hover:to-gray-700
                 transition-all duration-300 ease-in-out
                 shadow-md hover:shadow-lg
                 border border-gray-200/50 dark:border-gray-600/50
                 hover:scale-105 active:scale-95
                 group">
      
      <div className="transition-transform duration-300 group-hover:rotate-12">
        {isDark ? (
          <Sun className="w-5 h-5 text-yellow-500 dark:text-yellow-400 drop-shadow-sm" />
        ) : (
          <Moon className="w-5 h-5 text-gray-600 dark:text-gray-300 drop-shadow-sm" />
        )}
      </div>
    </button>
  )
}

export default ThemeToggle
