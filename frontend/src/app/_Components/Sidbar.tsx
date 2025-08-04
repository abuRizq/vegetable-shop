"use client"

import { useState } from "react"

import { Home, Heart, Clock, ShoppingCart, Tag, Settings, LogOut, Star, ChevronLeft, ChevronRight, Leaf } from "lucide-react"
import NavItem from "./NavItem"
import ThemeToggle from "./ThemeToggle"

function Sidebar() {
    const [isCollapsed, setIsCollapsed] = useState(false)
    const toggleSidebar = () => {
        setIsCollapsed(!isCollapsed)
    }
    return (
        <aside
            className={`
            h-screen flex flex-col 
            border-r shadow-2xl
            transition-all duration-500 ease-in-out relative
            backdrop-blur-xl sidebar-transition
            fixed left-0 top-0 z-10
            ${isCollapsed ? "w-[80px]" : "w-[280px]"}
        `}
            style={{
                backgroundColor: "hsl(var(--paper))",
                borderColor: "hsl(var(--divider))",
                boxShadow: "var(--shadow-lg)",
            }}
        >
            {/* Toggle Button */}
            <button
                onClick={toggleSidebar}
                className="
                    absolute -right-3 top-8 z-20
                    w-7 h-7 rounded-full shadow-xl
                    flex items-center justify-center
                    hover:shadow-2xl hover:scale-110 active:scale-95
                    transition-all duration-300 ease-out
                    group
                "
                style={{
                    backgroundColor: "hsl(var(--elevated))",
                    borderColor: "hsl(var(--divider))",
                    color: "hsl(var(--text-secondary))",
                    boxShadow: "var(--shadow-md)",
                }}
                aria-label={isCollapsed ? "Expand sidebar" : "Collapse sidebar"}
            >
                <div className="transition-transform duration-300 group-hover:scale-110">
                    {isCollapsed ? <ChevronRight size={14} /> : <ChevronLeft size={14} />}
                </div>
            </button>

            {/* Logo Section */}
            <div
                className={`
                  flex items-center justify-between p-6
                  border-b transition-all duration-500 ease-out
                  sidebar-transition
                  ${isCollapsed ? "px-4" : "px-6"}
              `}
                style={{
                    borderColor: "hsl(var(--divider))",
                    backgroundColor: "hsl(var(--action-hover))",
                }}
            >
                <div className="flex items-center overflow-hidden">
                    <div
                        className="flex items-center font-bold text-xl tracking-tight whitespace-nowrap"
                        style={{ color: "hsl(var(--text-primary))" }}
                    >
                        {isCollapsed ? (
                            <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center">
                                <Leaf className="w-5 h-5 text-white animate-leaf-sway" />
                            </div>
                        ) : (
                            <div className="flex items-center group cursor-default">
                                <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center mr-3">
                                    <Leaf className="w-5 h-5 text-white animate-leaf-sway" />
                                </div>
                                <span className="transition-all duration-300 group-hover:tracking-wider">FreshVeggies</span>
                            </div>
                        )}
                    </div>
                </div>
                {!isCollapsed && (
                    <div className="transition-all duration-300 hover:scale-105">
                        <ThemeToggle />
                    </div>
                )}
            </div>

            {/* Navigation Content */}
            <div
                className={`
                flex-1 py-6 space-y-8 overflow-y-auto overflow-x-hidden
                ${isCollapsed ? "px-2" : "px-4"}
            `}
            >
                {/* Browse Section */}
                <div className="space-y-3">
                    {!isCollapsed && (
                        <h3
                            className="text-xs font-bold uppercase tracking-widest mb-4 px-2 
                                     transition-all duration-300"
                            style={{ color: "hsl(var(--text-disabled))" }}
                        >
                            Browse
                        </h3>
                    )}
                    <nav className="space-y-1">
                        <NavItem icon={<Home size={20} />} text="Home" active collapsed={isCollapsed} />
                        <NavItem icon={<Clock size={20} />} text="Fresh Today" collapsed={isCollapsed} />
                    </nav>
                </div>

                {/* My List Section */}
                <div className="space-y-3">
                    {!isCollapsed && (
                        <h3
                            className="text-xs font-bold text-gray-500 dark:text-gray-400 uppercase tracking-widest mb-4 px-2
                                     transition-all duration-300 hover:text-gray-600 dark:hover:text-gray-300"
                        >
                            My List
                        </h3>
                    )}
                    <nav className="space-y-1">
                        <NavItem icon={<Heart size={20} />} text="Favorites" collapsed={isCollapsed} />
                        <NavItem icon={<Star size={20} />} text="Watchlist" collapsed={isCollapsed} />
                        <NavItem icon={<ShoppingCart size={20} />} text="Downloads" collapsed={isCollapsed} />
                    </nav>
                </div>

                {/* More Section */}
                <div className="space-y-3">
                    {!isCollapsed && (
                        <h3
                            className="text-xs font-bold uppercase tracking-widest mb-4 px-2
                                     transition-all duration-300"
                            style={{ color: "hsl(var(--text-disabled))" }}
                        >
                            My Orders
                        </h3>
                    )}
                    <nav className="space-y-1">
                        <NavItem icon={<Heart size={20} />} text="Favorites" collapsed={isCollapsed} />
                        <NavItem icon={<Star size={20} />} text="Wishlist" collapsed={isCollapsed} />
                        <NavItem icon={<ShoppingCart size={20} />} text="Cart" collapsed={isCollapsed} />
                    </nav>
                </div>
            </div>

            {/* Bottom Section */}
            <div
                className={`
                border-t p-4
                sidebar-transition
                ${isCollapsed ? "px-2" : "px-4"}
            `}
                style={{
                    borderColor: "hsl(var(--divider))",
                    backgroundColor: "hsl(var(--action-hover))",
                }}
            >
                <NavItem
                    icon={<LogOut size={20} />}
                    text="Sign Out"
                    className="hover:shadow-md"
                    style={{
                        color: "hsl(var(--error))",
                        backgroundColor: "hsl(var(--error) / 0.1)",
                    }}
                    collapsed={isCollapsed}
                />
            </div>
        </aside>
    )
}

export default Sidebar
