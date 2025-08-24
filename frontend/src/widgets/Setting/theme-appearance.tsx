"use client"

import { useState } from "react"
import { Palette, Monitor, Sun, Moon, Smartphone } from "lucide-react"

export function ThemeAppearance() {
    const [appearance, setAppearance] = useState({
        theme: "system",
        accentColor: "green",
        fontSize: "medium",
        compactMode: false,
        animations: true,
        highContrast: false,
    })

    const handleSelectChange = (name: string, value: string) => {
        setAppearance((prev) => ({ ...prev, [name]: value }))
    }

    const handleToggle = (name: string) => {
        setAppearance((prev) => ({ ...prev, [name]: !prev[name as keyof typeof prev] }))
    }

    const themeOptions = [
        { value: "light", label: "Light", icon: Sun },
        { value: "dark", label: "Dark", icon: Moon },
        { value: "system", label: "System", icon: Monitor },
    ]

    const accentColors = [
        { value: "green", label: "Green", color: "hsl(142, 76%, 36%)" },
        { value: "blue", label: "Blue", color: "hsl(200, 98%, 39%)" },
        { value: "purple", label: "Purple", color: "hsl(271, 81%, 56%)" },
        { value: "orange", label: "Orange", color: "hsl(25, 95%, 53%)" },
        { value: "red", label: "Red", color: "hsl(0, 84%, 60%)" },
    ]

    return (
        <div className="card p-6 space-y-6">
            <div className="flex items-center space-x-3">
                <Palette className="w-5 h-5" style={{ color: "hsl(var(--primary))" }} />
                <h2 className="text-xl font-semibold" style={{ color: "hsl(var(--text-primary))" }}>
                    Theme & Appearance
                </h2>
            </div>

            {/* Theme Selection */}
            <div className="space-y-4">
                <h3 className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                    Theme
                </h3>

                <div className="grid grid-cols-3 gap-3">
                    {themeOptions.map((option) => {
                        const Icon = option.icon
                        return (
                            <button
                                key={option.value}
                                onClick={() => handleSelectChange("theme", option.value)}
                                className={`p-4 rounded-lg border-2 transition-all duration-200 ${appearance.theme === option.value
                                        ? "border-[hsl(var(--primary))] bg-[hsl(var(--action-selected))]"
                                        : "border-[hsl(var(--divider))] hover:border-[hsl(var(--primary)/0.5)]"
                                    }`}
                            >
                                <Icon className="w-6 h-6 mx-auto mb-2" style={{ color: "hsl(var(--text-primary))" }} />
                                <p className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                    {option.label}
                                </p>
                            </button>
                        )
                    })}
                </div>
            </div>

            {/* Accent Color */}
            <div className="space-y-4">
                <h3 className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                    Accent Color
                </h3>

                <div className="flex flex-wrap gap-3">
                    {accentColors.map((color) => (
                        <button
                            key={color.value}
                            onClick={() => handleSelectChange("accentColor", color.value)}
                            className={`flex items-center space-x-2 px-4 py-2 rounded-lg border-2 transition-all duration-200 ${appearance.accentColor === color.value
                                    ? "border-[hsl(var(--primary))] bg-[hsl(var(--action-selected))]"
                                    : "border-[hsl(var(--divider))] hover:border-[hsl(var(--primary)/0.5)]"
                                }`}
                        >
                            <div className="w-4 h-4 rounded-full" style={{ backgroundColor: color.color }} />
                            <span className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                {color.label}
                            </span>
                        </button>
                    ))}
                </div>
            </div>

            {/* Font Size */}
            <div className="space-y-4">
                <h3 className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                    Font Size
                </h3>

                <div className="space-y-2">
                    <select
                        value={appearance.fontSize}
                        onChange={(e) => handleSelectChange("fontSize", e.target.value)}
                        className="input w-full px-4 py-3 rounded-lg focus:outline-none"
                    >
                        <option value="small">Small</option>
                        <option value="medium">Medium</option>
                        <option value="large">Large</option>
                        <option value="extra-large">Extra Large</option>
                    </select>
                </div>
            </div>

            {/* Display Options */}
            <div className="space-y-4">
                <h3 className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                    Display Options
                </h3>

                <div className="space-y-3">
                    {[
                        {
                            key: "compactMode",
                            label: "Compact Mode",
                            desc: "Reduce spacing and padding for more content",
                            icon: Smartphone,
                        },
                        {
                            key: "animations",
                            label: "Animations",
                            desc: "Enable smooth transitions and animations",
                            icon: Palette,
                        },
                        {
                            key: "highContrast",
                            label: "High Contrast",
                            desc: "Increase contrast for better accessibility",
                            icon: Monitor,
                        },
                    ].map((item) => {
                        const Icon = item.icon
                        return (
                            <div key={item.key} className="flex items-center justify-between p-3 rounded-lg card-elevated">
                                <div className="flex items-center space-x-3">
                                    <Icon className="w-5 h-5" style={{ color: "hsl(var(--text-secondary))" }} />
                                    <div>
                                        <p className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                            {item.label}
                                        </p>
                                        <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                                            {item.desc}
                                        </p>
                                    </div>
                                </div>
                                <button
                                    onClick={() => handleToggle(item.key)}
                                    className={`relative w-12 h-6 rounded-full transition-colors duration-200 ${appearance[item.key as keyof typeof appearance] ? "bg-primary" : "bg-[hsl(var(--divider))]"
                                        }`}
                                >
                                    <div
                                        className={`absolute top-0.5 w-5 h-5 bg-white rounded-full transition-transform duration-200 ${appearance[item.key as keyof typeof appearance] ? "translate-x-6" : "translate-x-0.5"
                                            }`}
                                    />
                                </button>
                            </div>
                        )
                    })}
                </div>
            </div>

            {/* Preview */}
            <div className="space-y-4">
                <h3 className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                    Preview
                </h3>

                <div className="p-4 rounded-lg border" style={{ borderColor: "hsl(var(--divider))" }}>
                    <div className="flex items-center space-x-3 mb-3">
                        <div className="w-8 h-8 bg-primary rounded-full flex items-center justify-center">
                            <span className="text-white text-sm">🥬</span>
                        </div>
                        <div>
                            <p className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                Fresh Spinach
                            </p>
                            <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                                Organic • $3.99/bunch
                            </p>
                        </div>
                    </div>
                    <button className="btn-primary px-4 py-2 rounded-lg text-sm">Add to Cart</button>
                </div>
            </div>
        </div>
    )
}
