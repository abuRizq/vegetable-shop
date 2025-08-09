"use client";
import Image from "next/image";
import {
  Bell,
  Settings,
  LogOut,
  Users,
  PartyPopper,
  Home,
  ChevronLeft,
  Plus,
  Heart,
  Play,
} from "lucide-react";
import Sidbar from "../_Components/Sidbar";
import Searchbar from "../_Components/header/Searchbar";
import Header from "../_Components/header/header";
import HeroSlider from "../_Components/main/Hero";

// --- Types

type FriendsSidebarProps = {
  avatars: string[];
};



// ----------- Right Sidebar -----------
// function FriendsSidebar({ avatars }: FriendsSidebarProps) {
//   return (
//     <aside
//       className="fixed right-0 top-0 h-screen w-20 flex flex-col items-center bg-white dark:bg-gray-900"
//       style={{
//         borderRadius: 0,
//         boxShadow: "none",
//         borderLeft: "1px solid #e6e6e6",
//         zIndex: 40,
//       }}
//     >
//       {/* زر + */}
//       <span
//         className="flex items-center justify-center bg-red-500 dark:bg-red-600 text-white rounded-full hover:bg-red-600 dark:hover:bg-red-700 transition-colors"
//         style={{
//           width: 40,
//           height: 40,
//           marginTop: 60,
//           marginBottom: 30,
//           fontSize: 28,
//           cursor: "pointer",
//         }}
//       >
//         <Plus size={28} />
//       </span>
//       {avatars.slice(1).map((src: string, i: number) => (
//         <Image
//           key={i}
//           src={src}
//           alt={`Friend ${i + 1}`}
//           width={38}
//           height={38}
//           className="rounded-full object-cover"
//           style={{
//             marginBottom: 25,
//             border: "1.5px solid #ececf2",
//             background: "#f7f7fc",
//             width: 38,
//             height: 38,
//           }}
//         />
//       ))}
//     </aside>
//   );
// }

// ----------- Main Page -----------
export default function HomePage() {


  return (
    <div
      className="min-h-screen w-full content-transition"
      style={{ backgroundColor: "hsl(var(--background))" }}
    >
      <main className="min-h-screen">
        <Header />
        <HeroSlider />
        <div className="w-[92%] mx-auto mt-10">
          <h2
            className="text-3xl font-bold mb-6"
            style={{ color: "hsl(var(--text-primary))" }}
          >
            Fresh Categories
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <div className="card p-6 hover:shadow-green transition-all duration-700 ease-in-out hover:scale-105 hover:-translate-y-2 transform">
              <div className="flex items-center mb-4">
                <div className="w-12 h-12 bg-primary rounded-lg flex items-center justify-center mr-4 transition-transform duration-600 ease-in-out group-hover:rotate-12">
                  <span className="text-2xl">🥬</span>
                </div>
                <h3
                  className="text-xl font-semibold transition-colors duration-400 ease-in-out"
                  style={{ color: "hsl(var(--text-primary))" }}
                >
                  Leafy Greens
                </h3>
              </div>
              <div className="space-y-3">
                <div className="h-32 bg-primary rounded-lg flex items-center justify-center overflow-hidden transition-colors duration-500 ease-in-out">
                  <span className="text-4xl transition-transform duration-600 ease-in-out hover:scale-125">🥬</span>
                </div>
                <p style={{ color: "hsl(var(--text-secondary))" }} className="transition-opacity duration-400 ease-in-out">
                  Fresh spinach, lettuce, kale and more
                </p>
                <button className="btn-primary w-full py-2 rounded-lg transition-all duration-500 ease-in-out hover:shadow-lg active:scale-95">
                  Shop Now
                </button>
              </div>
            </div>

            <div className="card p-6 hover:shadow-green transition-all duration-700 ease-in-out hover:scale-105 hover:-translate-y-2 transform group">
              <div className="flex items-center mb-4">
                <div className="w-12 h-12 bg-secondary rounded-lg flex items-center justify-center mr-4 transition-transform duration-600 ease-in-out group-hover:rotate-12">
                  <span className="text-2xl">🥕</span>
                </div>
                <h3
                  className="text-xl font-semibold transition-colors duration-400 ease-in-out"
                  style={{ color: "hsl(var(--text-primary))" }}
                >
                  Root Vegetables
                </h3>
              </div>
              <div className="space-y-3">
                <div className="h-32 bg-secondary rounded-lg flex items-center justify-center overflow-hidden transition-colors duration-500 ease-in-out">
                  <span className="text-4xl transition-transform duration-600 ease-in hover:scale-125">🥕</span>
                </div>
                <p style={{ color: "hsl(var(--text-secondary))" }} className="transition-opacity duration-400 ease-in-out">
                  Carrots, potatoes, onions and more
                </p>
                <button className="btn-secondary w-full py-2 rounded-lg transition-all duration-500 ease-in-out hover:shadow-lg active:scale-95">
                  Shop Now
                </button>
              </div>
            </div>

            <div className="card p-6 hover:shadow-green transition-all duration-700 ease-in-out hover:scale-105 hover:-translate-y-2 transform group">
              <div className="flex items-center mb-4">
                <div className="w-12 h-12 bg-accent rounded-lg flex items-center justify-center mr-4 transition-transform duration-600 ease-in-out group-hover:rotate-12">
                  <span className="text-2xl">🥒</span>
                </div>
                <h3
                  className="text-xl font-semibold transition-colors duration-400 ease-in-out"
                  style={{ color: "hsl(var(--text-primary))" }}
                >
                  Fresh Produce
                </h3>
              </div>
              <div className="space-y-3">
                <div className="h-32 bg-accent rounded-lg flex items-center justify-center overflow-hidden transition-colors duration-500 ease-in-out">
                  <span className="text-4xl transition-transform duration-600 ease-in-out hover:scale-125">🥒</span>
                </div>
                <p style={{ color: "hsl(var(--text-secondary))" }} className="transition-opacity duration-400 ease-in-out">
                  Cucumbers, tomatoes, peppers and more
                </p>
                <button className="btn-outline w-full py-2 rounded-lg transition-all duration-500 ease-in-out hover:shadow-lg active:scale-95">
                  Shop Now
                </button>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>
  );
}