"use client"

import { useState, useEffect } from "react"
import { ChevronLeft, ChevronRight, Star, Clock, Leaf, Snowflake, Sun, LeafIcon as MapleLeaf } from "lucide-react"

type SeasonalProduct = {
    id: string
    name: string
    price: number
    originalPrice?: number
    image: string
    season: "spring" | "summer" | "fall" | "winter"
    freshness: "just-arrived" | "peak-season" | "limited-time"
    rating: number
    description: string
    badge?: string
    inStock: boolean
    harvestDate: string
}

const seasonalProducts: SeasonalProduct[] = [
    {
        id: "1",
        name: "Organic Spring Asparagus",
        price: 4.99,
        originalPrice: 6.99,
        image: "/placeholder.svg?height=200&width=200&text=🥒",
        season: "spring",
        freshness: "just-arrived",
        rating: 4.8,
        description: "Tender, crisp spears harvested this morning",
        badge: "New Arrival",
        inStock: true,
        harvestDate: "2024-01-15",
    },
    {
        id: "2",
        name: "Baby Spinach Leaves",
        price: 3.49,
        image: "/placeholder.svg?height=200&width=200&text=🥬",
        season: "spring",
        freshness: "peak-season",
        rating: 4.9,
        description: "Delicate, nutrient-rich baby leaves",
        inStock: true,
        harvestDate: "2024-01-14",
    },
    {
        id: "3",
        name: "Rainbow Swiss Chard",
        price: 5.99,
        originalPrice: 7.49,
        image: "/placeholder.svg?height=200&width=200&text=🌈",
        season: "spring",
        freshness: "limited-time",
        rating: 4.7,
        description: "Colorful stems with vibrant green leaves",
        badge: "Limited",
        inStock: true,
        harvestDate: "2024-01-13",
    },
    {
        id: "4",
        name: "Fresh Pea Shoots",
        price: 6.99,
        image: "/placeholder.svg?height=200&width=200&text=🌱",
        season: "spring",
        freshness: "just-arrived",
        rating: 4.6,
        description: "Sweet, tender microgreens perfect for salads",
        badge: "Chef's Choice",
        inStock: true,
        harvestDate: "2024-01-15",
    },
    {
        id: "5",
        name: "Heirloom Radishes",
        price: 2.99,
        image: "/placeholder.svg?height=200&width=200&text=🔴",
        season: "spring",
        freshness: "peak-season",
        rating: 4.5,
        description: "Crisp, peppery radishes in assorted colors",
        inStock: true,
        harvestDate: "2024-01-12",
    },
    {
        id: "6",
        name: "Spring Onion Bunches",
        price: 1.99,
        image: "/placeholder.svg?height=200&width=200&text=🧅",
        season: "spring",
        freshness: "just-arrived",
        rating: 4.4,
        description: "Mild, sweet onions with fresh green tops",
        inStock: true,
        harvestDate: "2024-01-14",
    },
]

const getSeasonIcon = (season: string) => {
    switch (season) {
        case "spring":
            return <Leaf className="w-4 h-4" />
        case "summer":
            return <Sun className="w-4 h-4" />
        case "fall":
            return <MapleLeaf className="w-4 h-4" />
        case "winter":
            return <Snowflake className="w-4 h-4" />
        default:
            return <Leaf className="w-4 h-4" />
    }
}

const getFreshnessColor = ( freshness: string) => {
    switch (freshness) {
        case "just-arrived":
            return "hsl(var(--accent))"
        case "peak-season":
            return "hsl(var(--primary))"
        case "limited-time":
            return "hsl(var(--secondary))"
        default:
            return "hsl(var(--primary))"
    }
}

const getFreshnessLabel = (freshness: string) => {
    switch (freshness) {
        case "just-arrived":
            return "Just Arrived"
        case "peak-season":
            return "Peak Season"
        case "limited-time":
            return "Limited Time"
        default:
            return "Fresh"
    }
}

export function SeasonalSection() {
    const [currentIndex, setCurrentIndex] = useState(0)
    const [itemsPerView, setItemsPerView] = useState(4)
    const [isAutoPlaying, setIsAutoPlaying] = useState(true)

    // Responsive items per view
    useEffect(() => {
        const updateItemsPerView = () => {
            if (window.innerWidth < 640) setItemsPerView(1)
            else if (window.innerWidth < 768) setItemsPerView(2)
            else if (window.innerWidth < 1024) setItemsPerView(3)
            else setItemsPerView(4)
        }

        updateItemsPerView()
        window.addEventListener("resize", updateItemsPerView)
        return () => window.removeEventListener("resize", updateItemsPerView)
    }, [])

    // Auto-play functionality
    useEffect(() => {
        if (!isAutoPlaying) return

        const interval = setInterval(() => {
            setCurrentIndex((prev) => {
                const maxIndex = Math.max(0, seasonalProducts.length - itemsPerView)
                return prev >= maxIndex ? 0 : prev + 1
            })
        }, 4000)

        return () => clearInterval(interval)
    }, [itemsPerView, isAutoPlaying])

    const goToPrevious = () => {
        setIsAutoPlaying(false)
        setCurrentIndex((prev) => (prev > 0 ? prev - 1 : Math.max(0, seasonalProducts.length - itemsPerView)))
    }

    const goToNext = () => {
        setIsAutoPlaying(false)
        const maxIndex = Math.max(0, seasonalProducts.length - itemsPerView)
        setCurrentIndex((prev) => (prev < maxIndex ? prev + 1 : 0))
    }

    const addToCart = (product: SeasonalProduct) => {
        // Simulate add to cart
        console.log("Added to cart:", product.name)
        // You would integrate with your cart system here
    }

    const calculateDaysAgo = (harvestDate: string) => {
        const today = new Date()
        const harvest = new Date(harvestDate)
        const diffTime = Math.abs(today.getTime() - harvest.getTime())
        const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
        return diffDays
    }

    return (
        <section className="p-6">
            <div className="p-6">
                <div className="flex items-center justify-between mb-4">
                    <div className="flex items-center space-x-3">
                        <div className="w-10 h-10 bg-primary rounded-xl flex items-center justify-center">
                            <Leaf className="w-5 h-5 text-white animate-leaf-sway" />
                        </div>
                        <div>
                            <h2 className="text-2xl font-bold" style={{ color: "hsl(var(--text-primary))" }}>
                                Seasonal & Freshly Arrived
                            </h2>
                            <p style={{ color: "hsl(var(--text-secondary))" }}>Hand-picked this week from local farms</p>
                        </div>
                    </div>

                    {/* Navigation Controls */}
                    <div className="flex items-center space-x-2">
                        <button
                            onClick={goToPrevious}
                            className="w-10 h-10 rounded-full border-2 flex items-center justify-center transition-all duration-200 hover:scale-110"
                            style={{
                                borderColor: "hsl(var(--divider))",
                                backgroundColor: "hsl(var(--paper))",
                                color: "hsl(var(--text-secondary))",
                            }}
                            onMouseEnter={(e) => {
                                e.currentTarget.style.borderColor = "hsl(var(--primary))"
                                e.currentTarget.style.color = "hsl(var(--primary))"
                            }}
                            onMouseLeave={(e) => {
                                e.currentTarget.style.borderColor = "hsl(var(--divider))"
                                e.currentTarget.style.color = "hsl(var(--text-secondary))"
                            }}
                        >
                            <ChevronLeft className="w-5 h-5" />
                        </button>
                        <button
                            onClick={goToNext}
                            className="w-10 h-10 rounded-full border-2 flex items-center justify-center transition-all duration-200 hover:scale-110"
                            style={{
                                borderColor: "hsl(var(--divider))",
                                backgroundColor: "hsl(var(--paper))",
                                color: "hsl(var(--text-secondary))",
                            }}
                            onMouseEnter={(e) => {
                                e.currentTarget.style.borderColor = "hsl(var(--primary))"
                                e.currentTarget.style.color = "hsl(var(--primary))"
                            }}
                            onMouseLeave={(e) => {
                                e.currentTarget.style.borderColor = "hsl(var(--divider))"
                                e.currentTarget.style.color = "hsl(var(--text-secondary))"
                            }}
                        >
                            <ChevronRight className="w-5 h-5" />
                        </button>
                    </div>
                </div>

                {/* Season Indicator */}
                <div className="flex items-center space-x-2 mb-6">
                    <div
                        className="flex items-center space-x-2 px-3 py-1 rounded-full text-sm font-medium"
                        style={{
                            backgroundColor: "hsl(var(--primary) / 0.1)",
                            color: "hsl(var(--primary))",
                        }}
                    >
                        {getSeasonIcon("spring")}
                        <span>Spring Harvest 2024</span>
                    </div>
                    <div
                        className="flex items-center space-x-2 px-3 py-1 rounded-full text-sm"
                        style={{
                            backgroundColor: "hsl(var(--accent) / 0.1)",
                            color: "hsl(var(--accent))",
                        }}
                    >
                        <Clock className="w-4 h-4" />
                        <span>Updated daily</span>
                    </div>
                </div>
            </div>

            {/* Products Carousel */}
            <div className="relative overflow-hidden">
                <div
                    className="flex transition-transform duration-500 ease-out"
                    style={{
                        transform: `translateX(-${currentIndex * (100 / itemsPerView)}%)`,
                    }}
                    onMouseEnter={() => setIsAutoPlaying(false)}
                    onMouseLeave={() => setIsAutoPlaying(true)}
                >
                    {seasonalProducts.map((product) => (
                        <div key={product.id} className="flex-shrink-0 px-3" style={{ width: `${100 / itemsPerView}%` }}>
                            <div className="card p-4 hover:shadow-green transition-all duration-300 hover:scale-[1.02] group">
                                {/* Product Image */}
                                <div className="relative mb-4 overflow-hidden rounded-lg">
                                    <img
                                        src={product.image || "/placeholder.svg"}
                                        alt={product.name}
                                        className="w-full h-48 object-cover transition-transform duration-300 group-hover:scale-110"
                                    />

                                    {/* Badges */}
                                    <div className="absolute top-2 left-2 flex flex-col space-y-1">
                                        {product.badge && (
                                            <span
                                                className="px-2 py-1 text-xs font-semibold rounded-full"
                                                style={{
                                                    backgroundColor: "hsl(var(--secondary))",
                                                    color: "white",
                                                }}
                                            >
                                                {product.badge}
                                            </span>
                                        )}
                                        <span
                                            className="px-2 py-1 text-xs font-medium rounded-full"
                                            style={{
                                                backgroundColor: getFreshnessColor(product.freshness),
                                                color: "white",
                                            }}
                                        >
                                            {getFreshnessLabel(product.freshness)}
                                        </span>
                                    </div>

                                    {/* Discount Badge */}
                                    {product.originalPrice && (
                                        <div
                                            className="absolute top-2 right-2 px-2 py-1 text-xs font-bold rounded-full"
                                            style={{
                                                backgroundColor: "hsl(var(--error))",
                                                color: "white",
                                            }}
                                        >
                                            {Math.round(((product.originalPrice - product.price) / product.originalPrice) * 100)}% OFF
                                        </div>
                                    )}

                                    {/* Harvest Date */}
                                    <div
                                        className="absolute bottom-2 left-2 px-2 py-1 text-xs rounded-full backdrop-blur-sm"
                                        style={{
                                            backgroundColor: "rgba(0, 0, 0, 0.7)",
                                            color: "white",
                                        }}
                                    >
                                        Harvested {calculateDaysAgo(product.harvestDate)} days ago
                                    </div>
                                </div>

                                {/* Product Info */}
                                <div className="space-y-3">
                                    <div>
                                        <h3 className="font-semibold text-lg mb-1" style={{ color: "hsl(var(--text-primary))" }}>
                                            {product.name}
                                        </h3>
                                        <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                                            {product.description}
                                        </p>
                                    </div>

                                    {/* Rating */}
                                    <div className="flex items-center space-x-2">
                                        <div className="flex items-center">
                                            {[...Array(5)].map((_, i) => (
                                                <Star
                                                    key={i}
                                                    className={`w-4 h-4 ${i < Math.floor(product.rating) ? "text-yellow-400 fill-current" : "text-gray-300"
                                                        }`}
                                                />
                                            ))}
                                        </div>
                                        <span className="text-sm font-medium" style={{ color: "hsl(var(--text-secondary))" }}>
                                            {product.rating}
                                        </span>
                                    </div>

                                    {/* Price */}
                                    <div className="flex items-center space-x-2">
                                        <span className="text-xl font-bold" style={{ color: "hsl(var(--primary))" }}>
                                            ${product.price.toFixed(2)}
                                        </span>
                                        {product.originalPrice && (
                                            <span className="text-sm line-through" style={{ color: "hsl(var(--text-disabled))" }}>
                                                ${product.originalPrice.toFixed(2)}
                                            </span>
                                        )}
                                    </div>

                                    {/* Stock Status */}
                                    <div className="flex items-center space-x-2">
                                        <div
                                            className={`w-2 h-2 rounded-full ${product.inStock ? "bg-[hsl(var(--accent))]" : "bg-[hsl(var(--error))]"
                                                }`}
                                        />
                                        <span
                                            className="text-sm font-medium"
                                            style={{
                                                color: product.inStock ? "hsl(var(--accent))" : "hsl(var(--error))",
                                            }}
                                        >
                                            {product.inStock ? "In Stock" : "Out of Stock"}
                                        </span>
                                    </div>

                                    {/* Add to Cart Button */}
                                    <button
                                        onClick={() => addToCart(product)}
                                        disabled={!product.inStock}
                                        className="btn-primary w-full py-2 rounded-lg font-medium transition-all duration-200 hover:scale-[1.02] active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
                                    >
                                        {product.inStock ? "Add to Cart" : "Out of Stock"}
                                    </button>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            {/* Indicators */}
            <div className="flex justify-center space-x-2 mt-6">
                {Array.from({ length: Math.max(1, seasonalProducts.length - itemsPerView + 1) }).map((_, index) => (
                    <button
                        key={index}
                        onClick={() => {
                            setCurrentIndex(index)
                            setIsAutoPlaying(false)
                        }}
                        className={`w-2 h-2 rounded-full transition-all duration-200 ${index === currentIndex ? "w-6 bg-[hsl(var(--primary))]" : "bg-[hsl(var(--divider))]"
                            }`}
                    />
                ))}
            </div>

            {/* View All Link */}
            <div className="text-center mt-8">
                <button className="btn-outline px-6 py-3 rounded-lg font-medium transition-all duration-200 hover:scale-105">
                    View All Seasonal Products
                </button>
            </div>
        </section>
    )
}
