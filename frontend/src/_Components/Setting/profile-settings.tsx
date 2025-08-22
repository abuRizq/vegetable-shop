"use client"

import type React from "react"

import { useState } from "react"
import { User, Camera, Save, Loader2 } from "lucide-react"

export function ProfileSettings() {
    const [formData, setFormData] = useState({
        firstName: "John",
        lastName: "Doe",
        email: "john.doe@example.com",
        phone: "+1 (555) 123-4567",
        bio: "Love fresh vegetables and healthy eating!",
    })
    const [saving, setSaving] = useState(false)
    const [success, setSuccess] = useState(false)

    const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const { name, value } = e.target
        setFormData((prev) => ({ ...prev, [name]: value }))
        if (success) setSuccess(false)
    }

    const handleSave = async () => {
        setSaving(true)
        await new Promise((resolve) => setTimeout(resolve, 1000))
        setSaving(false)
        setSuccess(true)
        setTimeout(() => setSuccess(false), 3000)
    }

    return (
        <div className="card p-6 space-y-6">
            <div className="flex items-center space-x-4">
                <h2 className="text-xl font-semibold" style={{ color: "hsl(var(--text-primary))" }}>
                    Profile Information
                </h2>
            </div>

            {/* Avatar Section */}
            <div className="flex items-center space-x-4">
                <div className="relative">
                    <div className="w-20 h-20 rounded-full bg-primary flex items-center justify-center">
                        <User className="w-10 h-10 text-white" />
                    </div>
                    <button className="absolute -bottom-1 -right-1 w-8 h-8 bg-secondary rounded-full flex items-center justify-center hover:scale-110 transition-transform">
                        <Camera className="w-4 h-4 text-white" />
                    </button>
                </div>
                <div>
                    <h3 className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                        Profile Photo
                    </h3>
                    <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                        Upload a new photo or change your current one
                    </p>
                </div>
            </div>

            {/* Form Fields */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="space-y-2">
                    <label className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                        First Name
                    </label>
                    <input
                        name="firstName"
                        value={formData.firstName}
                        onChange={handleInputChange}
                        className="input w-full px-4 py-3 rounded-lg focus:outline-none"
                    />
                </div>
                <div className="space-y-2">
                    <label className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                        Last Name
                    </label>
                    <input
                        name="lastName"
                        value={formData.lastName}
                        onChange={handleInputChange}
                        className="input w-full px-4 py-3 rounded-lg focus:outline-none"
                    />
                </div>
            </div>

            <div className="space-y-2">
                <label className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                    Email Address
                </label>
                <input
                    name="email"
                    type="email"
                    value={formData.email}
                    onChange={handleInputChange}
                    className="input w-full px-4 py-3 rounded-lg focus:outline-none"
                />
            </div>

            <div className="space-y-2">
                <label className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                    Phone Number
                </label>
                <input
                    name="phone"
                    type="tel"
                    value={formData.phone}
                    onChange={handleInputChange}
                    className="input w-full px-4 py-3 rounded-lg focus:outline-none"
                />
            </div>

            <div className="space-y-2">
                <label className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                    Bio
                </label>
                <textarea
                    name="bio"
                    value={formData.bio}
                    onChange={handleInputChange}
                    rows={3}
                    className="input w-full px-4 py-3 rounded-lg focus:outline-none resize-none"
                    placeholder="Tell us about yourself..."
                />
            </div>

            {/* Save Button */}
            <div className="flex items-center justify-between pt-4">
                {success && (
                    <p className="text-sm" style={{ color: "hsl(var(--success))" }}>
                        Profile updated successfully!
                    </p>
                )}
                <button
                    onClick={handleSave}
                    disabled={saving}
                    className="btn-primary px-6 py-2 rounded-lg font-medium flex items-center space-x-2 disabled:opacity-60 ml-auto"
                >
                    {saving ? (
                        <>
                            <Loader2 className="w-4 h-4 animate-spin" />
                            <span>Saving...</span>
                        </>
                    ) : (
                        <>
                            <Save className="w-4 h-4" />
                            <span>Save Changes</span>
                        </>
                    )}
                </button>
            </div>
        </div>
    )
}
