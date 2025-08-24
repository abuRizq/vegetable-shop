"use client"

import React, { useCallback, useEffect, useMemo, useRef, useState } from "react"
import { ChevronLeft, ChevronRight, Leaf, ShoppingCart, Package } from "lucide-react"

// ---------- Types ----------
type SlideData = {
  id: number | string
  badge: string
  title: string[] // lines will stack on mobile/desktop
  description: string
  ctaText: string
  ctaAction: "fresh" | "organic" | "delivery" | (string & {})
  // Tailwind gradient classes (kept), tuned to be more comfortable and vegetable-themed
  gradientClass: string
}

// ---------- Default Slides (you can pass your own via props) ----------
const defaultSlides: SlideData[] = [
  {
    id: 1,
    badge: "#Fresh & Natural",
    title: ["Farm Fresh", "Vegetables", "Daily!"],
    description:
      "Handpicked from local farms, delivered fresh to your doorstep. Experience nature's best with every bite.",
    ctaText: "Shop Fresh Produce",
    ctaAction: "fresh",
    gradientClass:
      "bg-gradient-to-br from-emerald-800 via-emerald-600 to-lime-500 dark:from-emerald-900 dark:via-emerald-800 dark:to-lime-600",
  },
  {
    id: 2,
    badge: "#100% Organic",
    title: ["Certified", "Organic", "Quality!"],
    description: "No pesticides, no chemicals — just pure, organic goodness. Healthy choices for you and your family.",
    ctaText: "Explore Organic",
    ctaAction: "organic",
    gradientClass:
      "bg-gradient-to-br from-amber-600 via-orange-500 to-yellow-400 dark:from-amber-700 dark:via-orange-600 dark:to-yellow-500",
  },
  {
    id: 3,
    badge: "#Lightning Fast",
    title: ["Same Day", "Fresh", "Delivery!"],
    description: "Order before 2 PM and get your groceries delivered the same day. Convenience meets freshness.",
    ctaText: "Order Now",
    ctaAction: "delivery",
    gradientClass:
      "bg-gradient-to-br from-teal-600 via-emerald-500 to-lime-500 dark:from-teal-700 dark:via-emerald-600 dark:to-lime-600",
  },
]

// ---------- Helpers ----------
function CTAIcon({ action }: { action: SlideData["ctaAction"] }) {
  if (action === "fresh") return <ShoppingCart className="w-5 h-5" />
  if (action === "organic") return <Leaf className="w-5 h-5" />
  if (action === "delivery") return <Package className="w-5 h-5" />
  return <ShoppingCart className="w-5 h-5" />
}

// ---------- Slide ----------
function Slide({
  slide,
  isActive,
  onCTA,
}: {
  slide: SlideData
  isActive: boolean
  onCTA: (action: SlideData["ctaAction"]) => void
}) {
  return (
    <div
      className={[
        // Container
        "min-w-full h-full px-6 sm:px-8 md:px-12 py-14 md:py-16",
        "flex items-center justify-center relative overflow-hidden rounded-3xl",
        // Background
        slide.gradientClass,
        // Soft glass overlay for readability
        "before:absolute before:inset-0 before:bg-gradient-to-b",
        "before:from-black/20 before:via-black/10 before:to-black/30",
        "before:pointer-events-none",
      ].join(" ")}
      role="group"
      aria-roledescription="slide"
    >
      {/* Decorative soft lights */}
      <div className="pointer-events-none absolute -left-24 -top-24 w-64 h-64 rounded-full bg-white/10 blur-3xl" />
      <div className="pointer-events-none absolute -right-24 -bottom-24 w-72 h-72 rounded-full bg-white/10 blur-3xl" />

      <div
        className={[
          "max-w-2xl text-center relative z-10",
          // Fade/slide in for active slide (motion-safe)
          isActive
            ? "motion-safe:opacity-100 motion-safe:translate-y-0"
            : "motion-safe:opacity-0 motion-safe:translate-y-3",
          "motion-safe:transition-all motion-safe:duration-500",
        ].join(" ")}
        aria-live={isActive ? "polite" : "off"}
      >
        {/* Badge */}
        <div
          className={[
            "inline-flex items-center bg-white/15 backdrop-blur-md text-white",
            "px-4 py-2 rounded-full text-xs sm:text-sm font-semibold",
            "tracking-wider uppercase mb-6 border border-white/20",
            isActive ? "shadow-md shadow-black/10" : "",
          ].join(" ")}
        >
          {slide.badge}
        </div>

        {/* Title */}
        <h1
          className={[
            "text-white font-black leading-tight mb-4 drop-shadow-md",
            "text-3xl sm:text-4xl md:text-5xl lg:text-6xl",
            "tracking-tight",
          ].join(" ")}
        >
          {slide.title.map((line, i) => (
            <React.Fragment key={i}>
              {line}
              {i < slide.title.length - 1 && <br />}
            </React.Fragment>
          ))}
        </h1>

        {/* Description */}
        <p className="text-white/90 text-base sm:text-lg md:text-xl leading-relaxed mb-8 max-w-lg mx-auto">
          {slide.description}
        </p>

        {/* CTA Button */}
        <button
          onClick={() => onCTA(slide.ctaAction)}
          className={[
            "inline-flex items-center gap-2",
            "px-6 sm:px-8 py-3 sm:py-4 rounded-full font-semibold text-base sm:text-lg",
            // Themed CTA
            "bg-white text-gray-900 hover:-translate-y-0.5 active:scale-95",
            "shadow-lg hover:shadow-xl transition-all duration-300",
            "focus:outline-none focus-visible:ring-2 focus-visible:ring-offset-2",
          ].join(" ")}
          style={{
            // Ring in brand color
            boxShadow: "var(--shadow-lg)",
          }}
        >
          <CTAIcon action={slide.ctaAction} />
          <span>{slide.ctaText}</span>
        </button>
      </div>
    </div>
  )
}

// ---------- Indicators ----------
function Indicators({
  current,
  total,
  onSelect,
}: {
  current: number
  total: number
  onSelect: (idx: number) => void
}) {
  return (
    <div
      className="absolute bottom-4 sm:bottom-6 left-1/2 -translate-x-1/2 flex gap-2 z-20"
      role="tablist"
      aria-label="Slider indicators"
    >
      {Array.from({ length: total }, (_, i) => (
        <button
          key={i}
          onClick={() => onSelect(i)}
          role="tab"
          aria-selected={i === current}
          aria-label={`Go to slide ${i + 1}`}
          className={[
            "h-2.5 sm:h-3 rounded-full transition-all duration-300",
            "border border-white/30 hover:scale-110",
            i === current ? "bg-white w-6 sm:w-8 shadow-lg" : "bg-white/40 w-2.5 sm:w-3 hover:bg-white/70",
          ].join(" ")}
        />
      ))}
    </div>
  )
}

// ---------- Controls ----------
function ArrowButton({ dir, onClick }: { dir: "prev" | "next"; onClick: () => void }) {
  const isPrev = dir === "prev"
  const Icon = isPrev ? ChevronLeft : ChevronRight
  return (
    <button
      onClick={onClick}
      aria-label={isPrev ? "Previous slide" : "Next slide"}
      className={[
        "absolute top-1/2 -translate-y-1/2 z-20",
        isPrev ? "left-3 sm:left-4" : "right-3 sm:right-4",
        "rounded-full backdrop-blur-md bg-white/15 border border-white/25",
        "p-2 sm:p-3 text-white hover:bg-white/25 active:scale-95 transition",
        "focus:outline-none focus-visible:ring-2 focus-visible:ring-white/60",
      ].join(" ")}
    >
      <Icon className="w-5 h-5 sm:w-6 sm:h-6" aria-hidden="true" />
    </button>
  )
}

// ---------- Main Slider ----------
function HeroSlider({
  slides = defaultSlides,
  autoPlayMs = 5000,
  className = "",
  onCTA,
}: {
  slides?: SlideData[]
  autoPlayMs?: number
  className?: string
  onCTA?: (action: SlideData["ctaAction"]) => void
}) {
  const [index, setIndex] = useState(0)
  const total = slides.length
  const containerRef = useRef<HTMLDivElement | null>(null)
  const timerRef = useRef<NodeJS.Timeout | null>(null)
  const isHoverRef = useRef(false)
  const startX = useRef<number | null>(null)
  const deltaX = useRef(0)

  const goTo = useCallback((i: number) => setIndex((prev) => (i + total) % total), [total])
  const next = useCallback(() => goTo(index + 1), [index, goTo])
  const prev = useCallback(() => goTo(index - 1), [index, goTo])

  const handleCTA = useCallback(
    (action: SlideData["ctaAction"]) => {
      if (onCTA) onCTA(action)
      else {
        const map: Record<string, string> = {
          fresh: "Ready to shop fresh produce! 🥬🥕",
          organic: "Exploring organic options! 🌱",
          delivery: "Fast delivery coming your way! 🚚",
        }
        alert(map[action as string] ?? "Taking action!")
      }
    },
    [onCTA],
  )

  // Autoplay
  const clearTimer = () => {
    if (timerRef.current) clearInterval(timerRef.current)
    timerRef.current = null
  }
  const startTimer = useCallback(() => {
    clearTimer()
    if (autoPlayMs > 0 && !isHoverRef.current) {
      timerRef.current = setInterval(() => {
        next()
      }, autoPlayMs)
    }
  }, [autoPlayMs, next])

  useEffect(() => {
    startTimer()
    return clearTimer
  }, [index, startTimer])

  // Pause on hover
  const onMouseEnter = () => {
    isHoverRef.current = true
    clearTimer()
  }
  const onMouseLeave = () => {
    isHoverRef.current = false
    startTimer()
  }

  // Keyboard arrows
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "ArrowRight") next()
      if (e.key === "ArrowLeft") prev()
    }
    window.addEventListener("keydown", onKey)
    return () => window.removeEventListener("keydown", onKey)
  }, [next, prev])

  // Touch swipe
  const onTouchStart = (e: React.TouchEvent) => {
    startX.current = e.touches[0].clientX
    deltaX.current = 0
    clearTimer()
  }
  const onTouchMove = (e: React.TouchEvent) => {
    if (startX.current == null) return
    deltaX.current = e.touches[0].clientX - startX.current
  }
  const onTouchEnd = () => {
    if (Math.abs(deltaX.current) > 50) {
      deltaX.current < 0 ? next() : prev()
    }
    startX.current = null
    startTimer()
  }

  // Transform for track
  const trackStyle = useMemo(() => ({ transform: `translateX(-${index * 100}%)` }), [index])

  return (
    <section
      ref={containerRef}
      className={[
        // Container sizes: good on phones & laptops
        "w-[92%] mx-auto my-3 sm:my-4",
        "h-[360px] sm:h-[340px] md:h-[520px] lg:h-[500px]",
        "relative select-none p-2",
        // Themed shell
        "rounded-3xl border",
        "bg-[hsl(var(--paper))] border-[hsl(var(--divider))]",
        "shadow-xl",
        className,
      ].join(" ")}
      onMouseEnter={onMouseEnter}
      onMouseLeave={onMouseLeave}
      onTouchStart={onTouchStart}
      onTouchMove={onTouchMove}
      onTouchEnd={onTouchEnd}
      aria-roledescription="carousel"
      aria-label="FreshVeggies highlights"
    >
      {/* Slides track */}
      <div className="absolute inset-0 overflow-hidden rounded-2xl" role="presentation">
        <div className="flex h-full transition-transform duration-500 ease-out" style={trackStyle}>
          {slides.map((s, i) => (
            <Slide key={s.id} slide={s} isActive={i === index} onCTA={handleCTA} />
          ))}
        </div>
      </div>

      {/* Controls */}
      <ArrowButton dir="prev" onClick={prev} />
      <ArrowButton dir="next" onClick={next} />

      {/* Indicators */}
      <Indicators current={index} total={total} onSelect={goTo} />

      {/* Soft inner shadow frame for depth */}
      <div className="pointer-events-none absolute inset-0 rounded-2xl ring-1 ring-black/5" />
    </section>
  )
}
export {
  HeroSlider
}
/*
USAGE
------
import HeroSlider from "@/components/hero-slider"

<HeroSlider />
// or pass your own slides:
<HeroSlider slides={mySlides} autoPlayMs={6000} onCTA={(a)=>router.push(`/shop?tag=${a}`)} />
*/
