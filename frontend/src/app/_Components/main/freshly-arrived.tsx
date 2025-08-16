"use client"

import { useState, useEffect } from "react"
import { Clock, Truck, Leaf, Star, Timer } from "lucide-react"

type FreshProduct = {
    id: string
    name: string
    price: number
    image: string
    arrivalTime: string
    farmName: string
    distance: string
    rating: number
    category: string
    description: string
    isNew: boolean
    stockLevel: "high" | "medium" | "low"
    estimatedSellOut: string
}

const freshProducts: FreshProduct[] = [
    {
        id: "1",
        name: "Farm Fresh Tomatoes",
        price: 3.99,
        image: "/placeholder.svg?height=150&width=150&text=🍅",
        arrivalTime: "2 hours ago",
        farmName: "Sunny Acres Farm",
        distance: "12 miles away",
        rating: 4.9,
        category: "Vegetables",
        description: "Vine-ripened heirloom tomatoes",
        isNew: true,
        stockLevel: "high",
        estimatedSellOut: "Tomorrow",
    },
    {
        id: "2",
        name: "Organic Kale Bunches",
        price: 2.49,
        image: "/placeholder.svg?height=150&width=150&text=🥬",
        arrivalTime: "4 hours ago",
        farmName: "Green Valley Organics",
        distance: "8 miles away",
        rating: 4.8,
        category: "Leafy Greens",
        description: "Curly kale perfect for smoothies",
        isNew: false,
        stockLevel: "medium",
        estimatedSellOut: "Tonight",
    },
    {
        id: "3",
        name: "Sweet Bell Peppers",
        price: 4.49,
        image: "/placeholder.svg?height=150&width=150&text=🫑",
        arrivalTime: "1 hour ago",
        farmName: "Rainbow Gardens",
        distance: "15 miles away",
        rating: 4.7,
        category: "Vegetables",
        description: "Colorful mix of red, yellow, and orange",
        isNew: true,
        stockLevel: "low",
        estimatedSellOut: "In 3 hours",
    },
    {
        id: "4",
        name: "Baby Carrots",
        price: 1.99,
        image: "/placeholder.svg?height=150&width=150&text=🥕",
        arrivalTime: "30 minutes ago",
        farmName: "Harvest Moon Farm",
        distance: "6 miles away",
        rating: 4.6,
        category: "Root Vegetables",
        description: "Sweet, tender baby carrots",
        isNew: true,
        stockLevel: "high",
        estimatedSellOut: "Tomorrow evening",
    },
    {
        id: "5",
        name: "Fresh Herbs Mix",
        price: 5.99,
        image: "/placeholder.svg?height=150&width=150&text=🌿",
        arrivalTime: "6 hours ago",
        farmName: "Herb Haven",
        distance: "10 miles away",
        rating: 4.9,
        category: "Herbs",
        description: "Basil, parsley, cilantro, and mint",
        isNew: false,
        stockLevel: "medium",
        estimatedSellOut: "Tomorrow morning",
    },
    {
        id: "6",
        name: "Crisp Lettuce Heads",
        price: 2.99,
        image: "/placeholder.svg?height=150&width=150&text=🥬",
        arrivalTime: "3 hours ago",
        farmName: "Fresh Fields Co-op",
        distance: "9 miles away",
        rating: 4.5,
        category: "Leafy Greens",
        description: "Iceberg and romaine lettuce",
        isNew: false,
        stockLevel: "high",
        estimatedSellOut: "Day after tomorrow",
    },
]

const getStockColor = (level: string) => {
    switch (level) {
        case "high":
            return "hsl(var(--accent))"
        case "medium":
            return "hsl(var(--secondary))"
        case "low":
            return "hsl(var(--error))"
        default:
            return "hsl(var(--accent))"
    }
}

const getStockLabel = (level: string) => {
    switch (level) {
        case "high":
            return "In Stock"
        case "medium":
            return "Limited Stock"
        case "low":
            return "Almost Gone"
        default:
            return "In Stock"
    }
}

export function FreshlyArrived() {
    const [timeUpdates, setTimeUpdates] = useState<{ [key: string]: string }>({})

    // Update arrival times every minute
    useEffect(() => {
        const updateTimes = () => {
            const updates: { [key: string]: string } = {}
            freshProducts.forEach((product) => {
                // Simulate time progression
                const baseTime = product.arrivalTime
                updates[product.id] = baseTime
            })
            setTimeUpdates(updates)
        }

        updateTimes()
        const interval = setInterval(updateTimes, 60000) // Update every minute

        return () => clearInterval(interval)
    }, [])

    const addToCart = (product: FreshProduct) => {
        console.log("Added to cart:", product.name)
        // Integration with cart system
    }

    return (
        <section className="p-6">
            <div className="p-6">
                <div className="flex items-center space-x-3 mb-4">
                    <div className="w-10 h-10 bg-accent rounded-xl flex items-center justify-center">
                        <Clock className="w-5 h-5 text-white" />
                    </div>
                    <div>
                        <h2 className="text-2xl font-bold" style={{ color: "hsl(var(--text-primary))" }}>
                            Freshly Arrived Today
                        </h2>
                        <p style={{ color: "hsl(var(--text-secondary))" }}>
                            Just delivered from local farms • Updated in real-time
                        </p>
                    </div>
                </div>

                {/* Live Update Indicator */}
                <div className="flex items-center space-x-4 mb-6">
                    <div
                        className="flex items-center space-x-2 px-3 py-1 rounded-full text-sm font-medium"
                        style={{
                            backgroundColor: "hsl(var(--accent) / 0.1)",
                            color: "hsl(var(--accent))",
                        }}
                    >
                        <div className="w-2 h-2 bg-[hsl(var(--accent))] rounded-full animate-pulse" />
                        <span>Live Updates</span>
                    </div>
                    <div
                        className="flex items-center space-x-2 px-3 py-1 rounded-full text-sm"
                        style={{
                            backgroundColor: "hsl(var(--primary) / 0.1)",
                            color: "hsl(var(--primary))",
                        }}
                    >
                        <Truck className="w-4 h-4" />
                        <span>6 deliveries today</span>
                    </div>
                </div>
            </div>

            {/* Products Grid */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
                {freshProducts.map((product) => (
                    <div
                        key={product.id}
                        className="card p-4 hover:shadow-green transition-all duration-300 hover:scale-[1.02] group relative"
                    >
                        {/* New Badge */}
                        {product.isNew && (
                            <div
                                className="absolute -top-2 -right-2 w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold z-10"
                                style={{
                                    backgroundColor: "hsl(var(--secondary))",
                                    color: "white",
                                }}
                            >
                                NEW
                            </div>
                        )}

                        {/* Product Image */}
                        <div className="relative mb-3 overflow-hidden rounded-lg">
                            <img
                                src={product.image || "/placeholder.svg"}
                                alt={product.name}
                                className="w-full h-32 object-cover transition-transform duration-300 group-hover:scale-110"
                            />

                            {/* Stock Level Indicator */}
                            <div
                                className="absolute top-2 left-2 px-2 py-1 text-xs font-medium rounded-full"
                                style={{
                                    backgroundColor: getStockColor(product.stockLevel),
                                    color: "white",
                                }}
                            >
                                {getStockLabel(product.stockLevel)}
                            </div>
                        </div>

                        {/* Product Info */}
                        <div className="space-y-2">
                            <div>
                                <h3 className="font-semibold text-sm mb-1" style={{ color: "hsl(var(--text-primary))" }}>
                                    {product.name}
                                </h3>
                                <p className="text-xs" style={{ color: "hsl(var(--text-secondary))" }}>
                                    {product.description}
                                </p>
                            </div>

                            {/* Farm Info */}
                            <div className="flex items-center space-x-2 text-xs" style={{ color: "hsl(var(--text-secondary))" }}>
                                <Leaf className="w-3 h-3" />
                                <span>{product.farmName}</span>
                                <span>•</span>
                                <span>{product.distance}</span>
                            </div>

                            {/* Arrival Time */}
                            <div className="flex items-center space-x-1 text-xs font-medium" style={{ color: "hsl(var(--accent))" }}>
                                <Clock className="w-3 h-3" />
                                <span>Arrived {timeUpdates[product.id] || product.arrivalTime}</span>
                            </div>

                            {/* Rating */}
                            <div className="flex items-center space-x-1">
                                <div className="flex items-center">
                                    {[...Array(5)].map((_, i) => (
                                        <Star
                                            key={i}
                                            className={`w-3 h-3 ${i < Math.floor(product.rating) ? "text-yellow-400 fill-current" : "text-gray-300"
                                                }`}
                                        />
                                    ))}
                                </div>
                                <span className="text-xs font-medium" style={{ color: "hsl(var(--text-secondary))" }}>
                                    {product.rating}
                                </span>
                            </div>

                            {/* Price */}
                            <div className="flex items-center justify-between">
                                <span className="text-lg font-bold" style={{ color: "hsl(var(--primary))" }}>
                                    ${product.price.toFixed(2)}
                                </span>
                                {product.stockLevel === "low" && (
                                    <div className="flex items-center space-x-1 text-xs" style={{ color: "hsl(var(--error))" }}>
                                        <Timer className="w-3 h-3" />
                                        <span>Sells out {product.estimatedSellOut}</span>
                                    </div>
                                )}
                            </div>

                            {/* Add to Cart Button */}
                            <button
                                onClick={() => addToCart(product)}
                                className="btn-primary w-full py-2 rounded-lg font-medium text-sm transition-all duration-200 hover:scale-[1.02] active:scale-[0.98]"
                            >
                                Add to Cart
                            </button>
                        </div>
                    </div>
                ))}
            </div>

            {/* Bottom Stats */}
            <div className="mt-8 grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div className="card p-4 text-center">
                    <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center mx-auto mb-2">
                        <Truck className="w-4 h-4 text-white" />
                    </div>
                    <p className="text-2xl font-bold" style={{ color: "hsl(var(--primary))" }}>
                        6
                    </p>
                    <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                        Deliveries Today
                    </p>
                </div>

                <div className="card p-4 text-center">
                    <div className="w-8 h-8 bg-accent rounded-lg flex items-center justify-center mx-auto mb-2">
                        <Leaf className="w-4 h-4 text-white" />
                    </div>
                    <p className="text-2xl font-bold" style={{ color: "hsl(var(--accent))" }}>
                        12
                    </p>
                    <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                        Local Farms
                    </p>
                </div>

                <div className="card p-4 text-center">
                    <div className="w-8 h-8 bg-secondary rounded-lg flex items-center justify-center mx-auto mb-2">
                        <Clock className="w-4 h-4 text-white" />
                    </div>
                    <p className="text-2xl font-bold" style={{ color: "hsl(var(--secondary))" }}>
                        2.5
                    </p>
                    <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                        Avg Hours Fresh
                    </p>
                </div>
            </div>

            {/* View All Link */}
            <div className="text-center mt-8">
                <button className="btn-outline px-6 py-3 rounded-lg font-medium transition-all duration-200 hover:scale-105">
                    View All Fresh Arrivals
                </button>
            </div>
        </section>
    )
}
