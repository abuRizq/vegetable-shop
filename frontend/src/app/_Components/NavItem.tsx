import type React from "react"

export interface NavItemProps {
    icon: React.ReactNode
    text: string
    active?: boolean
    className?: string
    collapsed?: boolean
}

function NavItem({ icon, text, active = false, className = "", collapsed = false }: NavItemProps) {
    return (
        <div className="relative group">
            <a
                href="#"
                className={`
                    flex items-center rounded-xl
                    transition-all duration-300 ease-out
                    hover:bg-gray-100/80 dark:hover:bg-gray-800/60
                    focus:outline-none focus:ring-2 focus:ring-blue-500/50 focus:ring-opacity-50
                    backdrop-blur-sm
                    ${collapsed ? "px-3 py-3 justify-center mx-1" : "px-4 py-3 mx-2"}
                    ${active
                        ? "bg-gradient-to-r from-blue-50/90 to-indigo-50/90 dark:from-blue-900/40 dark:to-indigo-900/40 text-blue-700 dark:text-blue-400 shadow-lg shadow-blue-500/10 border border-blue-200/50 dark:border-blue-800/50 scale-[1.02]"
                        : "text-gray-700 dark:text-gray-300 hover:text-gray-900 dark:hover:text-white hover:shadow-md hover:shadow-gray-200/20 dark:hover:shadow-black/10 hover:scale-[1.01]"
                    }
                    ${className}
                `}
            >
                <span
                    className={`
                    flex-shrink-0 transition-all duration-300
                    ${active ? "scale-110 text-blue-600 dark:text-blue-400 drop-shadow-sm" : "group-hover:scale-105"}
                    ${collapsed ? "" : "mr-3"}
                `}
                >
                    {icon}
                </span>
                {!collapsed && (
                    <span className="font-medium text-sm truncate transition-all duration-300 tracking-wide">{text}</span>
                )}
                {active && !collapsed && (
                    <div className="ml-auto flex items-center">
                        <div className="w-2 h-2 bg-gradient-to-r from-blue-500 to-indigo-500 rounded-full animate-pulse shadow-sm"></div>
                    </div>
                )}
            </a>

            {/* Enhanced Tooltip for collapsed state */}
            {collapsed && (
                <div
                    className="
                    absolute left-full ml-4 px-3 py-2
                    bg-gray-900/95 dark:bg-gray-700/95 text-white text-sm font-medium
                    rounded-lg shadow-2xl shadow-black/20 z-50 whitespace-nowrap
                    opacity-0 group-hover:opacity-100 
                    pointer-events-none group-hover:pointer-events-auto
                    transition-all duration-300 ease-out
                    top-1/2 transform -translate-y-1/2
                    scale-95 group-hover:scale-100
                    backdrop-blur-sm border border-white/10
                "
                >
                    {text}
                    <div className="absolute left-0 top-1/2 transform -translate-y-1/2 -translate-x-full">
                        <div className="w-0 h-0 border-t-[6px] border-b-[6px] border-r-[6px] border-transparent border-r-gray-900/95 dark:border-r-gray-700/95"></div>
                    </div>
                </div>
            )}
        </div>
    )
}

export default NavItem
