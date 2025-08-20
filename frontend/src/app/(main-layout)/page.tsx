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
import Sidbar from "../_Components/sidbar";
import Searchbar from "../_Components/header/search-bar";
import Header from "../_Components/header/header";
import HeroSlider from "../_Components/main/Hero";
import Catagry from "../_Components/main/catagry";
import { FreshlyArrived } from "../_Components/main/freshly-arrived";
import { SeasonalSection } from "../_Components/main/seasonal-section";
import { CustomerReviews } from "../_Components/main/customer-reviews";

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