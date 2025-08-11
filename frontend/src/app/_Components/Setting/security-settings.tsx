"use client"

import { useState } from "react"
import { Shield, Key, Smartphone, Eye, EyeOff, AlertTriangle } from "lucide-react"

export function SecuritySettings() {
    const [security, setSecurityState] = useState({
        twoFactorEnabled: false,
        biometricEnabled: true,
        sessionTimeout: "30",
        loginAlerts: true,
        passwordLastChanged: "2024-01-15",
    })

    const [showChangePassword, setShowChangePassword] = useState(false)
    const [passwordForm, setPasswordForm] = useState({
        current: "",
        new: "",
        confirm: "",
    })
    const [showPasswords, setShowPasswords] = useState({
        current: false,
        new: false,
        confirm: false,
    })

    const handleToggle = (setting: string) => {
        setSecurityState((prev) => ({ ...prev, [setting]: !prev[setting as keyof typeof prev] }))
    }

    const handlePasswordChange = async () => {
        // Simulate password change
        await new Promise((resolve) => setTimeout(resolve, 1000))
        setPasswordForm({ current: "", new: "", confirm: "" })
        setShowChangePassword(false)
        setSecurityState((prev) => ({ ...prev, passwordLastChanged: new Date().toISOString().split("T")[0] }))
        alert("Password changed successfully!")
    }

    const togglePasswordVisibility = (field: string) => {
        setShowPasswords((prev) => ({ ...prev, [field]: !prev[field as keyof typeof prev] }))
    }

    return (
        <div className="card p-6 space-y-6">
            <div className="flex items-center space-x-3">
                <Shield className="w-5 h-5" style={{ color: "hsl(var(--primary))" }} />
                <h2 className="text-xl font-semibold" style={{ color: "hsl(var(--text-primary))" }}>
                    Security Settings
                </h2>
            </div>

            {/* Password */}
            <div className="space-y-4">
                <h3 className="font-medium flex items-center space-x-2" style={{ color: "hsl(var(--text-primary))" }}>
                    <Key className="w-4 h-4" />
                    <span>Password</span>
                </h3>

                <div className="flex items-center justify-between p-4 rounded-lg card-elevated">
                    <div>
                        <p className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                            Change Password
                        </p>
                        <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                            Last changed: {new Date(security.passwordLastChanged).toLocaleDateString()}
                        </p>
                    </div>
                    <button onClick={() => setShowChangePassword(true)} className="btn-outline px-4 py-2 rounded-lg font-medium">
                        Change
                    </button>
                </div>
            </div>

            {/* Two-Factor Authentication */}
            <div className="space-y-4">
                <h3 className="font-medium flex items-center space-x-2" style={{ color: "hsl(var(--text-primary))" }}>
                    <Smartphone className="w-4 h-4" />
                    <span>Two-Factor Authentication</span>
                </h3>

                <div className="flex items-center justify-between p-4 rounded-lg card-elevated">
                    <div>
                        <p className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                            SMS Authentication
                        </p>
                        <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                            Receive verification codes via SMS
                        </p>
                    </div>
                    <button
                        onClick={() => handleToggle("twoFactorEnabled")}
                        className={`relative w-12 h-6 rounded-full transition-colors duration-200 ${security.twoFactorEnabled ? "bg-primary" : "bg-[hsl(var(--divider))]"
                            }`}
                    >
                        <div
                            className={`absolute top-0.5 w-5 h-5 bg-white rounded-full transition-transform duration-200 ${security.twoFactorEnabled ? "translate-x-6" : "translate-x-0.5"
                                }`}
                        />
                    </button>
                </div>

                <div className="flex items-center justify-between p-4 rounded-lg card-elevated">
                    <div>
                        <p className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                            Biometric Login
                        </p>
                        <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                            Use fingerprint or face recognition
                        </p>
                    </div>
                    <button
                        onClick={() => handleToggle("biometricEnabled")}
                        className={`relative w-12 h-6 rounded-full transition-colors duration-200 ${security.biometricEnabled ? "bg-primary" : "bg-[hsl(var(--divider))]"
                            }`}
                    >
                        <div
                            className={`absolute top-0.5 w-5 h-5 bg-white rounded-full transition-transform duration-200 ${security.biometricEnabled ? "translate-x-6" : "translate-x-0.5"
                                }`}
                        />
                    </button>
                </div>
            </div>

            {/* Session Management */}
            <div className="space-y-4">
                <h3 className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                    Session Management
                </h3>

                <div className="space-y-2">
                    <label className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                        Auto-logout after inactivity
                    </label>
                    <select
                        value={security.sessionTimeout}
                        onChange={(e) => setSecurityState((prev) => ({ ...prev, sessionTimeout: e.target.value }))}
                        className="input w-full px-4 py-3 rounded-lg focus:outline-none"
                    >
                        <option value="15">15 minutes</option>
                        <option value="30">30 minutes</option>
                        <option value="60">1 hour</option>
                        <option value="120">2 hours</option>
                        <option value="never">Never</option>
                    </select>
                </div>

                <div className="flex items-center justify-between p-4 rounded-lg card-elevated">
                    <div>
                        <p className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                            Login Alerts
                        </p>
                        <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                            Get notified of new device logins
                        </p>
                    </div>
                    <button
                        onClick={() => handleToggle("loginAlerts")}
                        className={`relative w-12 h-6 rounded-full transition-colors duration-200 ${security.loginAlerts ? "bg-primary" : "bg-[hsl(var(--divider))]"
                            }`}
                    >
                        <div
                            className={`absolute top-0.5 w-5 h-5 bg-white rounded-full transition-transform duration-200 ${security.loginAlerts ? "translate-x-6" : "translate-x-0.5"
                                }`}
                        />
                    </button>
                </div>
            </div>

            {/* Active Sessions */}
            <div className="space-y-4">
                <h3 className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                    Active Sessions
                </h3>

                <div className="space-y-3">
                    {[
                        { device: "iPhone 13", location: "New York, NY", current: true, lastActive: "Now" },
                        { device: "MacBook Pro", location: "New York, NY", current: false, lastActive: "2 hours ago" },
                        { device: "Chrome Browser", location: "New York, NY", current: false, lastActive: "1 day ago" },
                    ].map((session, index) => (
                        <div key={index} className="flex items-center justify-between p-4 rounded-lg card-elevated">
                            <div className="flex items-center space-x-3">
                                <div className="w-10 h-10 bg-[hsl(var(--action-hover))] rounded-lg flex items-center justify-center">
                                    <Smartphone className="w-5 h-5" style={{ color: "hsl(var(--text-secondary))" }} />
                                </div>
                                <div>
                                    <p className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                        {session.device}
                                        {session.current && (
                                            <span
                                                className="ml-2 px-2 py-1 text-xs font-medium rounded-full"
                                                style={{
                                                    backgroundColor: "hsl(var(--primary) / 0.1)",
                                                    color: "hsl(var(--primary))",
                                                }}
                                            >
                                                Current
                                            </span>
                                        )}
                                    </p>
                                    <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                                        {session.location} • {session.lastActive}
                                    </p>
                                </div>
                            </div>
                            {!session.current && (
                                <button className="text-sm font-medium hover:underline" style={{ color: "hsl(var(--error))" }}>
                                    Revoke
                                </button>
                            )}
                        </div>
                    ))}
                </div>
            </div>

            {/* Change Password Modal */}
            {showChangePassword && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
                    <div
                        className="w-full max-w-md rounded-xl border shadow-2xl p-6"
                        style={{ backgroundColor: "hsl(var(--paper))", borderColor: "hsl(var(--divider))" }}
                    >
                        <div className="flex items-center justify-between mb-6">
                            <h3 className="text-lg font-semibold" style={{ color: "hsl(var(--text-primary))" }}>
                                Change Password
                            </h3>
                            <button
                                onClick={() => setShowChangePassword(false)}
                                className="text-2xl"
                                style={{ color: "hsl(var(--text-secondary))" }}
                            >
                                ×
                            </button>
                        </div>

                        <div className="space-y-4">
                            <div className="space-y-2">
                                <label className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                    Current Password
                                </label>
                                <div className="relative">
                                    <input
                                        type={showPasswords.current ? "text" : "password"}
                                        value={passwordForm.current}
                                        onChange={(e) => setPasswordForm((prev) => ({ ...prev, current: e.target.value }))}
                                        className="input w-full px-4 py-3 pr-12 rounded-lg focus:outline-none"
                                    />
                                    <button
                                        type="button"
                                        onClick={() => togglePasswordVisibility("current")}
                                        className="absolute right-3 top-1/2 -translate-y-1/2"
                                        style={{ color: "hsl(var(--text-disabled))" }}
                                    >
                                        {showPasswords.current ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                                    </button>
                                </div>
                            </div>

                            <div className="space-y-2">
                                <label className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                    New Password
                                </label>
                                <div className="relative">
                                    <input
                                        type={showPasswords.new ? "text" : "password"}
                                        value={passwordForm.new}
                                        onChange={(e) => setPasswordForm((prev) => ({ ...prev, new: e.target.value }))}
                                        className="input w-full px-4 py-3 pr-12 rounded-lg focus:outline-none"
                                    />
                                    <button
                                        type="button"
                                        onClick={() => togglePasswordVisibility("new")}
                                        className="absolute right-3 top-1/2 -translate-y-1/2"
                                        style={{ color: "hsl(var(--text-disabled))" }}
                                    >
                                        {showPasswords.new ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                                    </button>
                                </div>
                            </div>

                            <div className="space-y-2">
                                <label className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                    Confirm New Password
                                </label>
                                <div className="relative">
                                    <input
                                        type={showPasswords.confirm ? "text" : "password"}
                                        value={passwordForm.confirm}
                                        onChange={(e) => setPasswordForm((prev) => ({ ...prev, confirm: e.target.value }))}
                                        className="input w-full px-4 py-3 pr-12 rounded-lg focus:outline-none"
                                    />
                                    <button
                                        type="button"
                                        onClick={() => togglePasswordVisibility("confirm")}
                                        className="absolute right-3 top-1/2 -translate-y-1/2"
                                        style={{ color: "hsl(var(--text-disabled))" }}
                                    >
                                        {showPasswords.confirm ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                                    </button>
                                </div>
                            </div>

                            <div className="flex items-center space-x-2 p-3 rounded-lg bg-[hsl(var(--warning)/0.1)]">
                                <AlertTriangle className="w-5 h-5" style={{ color: "hsl(var(--warning))" }} />
                                <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                                    Use 8+ characters with uppercase, lowercase, and numbers
                                </p>
                            </div>

                            <div className="flex space-x-3">
                                <button
                                    onClick={() => setShowChangePassword(false)}
                                    className="flex-1 py-2 px-4 rounded-lg font-medium transition-colors"
                                    style={{
                                        backgroundColor: "hsl(var(--action-hover))",
                                        color: "hsl(var(--text-primary))",
                                    }}
                                >
                                    Cancel
                                </button>
                                <button
                                    onClick={handlePasswordChange}
                                    disabled={
                                        !passwordForm.current ||
                                        !passwordForm.new ||
                                        !passwordForm.confirm ||
                                        passwordForm.new !== passwordForm.confirm
                                    }
                                    className="btn-primary flex-1 py-2 px-4 rounded-lg font-medium disabled:opacity-60"
                                >
                                    Update Password
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    )
}
