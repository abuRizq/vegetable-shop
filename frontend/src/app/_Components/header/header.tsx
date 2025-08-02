"use client"

import { useState } from "react"
import Searchbar from "./Searchbar"
import Image from "next/image"
import { Bell, Settings, ChevronDown, User } from "lucide-react"

function Header() {
    const [showProfileMenu, setShowProfileMenu] = useState(false)

    return (
        <header className="w-full flex flex-row justify-between items-center p-6 bg-white/80 dark:bg-gray-900/80 backdrop-blur-xl border-b border-gray-200/50 dark:border-gray-700/50 sticky top-0 z-40">
            {/* Left Section - Search */}
            <div className="flex-1 max-w-2xl">
                <Searchbar />
            </div>

            {/* Right Section - User Profile & Actions */}
            <div className="flex items-center space-x-4 ml-6 flex-shrink-0">
                {/* Notifications */}
                <button className="relative p-2 rounded-xl bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700 transition-all duration-200 hover:scale-105 group">
                    <Bell
                        size={20}
                        className="text-gray-600 dark:text-gray-300 group-hover:text-gray-900 dark:group-hover:text-white"
                    />
                    {/* Notification Badge */}
                    <div className="absolute -top-1 -right-1 w-3 h-3 bg-red-500 rounded-full animate-pulse">
                        <div className="absolute inset-0 bg-red-500 rounded-full animate-ping opacity-75"></div>
                    </div>
                </button>

                {/* Settings */}
                <button className="p-2 rounded-xl bg-gray-100 dark:bg-gray-800 hover:bg-gray-200 dark:hover:bg-gray-700 transition-all duration-200 hover:scale-105 group">
                    <Settings
                        size={20}
                        className="text-gray-600 dark:text-gray-300 group-hover:text-gray-900 dark:group-hover:text-white group-hover:rotate-90 transition-transform duration-300"
                    />
                </button>

                {/* Profile Section */}
                <div className="relative">
                    <button
                        onClick={() => setShowProfileMenu(!showProfileMenu)}
                        className="flex items-center space-x-3 p-2 rounded-xl hover:bg-gray-100 dark:hover:bg-gray-800 transition-all duration-200 hover:scale-105 group"
                    >
                        {/* User Info */}
                        <div className="text-right hidden sm:block">
                            <div className="text-sm font-semibold text-gray-900 dark:text-white group-hover:text-red-600 dark:group-hover:text-red-400 transition-colors duration-200">
                                John Doe
                            </div>
                            <div className="text-xs text-gray-500 dark:text-gray-400">Premium Member</div>
                        </div>

                        {/* Avatar */}
                        <div className="relative">
                            <div className="w-10 h-10 rounded-full overflow-hidden ring-2 ring-gray-200 dark:ring-gray-700 group-hover:ring-red-500 transition-all duration-300">
                                <Image
                                    src="/avatars/user1.jpg"
                                    alt="User Avatar"
                                    width={40}
                                    height={40}
                                    className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-300"
                                />
                            </div>
                            {/* Online Status */}
                            <div className="absolute -bottom-0.5 -right-0.5 w-3 h-3 bg-green-500 rounded-full border-2 border-white dark:border-gray-900"></div>
                        </div>

                        {/* Dropdown Arrow */}
                        <ChevronDown
                            size={16}
                            className={`text-gray-400 transition-transform duration-200 ${showProfileMenu ? "rotate-180" : ""}`}
                        />
                    </button>

                    {/* Profile Dropdown Menu */}
                    {showProfileMenu && (
                        <div className="absolute right-0 top-full mt-2 w-64 bg-white dark:bg-gray-800 rounded-xl shadow-2xl border border-gray-200 dark:border-gray-700 overflow-hidden animate-slide-down z-50">
                            {/* User Info Header */}
                            <div className="p-4 bg-gradient-to-r from-red-50 to-pink-50 dark:from-red-900/20 dark:to-pink-900/20 border-b border-gray-200 dark:border-gray-700">
                                <div className="flex items-center space-x-3">
                                    <div className="w-12 h-12 rounded-full overflow-hidden">
                                        <Image
                                            src="/avatars/user1-avatar.jpg"
                                            alt="User Avatar"
                                            width={48}
                                            height={48}
                                            className="w-full h-full object-cover"
                                        />
                                    </div>
                                    <div>
                                        <div className="font-semibold text-gray-900 dark:text-white">John Doe</div>
                                        <div className="text-sm text-gray-500 dark:text-gray-400">john.doe@email.com</div>
                                    </div>
                                </div>
                            </div>

                            {/* Menu Items */}
                            <div className="p-2">
                                {[
                                    { icon: User, label: "My Profile", desc: "Manage your account" },
                                    { icon: Settings, label: "Settings", desc: "Preferences & privacy" },
                                    { icon: Bell, label: "Notifications", desc: "Manage notifications" },
                                ].map((item, index) => (
                                    <button
                                        key={item.label}
                                        className="w-full flex items-center space-x-3 p-3 rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-all duration-200 group"
                                        style={{ animationDelay: `${index * 50}ms` }}
                                    >
                                        <item.icon
                                            size={18}
                                            className="text-gray-500 dark:text-gray-400 group-hover:text-red-500 transition-colors duration-200"
                                        />
                                        <div className="flex-1 text-left">
                                            <div className="text-sm font-medium text-gray-900 dark:text-white">{item.label}</div>
                                            <div className="text-xs text-gray-500 dark:text-gray-400">{item.desc}</div>
                                        </div>
                                    </button>
                                ))}
                            </div>

                            {/* Logout */}
                            <div className="border-t border-gray-200 dark:border-gray-700 p-2">
                                <button className="w-full flex items-center space-x-3 p-3 rounded-lg hover:bg-red-50 dark:hover:bg-red-900/20 transition-all duration-200 group">
                                    <div className="w-5 h-5 rounded-full bg-red-500 flex items-center justify-center">
                                        <div className="w-2 h-2 bg-white rounded-full"></div>
                                    </div>
                                    <span className="text-sm font-medium text-red-600 dark:text-red-400">Sign Out</span>
                                </button>
                            </div>
                        </div>
                    )}
                </div>
            </div>

            {/* Click outside to close dropdown */}
            {showProfileMenu && <div className="fixed inset-0 z-30" onClick={() => setShowProfileMenu(false)} />}
        </header>
    )
}

export default Header
