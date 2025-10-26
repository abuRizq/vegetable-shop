"use client"

import { useState } from "react"
import { Bell, Mail, Smartphone, Volume2 } from "lucide-react"

export function NotificationSettings() {
    const [notifications, setNotifications] = useState({
        email: {
            orderUpdates: true,
            promotions: false,
            newsletter: true,
            security: true,
        },
        push: {
            orderUpdates: true,
            promotions: false,
            reminders: true,
            newArrivals: false,
        },
        sms: {
            orderUpdates: false,
            deliveryAlerts: true,
            promotions: false,
        },
        sound: {
            enabled: true,
            volume: 70,
        },
    })

    const handleToggle = (category: string, setting: string) => {
        setNotifications((prev) => ({
            ...prev,
            [category]: {
                ...prev[category as keyof typeof prev],
                [setting]: !prev[category as keyof typeof prev][setting as keyof typeof prev[keyof typeof prev]],
            },
        }))
    }

    const handleVolumeChange = (volume: number) => {
        setNotifications((prev) => ({
            ...prev,
            sound: { ...prev.sound, volume },
        }))
    }

    return (
        <div className="card p-6 space-y-6">
            <div className="flex items-center space-x-3">
                <Bell className="w-5 h-5" style={{ color: "hsl(var(--primary))" }} />
                <h2 className="text-xl font-semibold" style={{ color: "hsl(var(--text-primary))" }}>
                    Notification Settings
                </h2>
            </div>

            {/* Email Notifications */}
            <div className="space-y-4">
                <h3 className="font-medium flex items-center space-x-2" style={{ color: "hsl(var(--text-primary))" }}>
                    <Mail className="w-4 h-4" />
                    <span>Email Notifications</span>
                </h3>

                <div className="space-y-3">
                    {[
                        { key: "orderUpdates", label: "Order Updates", desc: "Status changes and delivery notifications" },
                        { key: "promotions", label: "Promotions", desc: "Special offers and seasonal deals" },
                        { key: "newsletter", label: "Newsletter", desc: "Weekly fresh tips and recipes" },
                        { key: "security", label: "Security Alerts", desc: "Account security and login notifications" },
                    ].map((item) => (
                        <div key={item.key} className="flex items-center justify-between p-3 rounded-lg card-elevated">
                            <div>
                                <p className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                    {item.label}
                                </p>
                                <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                                    {item.desc}
                                </p>
                            </div>
                            <button
                                onClick={() => handleToggle("email", item.key)}
                                className={`relative w-12 h-6 rounded-full transition-colors duration-200 ${notifications.email[item.key as keyof typeof notifications.email]
                                        ? "bg-primary"
                                        : "bg-[hsl(var(--divider))]"
                                    }`}
                            >
                                <div
                                    className={`absolute top-0.5 w-5 h-5 bg-white rounded-full transition-transform duration-200 ${notifications.email[item.key as keyof typeof notifications.email]
                                            ? "translate-x-6"
                                            : "translate-x-0.5"
                                        }`}
                                />
                            </button>
                        </div>
                    ))}
                </div>
            </div>

            {/* Push Notifications */}
            <div className="space-y-4">
                <h3 className="font-medium flex items-center space-x-2" style={{ color: "hsl(var(--text-primary))" }}>
                    <Smartphone className="w-4 h-4" />
                    <span>Push Notifications</span>
                </h3>

                <div className="space-y-3">
                    {[
                        { key: "orderUpdates", label: "Order Updates", desc: "Real-time order status updates" },
                        { key: "promotions", label: "Promotions", desc: "Flash sales and limited-time offers" },
                        { key: "reminders", label: "Reminders", desc: "Cart reminders and reorder suggestions" },
                        { key: "newArrivals", label: "New Arrivals", desc: "Fresh seasonal produce notifications" },
                    ].map((item) => (
                        <div key={item.key} className="flex items-center justify-between p-3 rounded-lg card-elevated">
                            <div>
                                <p className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                    {item.label}
                                </p>
                                <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                                    {item.desc}
                                </p>
                            </div>
                            <button
                                onClick={() => handleToggle("push", item.key)}
                                className={`relative w-12 h-6 rounded-full transition-colors duration-200 ${notifications.push[item.key as keyof typeof notifications.push]
                                        ? "bg-primary"
                                        : "bg-[hsl(var(--divider))]"
                                    }`}
                            >
                                <div
                                    className={`absolute top-0.5 w-5 h-5 bg-white rounded-full transition-transform duration-200 ${notifications.push[item.key as keyof typeof notifications.push]
                                            ? "translate-x-6"
                                            : "translate-x-0.5"
                                        }`}
                                />
                            </button>
                        </div>
                    ))}
                </div>
            </div>

            {/* SMS Notifications */}
            <div className="space-y-4">
                <h3 className="font-medium flex items-center space-x-2" style={{ color: "hsl(var(--text-primary))" }}>
                    <Smartphone className="w-4 h-4" />
                    <span>SMS Notifications</span>
                </h3>

                <div className="space-y-3">
                    {[
                        { key: "orderUpdates", label: "Order Updates", desc: "Critical order status via SMS" },
                        { key: "deliveryAlerts", label: "Delivery Alerts", desc: "Driver arrival and delivery confirmations" },
                        { key: "promotions", label: "Promotions", desc: "Exclusive SMS-only deals" },
                    ].map((item) => (
                        <div key={item.key} className="flex items-center justify-between p-3 rounded-lg card-elevated">
                            <div>
                                <p className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                    {item.label}
                                </p>
                                <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                                    {item.desc}
                                </p>
                            </div>
                            <button
                                onClick={() => handleToggle("sms", item.key)}
                                className={`relative w-12 h-6 rounded-full transition-colors duration-200 ${notifications.sms[item.key as keyof typeof notifications.sms]
                                        ? "bg-primary"
                                        : "bg-[hsl(var(--divider))]"
                                    }`}
                            >
                                <div
                                    className={`absolute top-0.5 w-5 h-5 bg-white rounded-full transition-transform duration-200 ${notifications.sms[item.key as keyof typeof notifications.sms] ? "translate-x-6" : "translate-x-0.5"
                                        }`}
                                />
                            </button>
                        </div>
                    ))}
                </div>
            </div>

            {/* Sound Settings */}
            <div className="space-y-4">
                <h3 className="font-medium flex items-center space-x-2" style={{ color: "hsl(var(--text-primary))" }}>
                    <Volume2 className="w-4 h-4" />
                    <span>Sound Settings</span>
                </h3>

                <div className="space-y-4">
                    <div className="flex items-center justify-between p-3 rounded-lg card-elevated">
                        <div>
                            <p className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                Enable Sounds
                            </p>
                            <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                                Play notification sounds
                            </p>
                        </div>
                        <button
                            onClick={() => handleToggle("sound", "enabled")}
                            className={`relative w-12 h-6 rounded-full transition-colors duration-200 ${notifications.sound.enabled ? "bg-primary" : "bg-[hsl(var(--divider))]"
                                }`}
                        >
                            <div
                                className={`absolute top-0.5 w-5 h-5 bg-white rounded-full transition-transform duration-200 ${notifications.sound.enabled ? "translate-x-6" : "translate-x-0.5"
                                    }`}
                            />
                        </button>
                    </div>

                    {notifications.sound.enabled && (
                        <div className="p-3 rounded-lg card-elevated">
                            <div className="flex items-center justify-between mb-2">
                                <p className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                    Volume
                                </p>
                                <span className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                                    {notifications.sound.volume}%
                                </span>
                            </div>
                            <input
                                type="range"
                                min="0"
                                max="100"
                                value={notifications.sound.volume}
                                onChange={(e) => handleVolumeChange(Number(e.target.value))}
                                className="w-full h-2 bg-[hsl(var(--divider))] rounded-lg appearance-none cursor-pointer slider"
                                style={{
                                    background: `linear-gradient(to right, hsl(var(--primary)) 0%, hsl(var(--primary)) ${notifications.sound.volume}%, hsl(var(--divider)) ${notifications.sound.volume}%, hsl(var(--divider)) 100%)`,
                                }}
                            />
                        </div>
                    )}
                </div>
            </div>
        </div>
    )
}
