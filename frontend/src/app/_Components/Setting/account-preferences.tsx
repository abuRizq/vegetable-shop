"use client"

import { useState } from "react"
import { Settings, Globe, Clock } from "lucide-react"

export function AccountPreferences() {
    const [preferences, setPreferences] = useState({
        language: "en",
        timezone: "America/New_York",
        currency: "USD",
        dateFormat: "MM/DD/YYYY",
        orderReminders: true,
        marketingEmails: false,
        weeklyDigest: true,
    })

    const handleSelectChange = (name: string, value: string) => {
        setPreferences((prev) => ({ ...prev, [name]: value }))
    }

    const handleToggle = (name: string) => {
        setPreferences((prev) => ({ ...prev, [name]: !prev[name as keyof typeof prev] }))
    }

    return (
        <div className="card p-6 space-y-6">
            <div className="flex items-center space-x-3">
                <Settings className="w-5 h-5" style={{ color: "hsl(var(--primary))" }} />
                <h2 className="text-xl font-semibold" style={{ color: "hsl(var(--text-primary))" }}>
                    Account Preferences
                </h2>
            </div>

            {/* Language & Region */}
            <div className="space-y-4">
                <h3 className="font-medium flex items-center space-x-2" style={{ color: "hsl(var(--text-primary))" }}>
                    <Globe className="w-4 h-4" />
                    <span>Language & Region</span>
                </h3>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="space-y-2">
                        <label className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                            Language
                        </label>
                        <select
                            value={preferences.language}
                            onChange={(e) => handleSelectChange("language", e.target.value)}
                            className="input w-full px-4 py-3 rounded-lg focus:outline-none"
                        >
                            <option value="en">English</option>
                            <option value="es">Español</option>
                            <option value="fr">Français</option>
                            <option value="de">Deutsch</option>
                        </select>
                    </div>

                    <div className="space-y-2">
                        <label className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                            Timezone
                        </label>
                        <select
                            value={preferences.timezone}
                            onChange={(e) => handleSelectChange("timezone", e.target.value)}
                            className="input w-full px-4 py-3 rounded-lg focus:outline-none"
                        >
                            <option value="America/New_York">Eastern Time</option>
                            <option value="America/Chicago">Central Time</option>
                            <option value="America/Denver">Mountain Time</option>
                            <option value="America/Los_Angeles">Pacific Time</option>
                        </select>
                    </div>
                </div>
            </div>

            {/* Format Preferences */}
            <div className="space-y-4">
                <h3 className="font-medium flex items-center space-x-2" style={{ color: "hsl(var(--text-primary))" }}>
                    <Clock className="w-4 h-4" />
                    <span>Format Preferences</span>
                </h3>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="space-y-2">
                        <label className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                            Currency
                        </label>
                        <select
                            value={preferences.currency}
                            onChange={(e) => handleSelectChange("currency", e.target.value)}
                            className="input w-full px-4 py-3 rounded-lg focus:outline-none"
                        >
                            <option value="USD">USD ($)</option>
                            <option value="EUR">EUR (€)</option>
                            <option value="GBP">GBP (£)</option>
                            <option value="CAD">CAD (C$)</option>
                        </select>
                    </div>

                    <div className="space-y-2">
                        <label className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                            Date Format
                        </label>
                        <select
                            value={preferences.dateFormat}
                            onChange={(e) => handleSelectChange("dateFormat", e.target.value)}
                            className="input w-full px-4 py-3 rounded-lg focus:outline-none"
                        >
                            <option value="MM/DD/YYYY">MM/DD/YYYY</option>
                            <option value="DD/MM/YYYY">DD/MM/YYYY</option>
                            <option value="YYYY-MM-DD">YYYY-MM-DD</option>
                        </select>
                    </div>
                </div>
            </div>

            {/* Communication Preferences */}
            <div className="space-y-4">
                <h3 className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                    Communication Preferences
                </h3>

                <div className="space-y-3">
                    {[
                        { key: "orderReminders", label: "Order Reminders", desc: "Get notified about your order status" },
                        { key: "marketingEmails", label: "Marketing Emails", desc: "Receive promotional offers and deals" },
                        { key: "weeklyDigest", label: "Weekly Digest", desc: "Weekly summary of fresh arrivals and tips" },
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
                                onClick={() => handleToggle(item.key)}
                                className={`relative w-12 h-6 rounded-full transition-colors duration-200 ${preferences[item.key as keyof typeof preferences] ? "bg-primary" : "bg-[hsl(var(--divider))]"
                                    }`}
                            >
                                <div
                                    className={`absolute top-0.5 w-5 h-5 bg-white rounded-full transition-transform duration-200 ${preferences[item.key as keyof typeof preferences] ? "translate-x-6" : "translate-x-0.5"
                                        }`}
                                />
                            </button>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    )
}
