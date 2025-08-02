"use client"

import { useState } from "react"

import { Home, Heart, Clock, ShoppingCart, Tag, Settings, LogOut, Star, ChevronLeft, ChevronRight } from "lucide-react"
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
            bg-gradient-to-b from-white via-gray-50/50 to-white 
            dark:from-gray-900 dark:via-gray-900/80 dark:to-gray-900
            border-r border-gray-200/60 dark:border-gray-700/60
            shadow-2xl shadow-gray-200/40 dark:shadow-black/30
            transition-all duration-500 ease-in-out relative
            backdrop-blur-xl
            ${isCollapsed ? "w-[80px]" : "w-[280px]"}
        `}
        >
            {/* Toggle Button */}
            <button
                onClick={toggleSidebar}
                className="
                    absolute -right-3 top-8 z-20
                    w-7 h-7 bg-gradient-to-br from-white to-gray-100 dark:from-gray-800 dark:to-gray-900
                    border border-gray-200/80 dark:border-gray-600/80
                    rounded-full shadow-xl shadow-gray-300/50 dark:shadow-black/50
                    flex items-center justify-center
                    hover:shadow-2xl hover:scale-110 active:scale-95
                    transition-all duration-300 ease-out
                    text-gray-600 dark:text-gray-300
                    hover:text-gray-900 dark:hover:text-white
                    hover:border-gray-300 dark:hover:border-gray-500
                    group
                "
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
                border-b border-gray-100/80 dark:border-gray-800/80
                transition-all duration-500 ease-out
                bg-gradient-to-r from-transparent via-gray-50/30 to-transparent
                dark:from-transparent dark:via-gray-800/20 dark:to-transparent
                ${isCollapsed ? "px-4" : "px-6"}
            `}
            >
                <div className="flex items-center overflow-hidden">
                    <div className="flex items-center font-bold text-xl text-gray-900 dark:text-white tracking-tight whitespace-nowrap">
                        {isCollapsed ? (
                            <span className="text-[#e50914] text-2xl font-black drop-shadow-sm hover:scale-110 transition-transform duration-300">
                                N
                            </span>
                        ) : (
                            <div className="flex items-center group cursor-default">
                                <span className="transition-all duration-300 group-hover:tracking-wider">Netflix</span>
                                <span className="text-[#e50914] text-2xl ml-0.5 font-black drop-shadow-sm group-hover:scale-110 transition-transform duration-300">
                                    .
                                </span>
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
                flex-1 py-6 space-y-8 overflow-hidden
                ${isCollapsed ? "px-2" : "px-4"}
            `}
            >
                {/* Browse Section */}
                <div className="space-y-3">
                    {!isCollapsed && (
                        <h3
                            className="text-xs font-bold text-gray-500 dark:text-gray-400 uppercase tracking-widest mb-4 px-2 
                                     transition-all duration-300 hover:text-gray-600 dark:hover:text-gray-300"
                        >
                            Browse
                        </h3>
                    )}
                    <nav className="space-y-1">
                        <NavItem icon={<Home size={20} />} text="Home" active collapsed={isCollapsed} />
                        <NavItem icon={<Clock size={20} />} text="Recently Added" collapsed={isCollapsed} />
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
                            className="text-xs font-bold text-gray-500 dark:text-gray-400 uppercase tracking-widest mb-4 px-2
                                     transition-all duration-300 hover:text-gray-600 dark:hover:text-gray-300"
                        >
                            More
                        </h3>
                    )}
                    <nav className="space-y-1">
                        <NavItem icon={<Tag size={20} />} text="Offers" collapsed={isCollapsed} />
                        <NavItem icon={<Settings size={20} />} text="Settings" collapsed={isCollapsed} />
                    </nav>
                </div>
            </div>

            {/* Bottom Section */}
            <div
                className={`
                border-t border-gray-100/80 dark:border-gray-800/80 p-4
                bg-gradient-to-r from-transparent via-gray-50/30 to-transparent
                dark:from-transparent dark:via-gray-800/20 dark:to-transparent
                ${isCollapsed ? "px-2" : "px-4"}
            `}
            >
                <NavItem
                    icon={<LogOut size={20} />}
                    text="Sign Out"
                    className="text-red-600 dark:text-red-400 hover:bg-red-50/80 dark:hover:bg-red-900/30 
                             hover:text-red-700 dark:hover:text-red-300 hover:shadow-red-200/20 dark:hover:shadow-red-900/20"
                    collapsed={isCollapsed}
                />
            </div>
        </aside>
    )
}

export default Sidebar
