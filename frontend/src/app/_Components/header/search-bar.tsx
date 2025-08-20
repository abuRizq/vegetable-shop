"use client"

import { useState, useRef } from "react"
import { Search, X } from "lucide-react"

function Searchbar() {
  const [isFocused, setIsFocused] = useState(false)
  const [searchValue, setSearchValue] = useState("")
  const [isAnimating, setIsAnimating] = useState(false)
  const inputRef = useRef<HTMLInputElement>(null)

  const handleFocus = () => {
    setIsFocused(true)
    setIsAnimating(true)
    setTimeout(() => setIsAnimating(false), 300)
  }

  const handleBlur = () => {
    setIsFocused(false)
  }

  const handleClear = () => {
    setSearchValue("")
    inputRef.current?.focus()
  }

  const handleClick = () => {
    if (!isFocused) {
      inputRef.current?.focus()
    }
  }

  return (
    <div className="flex-1 max-w-md mx-6">
      <div
        onClick={handleClick}
        className={`
          relative group cursor-text
          transition-all duration-300 ease-out
          ${isFocused ? "transform scale-105" : "hover:scale-102"}
        `}
      >
        {/* Search Icon */}
        <div
          className={`
            absolute left-3 top-1/2 transform -translate-y-1/2 z-10
            transition-all duration-300 ease-out
            ${isFocused ? "text-red-500 scale-110" : "text-gray-400 dark:text-gray-500"}
            ${isAnimating ? "animate-pulse" : ""}
          `}
        >
          <Search size={18} />
        </div>

        {/* Input Field */}
        <input

          ref={inputRef}
          type="text"
          value={searchValue}
          onChange={(e) => setSearchValue(e.target.value)}
          onFocus={handleFocus}
          onBlur={handleBlur}
          className={`
            w-full h-12 pl-10 pr-10
            rounded-2xl
            mt-2
            border-2
            bg-gradient-to-r from-gray-50 to-gray-100 dark:from-gray-800 dark:to-gray-700
            text-gray-900 dark:text-white
            placeholder-gray-500 dark:placeholder-gray-400
            outline-none
            transition-all duration-300 ease-out
            ${isFocused
              ? "border-red-500 shadow-lg shadow-red-500/20 dark:shadow-red-500/10 bg-white dark:bg-gray-800"
              : "border-gray-200 dark:border-gray-600 hover:border-gray-300 dark:hover:border-gray-500 hover:shadow-md"
            }
            ${isAnimating ? "animate-pulse" : ""}
          `}
          placeholder="Search vagatable, Fruite, shops..."
          style={{
            fontFamily: "Geist, Roboto, Arial, sans-serif",
            fontSize: 15,
          }}
        />

        {/* Clear Button */}
        {searchValue && (
          <button
            onClick={handleClear}
            className={`
              absolute right-3 top-1/2 transform -translate-y-1/2 z-10
              w-6 h-6 rounded-full
              bg-gray-300 dark:bg-gray-600
              hover:bg-gray-400 dark:hover:bg-gray-500
              text-gray-600 dark:text-gray-300
              hover:text-gray-800 dark:hover:text-white
              transition-all duration-200 ease-out
              hover:scale-110 active:scale-95
              opacity-0 animate-fade-in
            `}
            style={{ animation: "fadeIn 0.2s ease-out forwards" }}
          >
            <X size={14} />
          </button>
        )}

        {/* Focus Ring Animation */}
        <div
          className={`
            absolute inset-0 rounded-2xl
            transition-all duration-300 ease-out
            ${isFocused
              ? "ring-2 ring-red-500/30 ring-offset-2 ring-offset-transparent scale-105"
              : "ring-0 ring-transparent"
            }
          `}
        />

        {/* Ripple Effect */}
        {isAnimating && (
          <div className="absolute inset-0 rounded-2xl overflow-hidden pointer-events-none">
            <div className="absolute inset-0 bg-red-500/10 animate-ping rounded-2xl" />
          </div>
        )}

        {/* Glow Effect */}
        <div
          className={`
            absolute inset-0 rounded-2xl
            transition-all duration-500 ease-out
            ${isFocused
              ? "bg-gradient-to-r from-red-500/5 via-transparent to-red-500/5 animate-pulse"
              : "bg-transparent"
            }
          `}
        />
      </div>

      {/* Search Suggestions (Optional) */}
      {isFocused && searchValue && (
        <div className="absolute top-full left-0 right-0 mt-2 bg-white dark:bg-gray-800 rounded-xl shadow-2xl border border-gray-200 dark:border-gray-700 z-50 overflow-hidden animate-slide-down">
          <div className="p-4">
            <div className="text-sm text-gray-500 dark:text-gray-400 mb-2">Quick suggestions</div>
            <div className="space-y-2">
              {["Stranger Things", "The Crown", "Ozark", "Breaking Bad"].map((suggestion, index) => (
                <div
                  key={suggestion}
                  className="flex items-center p-2 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 cursor-pointer transition-colors duration-200"
                  style={{ animationDelay: `${index * 50}ms` }}
                >
                  <Search size={14} className="text-gray-400 mr-3" />
                  <span className="text-gray-900 dark:text-white">{suggestion}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default Searchbar
