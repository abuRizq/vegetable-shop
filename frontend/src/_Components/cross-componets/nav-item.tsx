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
                    nav-item-transition
                    focus:outline-none focus:ring-2 focus:ring-opacity-50
                    backdrop-blur-sm
                    ${collapsed ? "px-3 py-3 justify-center mx-1" : "px-4 py-3 mx-2"}
                    ${active ? "shadow-lg border scale-[1.02]" : "hover:shadow-md hover:scale-[1.01]"}
                    ${className}
                `}
                style={{
                    backgroundColor: active ? "hsl(var(--action-selected))" : "transparent",
                    color: active ? "hsl(var(--primary))" : "hsl(var(--text-primary))",
                    borderColor: active ? "hsl(var(--primary) / 0.3)" : "transparent",
                }}
            >
                <span
                    className={`
                    flex-shrink-0 transition-all duration-300
                    ${active ? "scale-110 drop-shadow-sm" : "group-hover:scale-105"}
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
                        <div className="w-2 h-2 bg-primary rounded-full animate-pulse shadow-sm"></div>
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
