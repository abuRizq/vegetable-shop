"use client";

import Header from "@/app/_Components/header/header";
import Catagry from "@/app/_Components/main/catagry";
import HeroSlider from "@/app/_Components/main/Hero";




// ----------- Main Page -----------
export default function HomePage  ()  {
  return (
    <div
      className="min-h-screen w-full content-transition"
      style={{ backgroundColor: "hsl(var(--background))" }}
    >
      <main className="min-h-screen">
        <Header />
        <HeroSlider />
        <Catagry />
        {/* Seasonal & Freshly Arrived Sections */}
        {/* <SeasonalSection /> */}
        {/* <FreshlyArrived /> */}
        {/* Customer Reviews Section */}
        {/* <CustomerReviews /> */}
      </main>
    </div>
  );
}