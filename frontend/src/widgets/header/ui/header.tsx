"use client"

import { useState } from "react"
import { Bell, Settings, ChevronDown, User, ShoppingCart } from "lucide-react"
import Searchbar from "./search-bar";
import Link from "next/link"
import ThemeToggle from "../../../shared/ui/theme-toggle"
import { useRouter } from "next/navigation";
import { useLogoutMutation } from "@/features/auth/logout/api/use-logout";
import { useUser } from "@/entities/user";

function Header() {
  const [showProfileMenu, setShowProfileMenu] = useState(false)
  // const test = useUser()
  // // console.log((test) , "test");
  
  const { data:user, isLoading } = useUser();
  const router = useRouter();
//  const user = data?.data;
  // console.log(JSON.stringify(test) , "frrr");
  
  // const user = data?.data
  const { mutate: logout} = useLogoutMutation({
    onSuccess: () => {
      console.log("Logged out successfully");
      router.push("/");
    },
    onError: (error) => {
      console.error("Logout error:", error.message);
    }
  });

  const onSubmit = () => {
    setShowProfileMenu(false);
    logout();
  };

  return (
    <header
      className="w-full flex flex-row justify-between items-center p-6 backdrop-blur-xl border-b sticky top-0 z-40"
      style={{
        backgroundColor: "hsl(var(--paper) / 0.8)",
        borderColor: "hsl(var(--divider))",
      }}
    >
      {/* Left Section - Search */}
      <div className="flex-1 max-w-2xl">
        <Searchbar />
      </div>
      
      {/* Right Section - User Profile & Actions */}
      <div className="flex items-center space-x-4 ml-6 flex-shrink-0">
        {/* Notifications */}
        <div className="transition-all duration-300 hover:scale-105">
          <ThemeToggle />
        </div>
        <div
          className="relative p-2 rounded-xl transition-all duration-200 hover:scale-105 group"
          style={{ backgroundColor: "hsl(var(--elevated))" }}
        >
          <ShoppingCart
            size={20}
            style={{ color: "hsl(var(--text-secondary))" }}
            className="group-hover:text-primary-color transition-colors duration-200"
          />

          {/* Notification Badge */}
          <div className="absolute -top-1 -right-1 w-3 h-3 bg-secondary rounded-full animate-pulse">
            <div className="absolute inset-0 bg-secondary rounded-full animate-ping opacity-75"></div>
          </div>
        </div>

        {/* Settings */}
        <Link
          href={"/settings"}
          className="p-2 rounded-xl transition-all duration-200 hover:scale-105 group"
          style={{ backgroundColor: "hsl(var(--elevated))" }}
        >
          <Settings
            size={20}
            style={{ color: "hsl(var(--text-secondary))" }}
            className="group-hover:text-primary-color group-hover:rotate-90 transition-all duration-300"
          />
        </Link>

        {/* Profile Section */}

        <div className="relative">
          {isLoading ? (
            <div className="w-10 h-10 rounded-full animate-pulse" style={{ backgroundColor: "hsl(var(--elevated))" }}></div>
          ) : user ? (
            <button
              onClick={() => setShowProfileMenu(!showProfileMenu)}
              className="flex items-center space-x-3 p-2 rounded-xl transition-all duration-200 hover:scale-105 group"
              style={{ backgroundColor: "hsl(var(--elevated))" }}
            >
              {/* User Info */}
              <div className="text-right hidden sm:block">
                <div
                  className="text-sm font-semibold group-hover:text-primary-color transition-colors duration-200"
                  style={{ color: "hsl(var(--text-primary))" }}
                >
                  {user.name}
                </div>
                <div
                  className="text-xs"
                  style={{ color: "hsl(var(--text-disabled))" }}
                >
                  Premium Member
                </div>
              </div>

              {/* Avatar */}
              <div className="relative">
                <div
                  className="w-10 h-10 rounded-full overflow-hidden ring-2 transition-all duration-300"
                  style={{
                    backgroundColor: "hsl(var(--primary))",
                    borderColor: "hsl(var(--divider))",
                  }}
                >
                  <div className="w-full h-full bg-primary flex items-center justify-center">
                    <User className="w-6 h-6 text-white" />
                  </div>
                </div>
                {/* Online Status */}
                <div
                  className="absolute -bottom-0.5 -right-0.5 w-3 h-3 rounded-full border-2"
                  style={{
                    backgroundColor: "hsl(var(--accent))",
                    borderColor: "hsl(var(--paper))",
                  }}
                ></div>
              </div>

              {/* Dropdown Arrow */}
              <ChevronDown
                size={16}
                style={{ color: "hsl(var(--text-disabled))" }}
                className={`transition-transform duration-200 ${showProfileMenu ? "rotate-180" : ""
                  }`}
              />
            </button>
          ) : (
            <Link
              href="/login"
              className="px-4 py-2 rounded-xl text-sm font-semibold transition-all duration-200 border"
              style={{
                backgroundColor: "hsl(var(--paper))",
                color: "hsl(var(--primary))",
                borderColor: "hsl(var(--primary))",
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.backgroundColor = "hsl(var(--primary))";
                e.currentTarget.style.color = "#fff";
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.backgroundColor = "hsl(var(--paper))";
                e.currentTarget.style.color = "hsl(var(--primary))";
              }}
            >
              Log In
            </Link>
          )}
          {/* Profile Dropdown Menu */}
          {showProfileMenu && (
            <div
              className="absolute right-0 top-full mt-2 w-64 rounded-xl shadow-2xl border overflow-hidden animate-slide-down z-50"
              style={{
                backgroundColor: "hsl(var(--paper))",
                borderColor: "hsl(var(--divider))",
                boxShadow: "var(--shadow-lg)",
              }}
            >
              {/* User Info Header */}
              <div
                className="p-4 border-b"
                style={{
                  backgroundColor: "hsl(var(--action-selected))",
                  borderColor: "hsl(var(--divider))",
                }}
              >
                <div className="flex items-center space-x-3">
                  <div className="w-12 h-12 rounded-full bg-primary flex items-center justify-center">
                    <User className="w-6 h-6 text-white" />
                  </div>
                  <div>
                    <div
                      className="font-semibold"
                      style={{ color: "hsl(var(--text-primary))" }}
                    >
                      {user?.name}
                    </div>
                    <div
                      className="text-sm"
                      style={{ color: "hsl(var(--text-secondary))" }}
                    >
                      {user?.email}
                    </div>
                  </div>
                </div>
              </div>

              {/* Menu Items */}
              <div className="p-2">
                {[
                  {
                    icon: User,
                    label: "My Profile",
                    desc: "Manage your account",
                  },
                  {
                    icon: Settings,
                    label: "Settings",
                    desc: "Preferences & privacy",
                  },
                  {
                    icon: Bell,
                    label: "Notifications",
                    desc: "Manage notifications",
                  },
                ].map((item, index) => (
                  <button
                    key={item.label}
                    className="w-full flex items-center space-x-3 p-3 rounded-lg transition-all duration-200 group hover:shadow-md"
                    style={{ animationDelay: `${index * 50}ms` }}
                    onMouseEnter={(e) => {
                      e.currentTarget.style.backgroundColor =
                        "hsl(var(--action-hover))";
                    }}
                    onMouseLeave={(e) => {
                      e.currentTarget.style.backgroundColor = "transparent";
                    }}
                  >
                    <item.icon
                      size={18}
                      style={{ color: "hsl(var(--text-secondary))" }}
                      className="group-hover:text-primary-color transition-colors duration-200"
                    />
                    <div className="flex-1 text-left">
                      <div
                        className="text-sm font-medium"
                        style={{ color: "hsl(var(--text-primary))" }}
                      >
                        {item.label}
                      </div>
                      <div
                        className="text-xs"
                        style={{ color: "hsl(var(--text-disabled))" }}
                      >
                        {item.desc}
                      </div>
                    </div>
                  </button>
                ))}
              </div>

              {/* Logout */}
              <div
                className="border-t p-2"
                style={{ borderColor: "hsl(var(--divider))" }}
              >
                <button
                  onClick={onSubmit}
                  className="w-full flex items-center space-x-3 p-3 rounded-lg transition-all duration-200 group"
                  style={{
                    backgroundColor: "hsl(var(--error) / 0.1)",
                    color: "hsl(var(--error))",
                  }}
                >
                  <div className="w-5 h-5 rounded-full bg-error flex items-center justify-center">
                    <div className="w-2 h-2 bg-white rounded-full"></div>
                  </div>
                  <span className="text-sm font-medium">Sign Out</span>
                </button>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Click outside to close dropdown */}
      {showProfileMenu && (
        <div
          className="fixed inset-0 z-30"
          onClick={() => setShowProfileMenu(false)}
        />
      )}
    </header>
  );
}

export { Header }
