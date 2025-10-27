"use client";

import { HeroSlider, Category, SeasonalSection } from "@/widgets/home-widgets/"
import { Header } from "@/widgets/header/";

// ----------- Main Page -----------
export default function HomePage() {
    return (
        <div
            className="min-h-screen w-full content-transition"
            // style={{ backgroundColor: "hsl(var(--background))" }}
        >
            <main className="min-h-screen">
                <Header />
                <HeroSlider />
                <Category />
                {/* Seasonal & Freshly Arrived Sections */}
                <SeasonalSection />
                {/* <FreshlyArrived /> */}
                {/* Customer Reviews Section */}
                {/* <CustomerReviews /> */}
            </main>
        </div>
    );
}