"use client"

import { useState } from "react"
import { Shield, Eye, Lock, Trash2, Download } from "lucide-react"

export function PrivacyControls() {
    const [privacy, setPrivacy] = useState({
        profileVisibility: "private",
        dataCollection: false,
        analytics: true,
        thirdPartySharing: false,
        locationTracking: false,
        cookiePreferences: "essential",
    })

    const [showDeleteModal, setShowDeleteModal] = useState(false)

    const handleSelectChange = (name: string, value: string) => {
        setPrivacy((prev) => ({ ...prev, [name]: value }))
    }

    const handleToggle = (name: string) => {
        setPrivacy((prev) => ({ ...prev, [name]: !prev[name as keyof typeof prev] }))
    }

    const handleDataExport = () => {
        // Simulate data export
        alert("Your data export will be sent to your email within 24 hours.")
    }

    const handleAccountDeletion = () => {
        setShowDeleteModal(true)
    }

    return (
        <div className="card p-6 space-y-6">
            <div className="flex items-center space-x-3">
                <Shield className="w-5 h-5" style={{ color: "hsl(var(--primary))" }} />
                <h2 className="text-xl font-semibold" style={{ color: "hsl(var(--text-primary))" }}>
                    Privacy Controls
                </h2>
            </div>

            {/* Profile Visibility */}
            <div className="space-y-4">
                <h3 className="font-medium flex items-center space-x-2" style={{ color: "hsl(var(--text-primary))" }}>
                    <Eye className="w-4 h-4" />
                    <span>Profile Visibility</span>
                </h3>

                <div className="space-y-2">
                    <label className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                        Who can see your profile
                    </label>
                    <select
                        value={privacy.profileVisibility}
                        onChange={(e) => handleSelectChange("profileVisibility", e.target.value)}
                        className="input w-full px-4 py-3 rounded-lg focus:outline-none"
                    >
                        <option value="public">Public - Anyone can see</option>
                        <option value="private">Private - Only you</option>
                        <option value="friends">Friends - People you follow</option>
                    </select>
                </div>
            </div>

            {/* Data Collection */}
            <div className="space-y-4">
                <h3 className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                    Data Collection & Usage
                </h3>

                <div className="space-y-3">
                    {[
                        {
                            key: "dataCollection",
                            label: "Personalized Recommendations",
                            desc: "Use your order history to suggest products",
                        },
                        {
                            key: "analytics",
                            label: "Analytics & Performance",
                            desc: "Help us improve the app with usage analytics",
                        },
                        {
                            key: "thirdPartySharing",
                            label: "Third-party Sharing",
                            desc: "Share data with partners for better services",
                        },
                        {
                            key: "locationTracking",
                            label: "Location Tracking",
                            desc: "Use location for delivery optimization",
                        },
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
                                className={`relative w-12 h-6 rounded-full transition-colors duration-200 ${privacy[item.key as keyof typeof privacy] ? "bg-primary" : "bg-[hsl(var(--divider))]"
                                    }`}
                            >
                                <div
                                    className={`absolute top-0.5 w-5 h-5 bg-white rounded-full transition-transform duration-200 ${privacy[item.key as keyof typeof privacy] ? "translate-x-6" : "translate-x-0.5"
                                        }`}
                                />
                            </button>
                        </div>
                    ))}
                </div>
            </div>

            {/* Cookie Preferences */}
            <div className="space-y-4">
                <h3 className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                    Cookie Preferences
                </h3>

                <div className="space-y-2">
                    <label className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                        Cookie Settings
                    </label>
                    <select
                        value={privacy.cookiePreferences}
                        onChange={(e) => handleSelectChange("cookiePreferences", e.target.value)}
                        className="input w-full px-4 py-3 rounded-lg focus:outline-none"
                    >
                        <option value="essential">Essential Only</option>
                        <option value="functional">Essential + Functional</option>
                        <option value="all">All Cookies</option>
                    </select>
                    <p className="text-xs" style={{ color: "hsl(var(--text-secondary))" }}>
                        Essential cookies are required for the app to function properly
                    </p>
                </div>
            </div>

            {/* Data Management */}
            <div className="space-y-4">
                <h3 className="font-medium flex items-center space-x-2" style={{ color: "hsl(var(--text-primary))" }}>
                    <Lock className="w-4 h-4" />
                    <span>Data Management</span>
                </h3>

                <div className="space-y-3">
                    <button
                        onClick={handleDataExport}
                        className="w-full flex items-center justify-between p-4 rounded-lg card-elevated hover:shadow-md transition-shadow"
                    >
                        <div className="flex items-center space-x-3">
                            <Download className="w-5 h-5" style={{ color: "hsl(var(--primary))" }} />
                            <div className="text-left">
                                <p className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                    Export Your Data
                                </p>
                                <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                                    Download a copy of your personal data
                                </p>
                            </div>
                        </div>
                    </button>

                    <button
                        onClick={handleAccountDeletion}
                        className="w-full flex items-center justify-between p-4 rounded-lg border border-[hsl(var(--error)/0.3)] bg-[hsl(var(--error)/0.05)] hover:bg-[hsl(var(--error)/0.1)] transition-colors"
                    >
                        <div className="flex items-center space-x-3">
                            <Trash2 className="w-5 h-5" style={{ color: "hsl(var(--error))" }} />
                            <div className="text-left">
                                <p className="font-medium" style={{ color: "hsl(var(--error))" }}>
                                    Delete Account
                                </p>
                                <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                                    Permanently delete your account and data
                                </p>
                            </div>
                        </div>
                    </button>
                </div>
            </div>

            {/* Delete Account Modal */}
            {showDeleteModal && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
                    <div
                        className="w-full max-w-md rounded-xl border shadow-2xl p-6"
                        style={{ backgroundColor: "hsl(var(--paper))", borderColor: "hsl(var(--divider))" }}
                    >
                        <div className="text-center space-y-4">
                            <div className="w-16 h-16 bg-[hsl(var(--error)/0.1)] rounded-full flex items-center justify-center mx-auto">
                                <Trash2 className="w-8 h-8" style={{ color: "hsl(var(--error))" }} />
                            </div>
                            <h3 className="text-lg font-semibold" style={{ color: "hsl(var(--text-primary))" }}>
                                Delete Account
                            </h3>
                            <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                                This action cannot be undone. All your data, orders, and preferences will be permanently deleted.
                            </p>
                            <div className="flex space-x-3">
                                <button
                                    onClick={() => setShowDeleteModal(false)}
                                    className="flex-1 py-2 px-4 rounded-lg font-medium transition-colors"
                                    style={{
                                        backgroundColor: "hsl(var(--action-hover))",
                                        color: "hsl(var(--text-primary))",
                                    }}
                                >
                                    Cancel
                                </button>
                                <button
                                    onClick={() => {
                                        setShowDeleteModal(false)
                                        alert("Account deletion request submitted. You'll receive a confirmation email.")
                                    }}
                                    className="flex-1 py-2 px-4 rounded-lg font-medium text-white"
                                    style={{ backgroundColor: "hsl(var(--error))" }}
                                >
                                    Delete
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}
