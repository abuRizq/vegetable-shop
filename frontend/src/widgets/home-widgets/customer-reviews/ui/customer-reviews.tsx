"use client"

import { useState, useEffect } from "react"
import { Star, Quote, ThumbsUp, MessageCircle, ChevronLeft, ChevronRight, Verified, Heart } from "lucide-react"

type CustomerReview = {
    id: string
    customerName: string
    customerAvatar: string
    location: string
    rating: number
    title: string
    review: string
    date: string
    verified: boolean
    helpful: number
    productsPurchased: string[]
    images?: string[]
    response?: {
        from: string
        message: string
        date: string
    }
    tags: string[]
    featured: boolean
}

const customerReviews: CustomerReview[] = [
    {
        id: "1",
        customerName: "Sarah Johnson",
        customerAvatar: "/placeholder.svg?height=60&width=60&text=SJ",
        location: "Brooklyn, NY",
        rating: 5,
        title: "Absolutely Fresh and Delicious!",
        review:
            "I've been ordering from FreshVeggies for 6 months now and I'm consistently amazed by the quality. The vegetables arrive within hours of being harvested, and you can really taste the difference. My family has never eaten healthier!",
        date: "2024-01-10",
        verified: true,
        helpful: 24,
        productsPurchased: ["Organic Spinach", "Cherry Tomatoes", "Bell Peppers"],
        images: ["/placeholder.svg?height=100&width=100&text=🥬", "/placeholder.svg?height=100&width=100&text=🍅"],
        response: {
            from: "FreshVeggies Team",
            message: "Thank you so much, Sarah! We're thrilled to be part of your family's healthy eating journey. 🌱",
            date: "2024-01-11",
        },
        tags: ["Quality", "Freshness", "Family"],
        featured: true,
    },
    {
        id: "2",
        customerName: "Michael Chen",
        customerAvatar: "/placeholder.svg?height=60&width=60&text=MC",
        location: "Manhattan, NY",
        rating: 5,
        title: "Game Changer for My Restaurant",
        review:
            "As a chef, I'm extremely picky about ingredients. FreshVeggies has become my go-to supplier. The consistency, quality, and variety are unmatched. My customers constantly compliment the freshness of our salads and vegetable dishes.",
        date: "2024-01-08",
        verified: true,
        helpful: 31,
        productsPurchased: ["Heirloom Tomatoes", "Baby Spinach", "Rainbow Chard"],
        tags: ["Professional", "Quality", "Consistency"],
        featured: true,
    },
    {
        id: "3",
        customerName: "Emily Rodriguez",
        customerAvatar: "/placeholder.svg?height=60&width=60&text=ER",
        location: "Queens, NY",
        rating: 4,
        title: "Great Service, Amazing Freshness",
        review:
            "The delivery is always on time and the vegetables are incredibly fresh. I love that I can see which farm each item comes from. The only reason I'm not giving 5 stars is that I wish there were more organic options, but overall fantastic service!",
        date: "2024-01-05",
        verified: true,
        helpful: 18,
        productsPurchased: ["Carrots", "Lettuce", "Cucumbers"],
        tags: ["Delivery", "Freshness", "Transparency"],
        featured: false,
    },
    {
        id: "4",
        customerName: "David Thompson",
        customerAvatar: "/placeholder.svg?height=60&width=60&text=DT",
        location: "Bronx, NY",
        rating: 5,
        title: "My Kids Actually Eat Vegetables Now!",
        review:
            "I was skeptical at first, but the difference in taste is remarkable. My picky 8-year-old now asks for seconds on broccoli! The vegetables are so fresh and flavorful that even kids can tell the difference. Worth every penny.",
        date: "2024-01-03",
        verified: true,
        helpful: 42,
        productsPurchased: ["Broccoli", "Sweet Corn", "Baby Carrots"],
        images: ["/placeholder.svg?height=100&width=100&text=🥦"],
        tags: ["Kids", "Taste", "Family"],
        featured: true,
    },
    {
        id: "5",
        customerName: "Lisa Park",
        customerAvatar: "/placeholder.svg?height=60&width=60&text=LP",
        location: "Staten Island, NY",
        rating: 5,
        title: "Supporting Local Farms Never Felt So Good",
        review:
            "I love knowing exactly where my food comes from. The farm profiles and harvest dates make me feel connected to the source. Plus, the vegetables stay fresh in my fridge for much longer than store-bought ones.",
        date: "2024-01-01",
        verified: true,
        helpful: 15,
        productsPurchased: ["Kale", "Swiss Chard", "Radishes"],
        tags: ["Local", "Sustainability", "Freshness"],
        featured: false,
    },
    {
        id: "6",
        customerName: "James Wilson",
        customerAvatar: "/placeholder.svg?height=60&width=60&text=JW",
        location: "Long Island, NY",
        rating: 4,
        title: "Excellent Quality, Fair Prices",
        review:
            "The quality is consistently excellent and the prices are very reasonable for organic produce. The packaging is also eco-friendly which I appreciate. Delivery is always punctual and the vegetables arrive in perfect condition.",
        date: "2023-12-28",
        verified: true,
        helpful: 22,
        productsPurchased: ["Organic Tomatoes", "Peppers", "Onions"],
        tags: ["Value", "Eco-friendly", "Organic"],
        featured: false,
    },
]

const testimonialStats = {
    totalReviews: 1247,
    averageRating: 4.8,
    fiveStars: 89,
    fourStars: 8,
    threeStars: 2,
    twoStars: 1,
    oneStars: 0,
    verifiedPurchases: 96,
}

export function CustomerReviews() {
    const [currentIndex, setCurrentIndex] = useState(0)
    const [filter, setFilter] = useState<"all" | "featured" | "verified">("featured")
    const [isAutoPlaying, setIsAutoPlaying] = useState(true)

    const filteredReviews = customerReviews.filter((review) => {
        if (filter === "featured") return review.featured
        if (filter === "verified") return review.verified
        return true
    })

    // Auto-play functionality
    useEffect(() => {
        if (!isAutoPlaying || filteredReviews.length <= 1) return

        const interval = setInterval(() => {
            setCurrentIndex((prev) => (prev + 1) % filteredReviews.length)
        }, 6000)

        return () => clearInterval(interval)
    }, [filteredReviews.length, isAutoPlaying])

    const goToPrevious = () => {
        setIsAutoPlaying(false)
        setCurrentIndex((prev) => (prev - 1 + filteredReviews.length) % filteredReviews.length)
    }

    const goToNext = () => {
        setIsAutoPlaying(false)
        setCurrentIndex((prev) => (prev + 1) % filteredReviews.length)
    }

    const markHelpful = (reviewId: string) => {
        console.log("Marked helpful:", reviewId)
        // Integration with backend to track helpful votes
    }

    const formatDate = (dateString: string) => {
        return new Date(dateString).toLocaleDateString("en-US", {
            year: "numeric",
            month: "long",
            day: "numeric",
        })
    }

    const currentReview = filteredReviews[currentIndex]

    return (
        <section className="p-6">
            <div className="p-6">
                <div className="flex items-center justify-between mb-6">
                    <div className="flex items-center space-x-3">
                        <div className="w-10 h-10 bg-secondary rounded-xl flex items-center justify-center">
                            <MessageCircle className="w-5 h-5 text-white" />
                        </div>
                        <div>
                            <h2 className="text-2xl font-bold" style={{ color: "hsl(var(--text-primary))" }}>
                                Customer Reviews & Testimonials
                            </h2>
                            <p style={{ color: "hsl(var(--text-secondary))" }}>
                                What our customers are saying about their fresh vegetable experience
                            </p>
                        </div>
                    </div>

                    {/* Filter Buttons */}
                    <div className="flex items-center space-x-2">
                        {[
                            { key: "featured", label: "Featured" },
                            { key: "verified", label: "Verified" },
                            { key: "all", label: "All Reviews" },
                        ].map((filterOption) => (
                            <button
                                key={filterOption.key}
                                onClick={() => {
                                    // eslint-disable-next-line @typescript-eslint/no-explicit-any
                                    setFilter(filterOption.key as any)
                                    setCurrentIndex(0)
                                    setIsAutoPlaying(true)
                                }}
                                className={`px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 ${filter === filterOption.key
                                    ? "bg-[hsl(var(--primary))] text-white shadow-md"
                                    : "bg-[hsl(var(--action-hover))] hover:bg-[hsl(var(--action-selected))]"
                                    }`}
                                style={{
                                    color: filter === filterOption.key ? "white" : "hsl(var(--text-primary))",
                                }}
                            >
                                {filterOption.label}
                            </button>
                        ))}
                    </div>
                </div>

                {/* Stats Overview */}
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
                    <div className="card p-4 text-center">
                        <div className="flex items-center justify-center space-x-1 mb-2">
                            <Star className="w-5 h-5 text-yellow-400 fill-current" />
                            <span className="text-2xl font-bold" style={{ color: "hsl(var(--text-primary))" }}>
                                {testimonialStats.averageRating}
                            </span>
                        </div>
                        <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                            Average Rating
                        </p>
                    </div>

                    <div className="card p-4 text-center">
                        <p className="text-2xl font-bold" style={{ color: "hsl(var(--primary))" }}>
                            {testimonialStats.totalReviews.toLocaleString()}
                        </p>
                        <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                            Total Reviews
                        </p>
                    </div>

                    <div className="card p-4 text-center">
                        <p className="text-2xl font-bold" style={{ color: "hsl(var(--accent))" }}>
                            {testimonialStats.verifiedPurchases}%
                        </p>
                        <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                            Verified Purchases
                        </p>
                    </div>

                    <div className="card p-4 text-center">
                        <p className="text-2xl font-bold" style={{ color: "hsl(var(--secondary))" }}>
                            {testimonialStats.fiveStars}%
                        </p>
                        <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                            5-Star Reviews
                        </p>
                    </div>
                </div>
            </div>

            {/* Main Review Display */}
            {currentReview && (
                <div className="relative">
                    <div
                        className="card p-8 mb-6 relative overflow-hidden"
                        onMouseEnter={() => setIsAutoPlaying(false)}
                        onMouseLeave={() => setIsAutoPlaying(true)}
                    >
                        {/* Background Quote Icon */}
                        <div className="absolute top-4 right-4 opacity-10">
                            <Quote className="w-24 h-24" style={{ color: "hsl(var(--primary))" }} />
                        </div>

                        {/* Featured Badge */}
                        {currentReview.featured && (
                            <div
                                className="absolute top-4 left-4 px-3 py-1 rounded-full text-sm font-medium"
                                style={{
                                    backgroundColor: "hsl(var(--secondary))",
                                    color: "white",
                                }}
                            >
                                Featured Review
                            </div>
                        )}

                        <div className="relative z-10">
                            {/* Customer Info */}
                            <div className="flex items-start space-x-4 mb-6">
                                <img
                                    src={currentReview.customerAvatar || "/placeholder.svg"}
                                    alt={currentReview.customerName}
                                    className="w-16 h-16 rounded-full object-cover border-2"
                                    style={{ borderColor: "hsl(var(--primary))" }}
                                />
                                <div className="flex-1">
                                    <div className="flex items-center space-x-2 mb-1">
                                        <h3 className="text-lg font-semibold" style={{ color: "hsl(var(--text-primary))" }}>
                                            {currentReview.customerName}
                                        </h3>
                                        {currentReview.verified && (
                                            <div className="flex items-center space-x-1">
                                                <Verified className="w-4 h-4" style={{ color: "hsl(var(--accent))" }} />
                                                <span className="text-xs font-medium" style={{ color: "hsl(var(--accent))" }}>
                                                    Verified Purchase
                                                </span>
                                            </div>
                                        )}
                                    </div>
                                    <p className="text-sm mb-2" style={{ color: "hsl(var(--text-secondary))" }}>
                                        {currentReview.location} • {formatDate(currentReview.date)}
                                    </p>

                                    {/* Rating */}
                                    <div className="flex items-center space-x-2">
                                        <div className="flex items-center">
                                            {[...Array(5)].map((_, i) => (
                                                <Star
                                                    key={i}
                                                    className={`w-5 h-5 ${i < currentReview.rating ? "text-yellow-400 fill-current" : "text-gray-300"
                                                        }`}
                                                />
                                            ))}
                                        </div>
                                        <span className="text-sm font-medium" style={{ color: "hsl(var(--text-secondary))" }}>
                                            {currentReview.rating}/5
                                        </span>
                                    </div>
                                </div>
                            </div>

                            {/* Review Content */}
                            <div className="mb-6">
                                <h4 className="text-xl font-semibold mb-3" style={{ color: "hsl(var(--text-primary))" }}>
                                    {currentReview.title}
                                </h4>
                                <p className="text-lg leading-relaxed" style={{ color: "hsl(var(--text-secondary))" }}>
                                    {/* "{currentReview.review}" */}
                                </p>
                            </div>

                            {/* Products Purchased */}
                            <div className="mb-6">
                                <p className="text-sm font-medium mb-2" style={{ color: "hsl(var(--text-primary))" }}>
                                    Products Purchased:
                                </p>
                                <div className="flex flex-wrap gap-2">
                                    {currentReview.productsPurchased.map((product, index) => (
                                        <span
                                            key={index}
                                            className="px-3 py-1 rounded-full text-sm"
                                            style={{
                                                backgroundColor: "hsl(var(--primary) / 0.1)",
                                                color: "hsl(var(--primary))",
                                            }}
                                        >
                                            {product}
                                        </span>
                                    ))}
                                </div>
                            </div>

                            {/* Review Images */}
                            {currentReview.images && currentReview.images.length > 0 && (
                                <div className="mb-6">
                                    <p className="text-sm font-medium mb-3" style={{ color: "hsl(var(--text-primary))" }}>
                                        Customer Photos:
                                    </p>
                                    <div className="flex space-x-3">
                                        {currentReview.images.map((image, index) => (
                                            <img
                                                key={index}
                                                src={image || "/placeholder.svg"}
                                                alt={`Customer photo ${index + 1}`}
                                                className="w-20 h-20 rounded-lg object-cover border"
                                                style={{ borderColor: "hsl(var(--divider))" }}
                                            />
                                        ))}
                                    </div>
                                </div>
                            )}

                            {/* Tags */}
                            <div className="mb-6">
                                <div className="flex flex-wrap gap-2">
                                    {currentReview.tags.map((tag, index) => (
                                        <span
                                            key={index}
                                            className="px-2 py-1 rounded text-xs font-medium"
                                            style={{
                                                backgroundColor: "hsl(var(--accent) / 0.1)",
                                                color: "hsl(var(--accent))",
                                            }}
                                        >
                                            #{tag}
                                        </span>
                                    ))}
                                </div>
                            </div>

                            {/* Company Response */}
                            {currentReview.response && (
                                <div
                                    className="p-4 rounded-lg border-l-4 mb-6"
                                    style={{
                                        backgroundColor: "hsl(var(--action-hover))",
                                        borderColor: "hsl(var(--primary))",
                                    }}
                                >
                                    <div className="flex items-center space-x-2 mb-2">
                                        <div className="w-6 h-6 bg-primary rounded-full flex items-center justify-center">
                                            <span className="text-white text-xs font-bold">FV</span>
                                        </div>
                                        <span className="text-sm font-medium" style={{ color: "hsl(var(--primary))" }}>
                                            {currentReview.response.from}
                                        </span>
                                        <span className="text-xs" style={{ color: "hsl(var(--text-disabled))" }}>
                                            {formatDate(currentReview.response.date)}
                                        </span>
                                    </div>
                                    <p className="text-sm" style={{ color: "hsl(var(--text-secondary))" }}>
                                        {currentReview.response.message}
                                    </p>
                                </div>
                            )}

                            {/* Actions */}
                            <div className="flex items-center justify-between">
                                <button
                                    onClick={() => markHelpful(currentReview.id)}
                                    className="flex items-center space-x-2 px-4 py-2 rounded-lg transition-all duration-200 hover:scale-105"
                                    style={{
                                        backgroundColor: "hsl(var(--action-hover))",
                                        color: "hsl(var(--text-primary))",
                                    }}
                                >
                                    <ThumbsUp className="w-4 h-4" />
                                    <span className="text-sm">Helpful ({currentReview.helpful})</span>
                                </button>

                                <div className="flex items-center space-x-2">
                                    <button
                                        onClick={goToPrevious}
                                        className="w-10 h-10 rounded-full border-2 flex items-center justify-center transition-all duration-200 hover:scale-110"
                                        style={{
                                            borderColor: "hsl(var(--divider))",
                                            backgroundColor: "hsl(var(--paper))",
                                            color: "hsl(var(--text-secondary))",
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
                                    >
                                        <ChevronRight className="w-5 h-5" />
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* Review Indicators */}
                    <div className="flex justify-center space-x-2 mb-8">
                        {filteredReviews.map((_, index) => (
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
                </div>
            )}

            {/* Rating Breakdown */}
            <div className="card p-6 mb-8">
                <h3 className="text-lg font-semibold mb-4" style={{ color: "hsl(var(--text-primary))" }}>
                    Rating Breakdown
                </h3>
                <div className="space-y-3">
                    {[
                        { stars: 5, percentage: testimonialStats.fiveStars },
                        { stars: 4, percentage: testimonialStats.fourStars },
                        { stars: 3, percentage: testimonialStats.threeStars },
                        { stars: 2, percentage: testimonialStats.twoStars },
                        { stars: 1, percentage: testimonialStats.oneStars },
                    ].map((rating) => (
                        <div key={rating.stars} className="flex items-center space-x-3">
                            <div className="flex items-center space-x-1 w-16">
                                <span className="text-sm" style={{ color: "hsl(var(--text-primary))" }}>
                                    {rating.stars}
                                </span>
                                <Star className="w-4 h-4 text-yellow-400 fill-current" />
                            </div>
                            <div className="flex-1 h-2 bg-[hsl(var(--divider))] rounded-full overflow-hidden">
                                <div
                                    className="h-full bg-[hsl(var(--primary))] transition-all duration-500"
                                    style={{ width: `${rating.percentage}%` }}
                                />
                            </div>
                            <span className="text-sm w-12 text-right" style={{ color: "hsl(var(--text-secondary))" }}>
                                {rating.percentage}%
                            </span>
                        </div>
                    ))}
                </div>
            </div>

            {/* Call to Action */}
            <div className="text-center">
                <div className="card p-8 mb-6">
                    <Heart className="w-12 h-12 mx-auto mb-4" style={{ color: "hsl(var(--secondary))" }} />
                    <h3 className="text-2xl font-bold mb-4" style={{ color: "hsl(var(--text-primary))" }}>
                        Join Our Happy Customers
                    </h3>
                    <p className="text-lg mb-6" style={{ color: "hsl(var(--text-secondary))" }}>
                        Experience the freshness that over 1,200 customers rave about
                    </p>
                    <div className="flex flex-col sm:flex-row gap-4 justify-center">
                        <button className="btn-primary px-8 py-3 rounded-lg font-semibold">Start Shopping</button>
                        <button className="btn-outline px-8 py-3 rounded-lg font-semibold">Read All Reviews</button>
                    </div>
                </div>
            </div>
        </section>
    )
}
