"use client"

import { useState } from "react"

import { User, Settings, Bell, Shield, Palette, Truck, CreditCard, Key, Leaf } from "lucide-react"
import { AccountPreferences } from "@/widgets/Setting/account-preferences"
import { DeliveryPreferences } from "@/widgets/Setting/delivery-preferences"
import { NotificationSettings } from "@/widgets/Setting/notification-settings"
import { PaymentMethods } from "@/widgets/Setting/payment-methods"
import { PrivacyControls } from "@/widgets/Setting/privacy-controls"
import { ProfileSettings } from "@/widgets/Setting/profile-settings"
import { SecuritySettings } from "@/widgets/Setting/security-settings"
import { ThemeAppearance } from "@/widgets/Setting/theme-appearance"

const settingsTabs = [
    { id: "profile", label: "Profile", icon: User, component: ProfileSettings },
    { id: "preferences", label: "Preferences", icon: Settings, component: AccountPreferences },
    { id: "notifications", label: "Notifications", icon: Bell, component: NotificationSettings },
    { id: "privacy", label: "Privacy", icon: Shield, component: PrivacyControls },
    { id: "appearance", label: "Appearance", icon: Palette, component: ThemeAppearance },
    { id: "delivery", label: "Delivery", icon: Truck, component: DeliveryPreferences },
    { id: "payment", label: "Payment", icon: CreditCard, component: PaymentMethods },
    { id: "security", label: "Security", icon: Key, component: SecuritySettings },
]

export default function SettingsPage() {
    const [activeTab, setActiveTab] = useState("profile")

    const ActiveComponent = settingsTabs.find((tab) => tab.id === activeTab)?.component || ProfileSettings

    return (
        <div className="min-h-screen w-full" style={{ backgroundColor: "hsl(var(--background))" }}>
            <div className="max-w-7xl mx-auto px-4 py-8">
                {/* Header */}
                <div className="mb-8">
                    <div className="flex items-center space-x-3 mb-4">
                        <div className="w-12 h-12 bg-primary rounded-xl flex items-center justify-center shadow-green">
                            <Leaf className="w-6 h-6 text-white" />
                        </div>
                        <div>
                            <h1 className="text-3xl font-bold" style={{ color: "hsl(var(--text-primary))" }}>
                                Settings
                            </h1>
                            <p style={{ color: "hsl(var(--text-secondary))" }}>Manage your account preferences and settings</p>
                        </div>
                    </div>
                </div>

                <div className="flex flex-col lg:flex-row gap-8">
                    {/* Sidebar */}
                    <div className="lg:w-64 flex-shrink-0">
                        <nav className="card p-4 space-y-2">
                            {settingsTabs.map((tab) => {
                                const Icon = tab.icon
                                return (
                                    <button
                                        key={tab.id}
                                        onClick={() => setActiveTab(tab.id)}
                                        className={`w-full flex items-center space-x-3 px-4 py-3 rounded-lg text-left transition-all duration-200 ${activeTab === tab.id
                                                ? "bg-[hsl(var(--action-selected))] shadow-md"
                                                : "hover:bg-[hsl(var(--action-hover))]"
                                            }`}
                                    >
                                        <Icon
                                            className="w-5 h-5"
                                            style={{
                                                color: activeTab === tab.id ? "hsl(var(--primary))" : "hsl(var(--text-secondary))",
                                            }}
                                        />
                                        <span
                                            className="font-medium"
                                            style={{
                                                color: activeTab === tab.id ? "hsl(var(--primary))" : "hsl(var(--text-primary))",
                                            }}
                                        >
                                            {tab.label}
                                        </span>
                                    </button>
                                )
                            })}
                        </nav>
                    </div>

                    {/* Main Content */}
                    <div className="flex-1">
                        <ActiveComponent />
                    </div>
                </div>
            </div>
        </div>
    )
}
