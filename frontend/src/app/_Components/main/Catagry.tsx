import React, { useEffect, useRef, useState } from "react";

function useReveal<T extends HTMLElement>(options?: IntersectionObserverInit) {
    const ref = useRef<T | null>(null);
    const [visible, setVisible] = useState(false);

    useEffect(() => {
        const el = ref.current;
        if (!el) return;
        const io = new IntersectionObserver(
            ([entry]) => {
                if (entry.isIntersecting) {
                    setVisible(true);
                    io.unobserve(entry.target); // reveal once
                }
            },
            { root: null, rootMargin: "0px 0px -8% 0px", threshold: 0.15, ...options }
        );
        io.observe(el);
        return () => io.disconnect();
    }, [options]);

    return { ref, visible };
}

type Card = {
    title: string;
    emoji: string;
    colorClass: string; // bg-* token you already use
    buttonClass: string; // your existing button variants
    description: string;
};

const CARDS: Card[] = [
    {
        title: "Leafy Greens",
        emoji: "🥬",
        colorClass: "bg-primary",
        buttonClass: "btn-primary",
        description: "Fresh spinach, lettuce, kale and more",
    },
    {
        title: "Root Vegetables",
        emoji: "🥕",
        colorClass: "bg-secondary",
        buttonClass: "btn-secondary",
        description: "Carrots, potatoes, onions and more",
    },
    {
        title: "Fresh Produce",
        emoji: "🥒",
        colorClass: "bg-accent",
        buttonClass: "btn-outline",
        description: "Cucumbers, tomatoes, peppers and more",
    },
    {
        title: "Fruits",
        emoji: "🍎",
        colorClass: "bg-primary",
        buttonClass: "btn-primary",
        description: "Apples, bananas, berries and more",
    },
    {
        title: "Herbs",
        emoji: "🌿",
        colorClass: "bg-secondary",
        buttonClass: "btn-secondary",
        description: "Mint, basil, coriander and more",
    },
    {
        title: "Organic Boxes",
        emoji: "📦",
        colorClass: "bg-accent",
        buttonClass: "btn-outline",
        description: "Curated seasonal organic veggie boxes",
    },
];

function CategoryCard({ c, idx }: { c: Card; idx: number }) {
    const { ref, visible } = useReveal<HTMLDivElement>();
    return (
        <div
            ref={ref}
            className={[
                "card p-6 group transform transition-all duration-700 ease-out",
                "hover:shadow-green hover:scale-[1.03] hover:-translate-y-2",
                // reveal: fade + slight rise; stagger with idx
                visible
                    ? "opacity-100 translate-y-0"
                    : "opacity-0 translate-y-5",
            ].join(" ")}
            style={{ transitionDelay: `${Math.min(idx * 80, 240)}ms` }}
        >
            <div className="flex items-center mb-4">
                <div
                    className={[
                        "w-12 h-12 rounded-lg flex items-center justify-center mr-4",
                        c.colorClass,
                        "transition-transform duration-500 ease-out",
                        "group-hover:rotate-12",
                    ].join(" ")}
                >
                    <span className="text-2xl">{c.emoji}</span>
                </div>
                <h3
                    className="text-xl font-semibold transition-colors duration-300"
                    style={{ color: "hsl(var(--text-primary))" }}
                >
                    {c.title}
                </h3>
            </div>

            <div className="space-y-3">
                <div
                    className={[
                        "h-32 rounded-lg flex items-center justify-center overflow-hidden",
                        c.colorClass,
                        "transition-colors duration-500",
                    ].join(" ")}
                >
                    <span className="text-4xl transition-transform duration-500 ease-out group-hover:scale-125">
                        {c.emoji}
                    </span>
                </div>
                <p
                    style={{ color: "hsl(var(--text-secondary))" }}
                    className="transition-opacity duration-300"
                >
                    {c.description}
                </p>
                <button
                    className={[
                        c.buttonClass,
                        "w-full py-2 rounded-lg transition-all duration-300",
                        "hover:shadow-lg active:scale-95",
                    ].join(" ")}
                >
                    Shop Now
                </button>
            </div>
        </div>
    );
}

export default function Category() {
    return (
        <section className="w-[92%] mx-auto mt-10">
            <h2
                className="text-3xl font-bold mb-6"
                style={{ color: "hsl(var(--text-primary))" }}
            >
                Fresh Categories
            </h2>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                {CARDS.map((c, i) => (
                    <CategoryCard key={c.title} c={c} idx={i} />
                ))}
            </div>
        </section>
    );
}
