"use client"

import { useState } from "react"
import { CreditCard, Plus, Trash2, Shield } from "lucide-react"

type PaymentMethod = {
    id: string
    type: "card" | "paypal" | "apple_pay"
    last4?: string
    brand?: string
    expiryMonth?: number
    expiryYear?: number
    isDefault: boolean
    email?: string
}

export function PaymentMethods() {
    const [paymentMethods, setPaymentMethods] = useState<PaymentMethod[]>([
        {
            id: "1",
            type: "card",
            last4: "4242",
            brand: "visa",
            expiryMonth: 12,
            expiryYear: 2025,
            isDefault: true,
        },
        {
            id: "2",
            type: "paypal",
            email: "john.doe@example.com",
            isDefault: false,
        },
    ])

    const [showAddCard, setShowAddCard] = useState(false)
    const [newCard, setNewCard] = useState({
        number: "",
        expiry: "",
        cvc: "",
        name: "",
    })

    const handleSetDefault = (id: string) => {
        setPaymentMethods((prev) =>
            prev.map((method) => ({
                ...method,
                isDefault: method.id === id,
            })),
        )
    }

    const handleRemove = (id: string) => {
        setPaymentMethods((prev) => prev.filter((method) => method.id !== id))
    }

    const handleAddCard = () => {
        // Simulate adding a new card
        const newMethod: PaymentMethod = {
            id: Date.now().toString(),
            type: "card",
            last4: newCard.number.slice(-4),
            brand: "visa", // Would be detected from card number
            expiryMonth: Number.parseInt(newCard.expiry.split("/")[0]),
            expiryYear: Number.parseInt("20" + newCard.expiry.split("/")[1]),
            isDefault: paymentMethods.length === 0,
        }

        setPaymentMethods((prev) => [...prev, newMethod])
        setNewCard({ number: "", expiry: "", cvc: "", name: "" })
        setShowAddCard(false)
    }

    const getCardIcon = (brand: string) => {
        switch (brand) {
            case "visa":
                return "💳"
            case "mastercard":
                return "💳"
            case "amex":
                return "💳"
            default:
                return "💳"
        }
    }

    return (
        <div className="card p-6 space-y-6">
            <div className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                    <CreditCard className="w-5 h-5" style={{ color: "hsl(var(--primary))" }} />
                    <h2 className="text-xl font-semibold" style={{ color: "hsl(var(--text-primary))" }}>
                        Payment Methods
                    </h2>
                </div>
                <button
                    onClick={() => setShowAddCard(true)}
                    className="btn-primary px-4 py-2 rounded-lg font-medium flex items-center space-x-2"
                >
                    <Plus className="w-4 h-4" />
                    <span>Add Method</span>
                </button>
            </div>

            {/* Payment Methods List */}
            <div className="space-y-3">
                {paymentMethods.map((method) => (
                    <div key={method.id} className="card-elevated p-4 rounded-lg">
                        <div className="flex items-center justify-between">
                            <div className="flex items-center space-x-3">
                                <div className="w-12 h-8 bg-gradient-to-r from-blue-600 to-purple-600 rounded flex items-center justify-center text-white text-lg">
                                    {method.type === "card" ? getCardIcon(method.brand || "") : method.type === "paypal" ? "🅿️" : "🍎"}
                                </div>
                                <div>
                                    {method.type === "card" ? (
                                        <>
                                            <p className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                                •••• •••• •••• {method.last4}
                                            </p>
                                            <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                                                Expires {method.expiryMonth?.toString().padStart(2, "0")}/{method.expiryYear}
                                            </p>
                                        </>
                                    ) : method.type === "paypal" ? (
                                        <>
                                            <p className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                                PayPal
                                            </p>
                                            <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                                                {method.email}
                                            </p>
                                        </>
                                    ) : (
                                        <>
                                            <p className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                                Apple Pay
                                            </p>
                                            <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                                                Touch ID or Face ID
                                            </p>
                                        </>
                                    )}
                                </div>
                                {method.isDefault && (
                                    <span
                                        className="px-2 py-1 text-xs font-medium rounded-full"
                                        style={{
                                            backgroundColor: "hsl(var(--primary) / 0.1)",
                                            color: "hsl(var(--primary))",
                                        }}
                                    >
                                        Default
                                    </span>
                                )}
                            </div>
                            <div className="flex items-center space-x-2">
                                {!method.isDefault && (
                                    <button
                                        onClick={() => handleSetDefault(method.id)}
                                        className="text-sm font-medium hover:underline"
                                        style={{ color: "hsl(var(--primary))" }}
                                    >
                                        Set Default
                                    </button>
                                )}
                                <button
                                    onClick={() => handleRemove(method.id)}
                                    className="p-2 rounded-lg hover:bg-[hsl(var(--error)/0.1)] transition-colors"
                                    style={{ color: "hsl(var(--error))" }}
                                >
                                    <Trash2 className="w-4 h-4" />
                                </button>
                            </div>
                        </div>
                    </div>
                ))}
            </div>

            {/* Add Card Modal */}
            {showAddCard && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
                    <div
                        className="w-full max-w-md rounded-xl border shadow-2xl p-6"
                        style={{ backgroundColor: "hsl(var(--paper))", borderColor: "hsl(var(--divider))" }}
                    >
                        <div className="flex items-center justify-between mb-6">
                            <h3 className="text-lg font-semibold" style={{ color: "hsl(var(--text-primary))" }}>
                                Add Payment Method
                            </h3>
                            <button
                                onClick={() => setShowAddCard(false)}
                                className="text-2xl"
                                style={{ color: "hsl(var(--text-secondary))" }}
                            >
                                ×
                            </button>
                        </div>

                        <div className="space-y-4">
                            <div className="space-y-2">
                                <label className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                    Card Number
                                </label>
                                <input
                                    type="text"
                                    placeholder="1234 5678 9012 3456"
                                    value={newCard.number}
                                    onChange={(e) => setNewCard((prev) => ({ ...prev, number: e.target.value }))}
                                    className="input w-full px-4 py-3 rounded-lg focus:outline-none"
                                />
                            </div>

                            <div className="grid grid-cols-2 gap-4">
                                <div className="space-y-2">
                                    <label className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                        Expiry Date
                                    </label>
                                    <input
                                        type="text"
                                        placeholder="MM/YY"
                                        value={newCard.expiry}
                                        onChange={(e) => setNewCard((prev) => ({ ...prev, expiry: e.target.value }))}
                                        className="input w-full px-4 py-3 rounded-lg focus:outline-none"
                                    />
                                </div>
                                <div className="space-y-2">
                                    <label className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                        CVC
                                    </label>
                                    <input
                                        type="text"
                                        placeholder="123"
                                        value={newCard.cvc}
                                        onChange={(e) => setNewCard((prev) => ({ ...prev, cvc: e.target.value }))}
                                        className="input w-full px-4 py-3 rounded-lg focus:outline-none"
                                    />
                                </div>
                            </div>

                            <div className="space-y-2">
                                <label className="text-sm font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                                    Cardholder Name
                                </label>
                                <input
                                    type="text"
                                    placeholder="John Doe"
                                    value={newCard.name}
                                    onChange={(e) => setNewCard((prev) => ({ ...prev, name: e.target.value }))}
                                    className="input w-full px-4 py-3 rounded-lg focus:outline-none"
                                />
                            </div>

                            <div className="flex items-center space-x-2 p-3 rounded-lg bg-[hsl(var(--action-hover))]">
                                <Shield className="w-5 h-5" style={{ color: "hsl(var(--primary))" }} />
                                <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                                    Your payment information is encrypted and secure
                                </p>
                            </div>

                            <div className="flex space-x-3">
                                <button
                                    onClick={() => setShowAddCard(false)}
                                    className="flex-1 py-2 px-4 rounded-lg font-medium transition-colors"
                                    style={{
                                        backgroundColor: "hsl(var(--action-hover))",
                                        color: "hsl(var(--text-primary))",
                                    }}
                                >
                                    Cancel
                                </button>
                                <button
                                    onClick={handleAddCard}
                                    disabled={!newCard.number || !newCard.expiry || !newCard.cvc || !newCard.name}
                                    className="btn-primary flex-1 py-2 px-4 rounded-lg font-medium disabled:opacity-60"
                                >
                                    Add Card
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* Security Notice */}
            <div className="flex items-center space-x-3 p-4 rounded-lg bg-[hsl(var(--action-hover))]">
                <Shield className="w-5 h-5" style={{ color: "hsl(var(--primary))" }} />
                <div>
                    <p className="font-medium" style={{ color: "hsl(var(--text-primary))" }}>
                        Secure Payments
                    </p>
                    <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                        All payment information is encrypted and processed securely
                    </p>
                </div>
            </div>
        </div>
    )
}
