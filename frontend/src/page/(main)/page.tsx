"use client";

import Header from "@/_Components/header/header";
import Catagry from "@/_Components/main/catagry";
import HeroSlider from "@/_Components/main/Hero";


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