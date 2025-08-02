"use client"

import type React from "react"

import { useState, useEffect, useRef } from "react"
import Image from "next/image"
import { ChevronLeft, ChevronRight, Play, Info, Heart, Plus } from "lucide-react"

interface SliderImage {
    id: string
    src: string
    alt: string
    title?: string
    description?: string
    category?: string
    rating?: number
}

interface ImageSliderProps {
    images: SliderImage[]
    autoPlay?: boolean
    autoPlayInterval?: number
    showControls?: boolean
    showIndicators?: boolean
    showOverlay?: boolean
    className?: string
    aspectRatio?: "16/9" | "4/3" | "1/1" | "21/9"
}

function ImageSlider({
    images,
    autoPlay = false,
    autoPlayInterval = 5000,
    showControls = true,
    showIndicators = true,
    showOverlay = true,
    className = "",
    aspectRatio = "16/9",
}: ImageSliderProps) {
    const [currentIndex, setCurrentIndex] = useState(0)
    const [isLoading, setIsLoading] = useState(true)
    const [loadedImages, setLoadedImages] = useState<Set<number>>(new Set())
    const [isHovered, setIsHovered] = useState(false)
    const [touchStart, setTouchStart] = useState(0)
    const [touchEnd, setTouchEnd] = useState(0)
    const intervalRef = useRef<NodeJS.Timeout | null>(null)

    // Auto-play functionality
    useEffect(() => {
        if (autoPlay && !isHovered && images.length > 1) {
            intervalRef.current = setInterval(() => {
                setCurrentIndex((prev) => (prev + 1) % images.length)
            }, autoPlayInterval)
        }

        return () => {
            if (intervalRef.current) {
                clearInterval(intervalRef.current)
            }
        }
    }, [autoPlay, autoPlayInterval, images.length, isHovered])

    // Navigation functions
    const goToPrevious = () => {
        setCurrentIndex((prev) => (prev - 1 + images.length) % images.length)
    }

    const goToNext = () => {
        setCurrentIndex((prev) => (prev + 1) % images.length)
    }

    const goToSlide = (index: number) => {
        setCurrentIndex(index)
    }

    // Touch handlers for mobile swipe
    const handleTouchStart = (e: React.TouchEvent) => {
        setTouchStart(e.targetTouches[0].clientX)
    }

    const handleTouchMove = (e: React.TouchEvent) => {
        setTouchEnd(e.targetTouches[0].clientX)
    }

    const handleTouchEnd = () => {
        if (!touchStart || !touchEnd) return

        const distance = touchStart - touchEnd
        const isLeftSwipe = distance > 50
        const isRightSwipe = distance < -50

        if (isLeftSwipe) {
            goToNext()
        } else if (isRightSwipe) {
            goToPrevious()
        }
    }

    // Image load handler
    const handleImageLoad = (index: number) => {
        setLoadedImages((prev) => new Set([...prev, index]))
        if (index === 0) {
            setIsLoading(false)
        }
    }

    if (!images || images.length === 0) {
        return (
            <div className="w-full h-64 bg-gray-200 dark:bg-gray-800 rounded-xl flex items-center justify-center">
                <p className="text-gray-500 dark:text-gray-400">No images available</p>
            </div>
        )
    }

    const currentImage = images[currentIndex]
    const aspectRatioClass = {
        "16/9": "aspect-video",
        "4/3": "aspect-[4/3]",
        "1/1": "aspect-square",
        "21/9": "aspect-[21/9]",
    }[aspectRatio]

    return (
        <div
            className={`relative w-full ${aspectRatioClass} rounded-xl overflow-hidden bg-gray-900 group ${className}`}
            onMouseEnter={() => setIsHovered(true)}
            onMouseLeave={() => setIsHovered(false)}
            onTouchStart={handleTouchStart}
            onTouchMove={handleTouchMove}
            onTouchEnd={handleTouchEnd}
        >
            {/* Main Image Container */}
            <div className="relative w-full h-full">
                {images.map((image, index) => (
                    <div
                        key={image.id}
                        className={`absolute inset-0 transition-all duration-700 ease-in-out ${index === currentIndex ? "opacity-100 scale-100" : "opacity-0 scale-105"
                            }`}
                    >
                        <Image
                            src={image.src || "/placeholder.svg"}
                            alt={image.alt}
                            fill
                            className="object-cover"
                            priority={index === 0}
                            onLoad={() => handleImageLoad(index)}
                            sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
                        />
                    </div>
                ))}

                {/* Loading Overlay */}
                {isLoading && (
                    <div className="absolute inset-0 bg-gray-900 flex items-center justify-center">
                        <div className="w-8 h-8 border-2 border-red-500 border-t-transparent rounded-full animate-spin"></div>
                    </div>
                )}

                {/* Gradient Overlay */}
                <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent" />

                {/* Content Overlay */}
                {showOverlay && currentImage && (
                    <div
                        className={`absolute bottom-0 left-0 right-0 p-6 text-white transition-all duration-300 ${isHovered ? "translate-y-0 opacity-100" : "translate-y-2 opacity-90"
                            }`}
                    >
                        {currentImage.category && (
                            <span className="inline-block px-2 py-1 bg-red-600 text-xs font-semibold rounded mb-2">
                                {currentImage.category}
                            </span>
                        )}

                        {currentImage.title && <h3 className="text-xl font-bold mb-2 line-clamp-1">{currentImage.title}</h3>}

                        {currentImage.description && (
                            <p className="text-sm text-gray-200 mb-4 line-clamp-2">{currentImage.description}</p>
                        )}

                        {currentImage.rating && (
                            <div className="flex items-center mb-4">
                                <div className="flex items-center">
                                    {[...Array(5)].map((_, i) => (
                                        <div
                                            key={i}
                                            className={`w-4 h-4 ${i < Math.floor(currentImage.rating!) ? "text-yellow-400" : "text-gray-400"
                                                }`}
                                        >
                                            ★
                                        </div>
                                    ))}
                                    <span className="ml-2 text-sm">{currentImage.rating}/5</span>
                                </div>
                            </div>
                        )}

                        {/* Action Buttons */}
                        <div className="flex items-center space-x-3">
                            <button className="flex items-center space-x-2 bg-white text-black px-4 py-2 rounded-lg hover:bg-gray-200 transition-colors duration-200">
                                <Play size={16} />
                                <span className="font-semibold">Play</span>
                            </button>
                            <button className="flex items-center space-x-2 bg-gray-800/80 text-white px-4 py-2 rounded-lg hover:bg-gray-700/80 transition-colors duration-200">
                                <Info size={16} />
                                <span>More Info</span>
                            </button>
                            <button className="p-2 bg-gray-800/80 text-white rounded-full hover:bg-gray-700/80 transition-colors duration-200">
                                <Heart size={16} />
                            </button>
                            <button className="p-2 bg-gray-800/80 text-white rounded-full hover:bg-gray-700/80 transition-colors duration-200">
                                <Plus size={16} />
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {/* Navigation Controls */}
            {showControls && images.length > 1 && (
                <>
                    <button
                        onClick={goToPrevious}
                        className={`absolute left-4 top-1/2 -translate-y-1/2 w-10 h-10 bg-black/50 hover:bg-black/70 text-white rounded-full flex items-center justify-center transition-all duration-200 ${isHovered ? "opacity-100 scale-100" : "opacity-0 scale-90"
                            }`}
                    >
                        <ChevronLeft size={20} />
                    </button>
                    <button
                        onClick={goToNext}
                        className={`absolute right-4 top-1/2 -translate-y-1/2 w-10 h-10 bg-black/50 hover:bg-black/70 text-white rounded-full flex items-center justify-center transition-all duration-200 ${isHovered ? "opacity-100 scale-100" : "opacity-0 scale-90"
                            }`}
                    >
                        <ChevronRight size={20} />
                    </button>
                </>
            )}

            {/* Indicators */}
            {showIndicators && images.length > 1 && (
                <div className="absolute bottom-4 left-1/2 -translate-x-1/2 flex space-x-2">
                    {images.map((_, index) => (
                        <button
                            key={index}
                            onClick={() => goToSlide(index)}
                            className={`w-2 h-2 rounded-full transition-all duration-200 ${index === currentIndex ? "bg-white scale-125" : "bg-white/50 hover:bg-white/75"
                                }`}
                        />
                    ))}
                </div>
            )}

            {/* Image Counter */}
            <div className="absolute top-4 right-4 bg-black/50 text-white px-2 py-1 rounded text-sm">
                {currentIndex + 1} / {images.length}
            </div>
        </div>
    )
}

export default ImageSlider
