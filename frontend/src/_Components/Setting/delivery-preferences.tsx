/* eslint-disable @typescript-eslint/no-explicit-any */
"use client"

import { useState } from "react"
import { Truck, MapPin, Clock, Calendar } from "lucide-react"

export function DeliveryPreferences() {
    const [delivery, setDelivery] = useState({
        defaultAddress: {
            street: "123 Main Street",
            city: "New York",
            state: "NY",
            zip: "10001",
            instructions: "Leave at front door",
        },
        preferredTime: "morning",
        deliveryDays: ["monday", "wednesday", "friday"],
        notifications: {
            onTheWay: true,
            delivered: true,
            delays: true,
        },
        specialInstructions: "Please ring doorbell twice",
    })

    const [isEditingAddress, setIsEditingAddress] = useState(false)

    const handleAddressChange = (field: string, value: string) => {
        setDelivery((prev) => ({
            ...prev,
            defaultAddress: { ...prev.defaultAddress, [field]: value },
        }))
    }

    const handleToggle = (category: string, setting: string) => {
        setDelivery((prev) => ({
            ...prev,
            [category]: {
                ...(prev[category as keyof typeof prev] as Record<string, any>),
                [setting]: !(
                    (prev[category as keyof typeof prev] as Record<string, any>)[setting]
                ),
            },
        }))
    }

    const handleDayToggle = (day: string) => {
        setDelivery((prev) => ({
            ...prev,
            deliveryDays: prev.deliveryDays.includes(day)
                ? prev.deliveryDays.filter((d) => d !== day)
                : [...prev.deliveryDays, day],
        }))
    }

    const days = [
        { key: "monday", label: "Mon" },
        { key: "tuesday", label: "Tue" },
        { key: "wednesday", label: "Wed" },
        { key: "thursday", label: "Thu" },
        { key: "friday", label: "Fri" },
        { key: "saturday", label: "Sat" },
        { key: "sunday", label: "Sun" },
    ]

    return (
        <div className="card p-6 space-y-6">
            <div className="flex items-center space-x-3">
                <Truck className="w-5 h-5" style={{ color: "hsl(var(--primary))" }} />
                <h2 className="text-xl font-semibold" style={{ color: "hsl(var(--text-primary))" }}>
                    Delivery Preferences
                </h2>
            </div>

            {/* Default Address */}
            <div className="space-y-4">
                <div className="flex items-center justify-between">
                    <h3 className="font-medium flex items-center space-x-2" style={{ color: "hsl(var(--text-primary))" }}>
                        <MapPin className="w-4 h-4" />
                        <span>Default Delivery Address</span>
                    </h3>
                    <button
                        onClick={() => setIsEditingAddress(!isEditingAddress)}
                        className="text-sm font-medium hover:underline"
                        style={{ color: "hsl(var(--primary))" }}
                    >
                        {isEditingAddress ? "Save" : "Edit"}
                    </button>
                </div>

                {isEditingAddress ? (
                    <div className="space-y-4">
                        <div className="space-y-2">
                            <label className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                Street Address
                            </label>
                            <input
                                value={delivery.defaultAddress.street}
                                onChange={(e) => handleAddressChange("street", e.target.value)}
                                className="input w-full px-4 py-3 rounded-lg focus:outline-none"
                            />
                        </div>
                        <div className="grid grid-cols-2 gap-4">
                            <div className="space-y-2">
                                <label className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                    City
                                </label>
                                <input
                                    value={delivery.defaultAddress.city}
                                    onChange={(e) => handleAddressChange("city", e.target.value)}
                                    className="input w-full px-4 py-3 rounded-lg focus:outline-none"
                                />
                            </div>
                            <div className="space-y-2">
                                <label className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                    State
                                </label>
                                <input
                                    value={delivery.defaultAddress.state}
                                    onChange={(e) => handleAddressChange("state", e.target.value)}
                                    className="input w-full px-4 py-3 rounded-lg focus:outline-none"
                                />
                            </div>
                        </div>
                        <div className="space-y-2">
                            <label className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                ZIP Code
                            </label>
                            <input
                                value={delivery.defaultAddress.zip}
                                onChange={(e) => handleAddressChange("zip", e.target.value)}
                                className="input w-full px-4 py-3 rounded-lg focus:outline-none"
                            />
                        </div>
                        <div className="space-y-2">
                            <label className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                Delivery Instructions
                            </label>
                            <textarea
                                value={delivery.defaultAddress.instructions}
                                onChange={(e) => handleAddressChange("instructions", e.target.value)}
                                rows={2}
                                className="input w-full px-4 py-3 rounded-lg focus:outline-none resize-none"
                                placeholder="Special instructions for delivery..."
                            />
                        </div>
                    </div>
                ) : (
                    <div className="p-4 rounded-lg card-elevated">
                        <p className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                            {delivery.defaultAddress.street}
                        </p>
                        <p style={{ color: "hsl(var(--text-secondary))" }}>
                            {delivery.defaultAddress.city}, {delivery.defaultAddress.state} {delivery.defaultAddress.zip}
                        </p>
                        {delivery.defaultAddress.instructions && (
                            <p className="text-sm mt-2" style={{ color: "hsl(var(--text-secondary))" }}>
                                Instructions: {delivery.defaultAddress.instructions}
                            </p>
                        )}
                    </div>
                )}
            </div>

            {/* Preferred Delivery Time */}
            <div className="space-y-4">
                <h3 className="font-medium flex items-center space-x-2" style={{ color: "hsl(var(--text-primary))" }}>
                    <Clock className="w-4 h-4" />
                    <span>Preferred Delivery Time</span>
                </h3>

                <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                    {[
                        { value: "morning", label: "Morning", time: "8AM - 12PM" },
                        { value: "afternoon", label: "Afternoon", time: "12PM - 5PM" },
                        { value: "evening", label: "Evening", time: "5PM - 8PM" },
                    ].map((option) => (
                        <button
                            key={option.value}
                            onClick={() => setDelivery((prev) => ({ ...prev, preferredTime: option.value }))}
                            className={`p-4 rounded-lg border-2 transition-all duration-200 ${delivery.preferredTime === option.value
                                    ? "border-[hsl(var(--primary))] bg-[hsl(var(--action-selected))]"
                                    : "border-[hsl(var(--divider))] hover:border-[hsl(var(--primary)/0.5)]"
                                }`}
                        >
                            <p className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                {option.label}
                            </p>
                            <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                                {option.time}
                            </p>
                        </button>
                    ))}
                </div>
            </div>

            {/* Delivery Days */}
            <div className="space-y-4">
                <h3 className="font-medium flex items-center space-x-2" style={{ color: "hsl(var(--text-primary))" }}>
                    <Calendar className="w-4 h-4" />
                    <span>Preferred Delivery Days</span>
                </h3>

                <div className="flex flex-wrap gap-2">
                    {days.map((day) => (
                        <button
                            key={day.key}
                            onClick={() => handleDayToggle(day.key)}
                            className={`px-4 py-2 rounded-lg border-2 transition-all duration-200 ${delivery.deliveryDays.includes(day.key)
                                    ? "border-[hsl(var(--primary))] bg-[hsl(var(--action-selected))]"
                                    : "border-[hsl(var(--divider))] hover:border-[hsl(var(--primary)/0.5)]"
                                }`}
                        >
                            <span className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                {day.label}
                            </span>
                        </button>
                    ))}
                </div>
            </div>

            {/* Delivery Notifications */}
            <div className="space-y-4">
                <h3 className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                    Delivery Notifications
                </h3>

                <div className="space-y-3">
                    {[
                        {
                            key: "onTheWay",
                            label: "Driver is on the way",
                            desc: "Get notified when your order is out for delivery",
                        },
                        { key: "delivered", label: "Order delivered", desc: "Confirmation when your order has been delivered" },
                        { key: "delays", label: "Delivery delays", desc: "Alert me about any delivery delays or issues" },
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
                                onClick={() => handleToggle("notifications", item.key)}
                                className={`relative w-12 h-6 rounded-full transition-colors duration-200 ${delivery.notifications[item.key as keyof typeof delivery.notifications]
                                        ? "bg-primary"
                                        : "bg-[hsl(var(--divider))]"
                                    }`}
                            >
                                <div
                                    className={`absolute top-0.5 w-5 h-5 bg-white rounded-full transition-transform duration-200 ${delivery.notifications[item.key as keyof typeof delivery.notifications]
                                            ? "translate-x-6"
                                            : "translate-x-0.5"
                                        }`}
                                />
                            </button>
                        </div>
                    ))}
                </div>
            </div>

            {/* Special Instructions */}
            <div className="space-y-4">
                <h3 className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                    Special Instructions
                </h3>

                <div className="space-y-2">
                    <textarea
                        value={delivery.specialInstructions}
                        onChange={(e) => setDelivery((prev) => ({ ...prev, specialInstructions: e.target.value }))}
                        rows={3}
                        className="input w-full px-4 py-3 rounded-lg focus:outline-none resize-none"
                        placeholder="Any special instructions for all deliveries..."
                    />
                    <p className="text-xs" style={{ color: "hsl(var(--text-secondary))" }}>
                        These instructions will be applied to all your future orders
                    </p>
                </div>
            </div>
        </div>
    )
}
