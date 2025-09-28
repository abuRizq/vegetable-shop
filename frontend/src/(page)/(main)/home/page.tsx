"use client";

import { HeroSlider, Category } from "@/widgets/home-widgets/"
import { Header } from "@/widgets/header/";
import { MainLayout } from "@/widgets/layouts";

// ----------- Main Page -----------
export default function HomePage() {
    return (
        <div
            className="min-h-screen w-full content-transition"
            style={{ backgroundColor: "hsl(var(--background))" }}
        >
            <main className="min-h-screen">
                <MainLayout>
                    <Header />
                    <HeroSlider />
                    <Category />
                </MainLayout>
                {/* Seasonal & Freshly Arrived Sections */}
                {/* <SeasonalSection /> */}
                {/* <FreshlyArrived /> */}
                {/* Customer Reviews Section */}
                {/* <CustomerReviews /> */}
            </main>
        </div>
    );
}