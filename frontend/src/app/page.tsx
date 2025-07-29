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
import ThemeToggle from "./_Components/ThemeToggle";

// --- Types
type NavItemProps = {
  icon: React.ReactNode;
  text: string;
  active?: boolean;
};
type FriendsSidebarProps = {
  avatars: string[];
};

// ----------- Sidebar Item -----------
function NavItem({ icon, text, active = false }: NavItemProps) {
  return (
    <div
      className={`flex items-center gap-3 px-3 py-[7px] rounded-md cursor-pointer transition-all
        ${active
          ? "bg-[#fbeff1] dark:bg-red-900/20 text-[#e50914] dark:text-red-400 font-bold"
          : "hover:bg-[#f8f8f8] dark:hover:bg-gray-700 text-[#232323] dark:text-gray-300 font-normal"
        }
      `}
      style={{
        fontSize: 15,
        fontWeight: active ? 700 : 500,
        letterSpacing: "0.02em",
        marginBottom: 2,
        fontFamily: "Geist, Roboto, Arial, sans-serif",
      }}
    >
      <span className="text-[20px] flex items-center justify-center">{icon}</span>
      {text}
    </div>
  );
}

// ----------- Right Sidebar -----------
function FriendsSidebar({ avatars }: FriendsSidebarProps) {
  return (
    <aside
      className="fixed right-0 top-0 h-screen w-20 flex flex-col items-center bg-white dark:bg-gray-900"
      style={{
        borderRadius: 0,
        boxShadow: "none",
        borderLeft: "1px solid #e6e6e6",
        zIndex: 40,
      }}
    >
      {/* زر + */}
      <span
        className="flex items-center justify-center bg-red-500 dark:bg-red-600 text-white rounded-full hover:bg-red-600 dark:hover:bg-red-700 transition-colors"
        style={{
          width: 40,
          height: 40,
          marginTop: 60,
          marginBottom: 30,
          fontSize: 28,
          cursor: "pointer",
        }}
      >
        <Plus size={28} />
      </span>
      {avatars.slice(1).map((src: string, i: number) => (
        <Image
          key={i}
          src={src}
          alt={`Friend ${i + 1}`}
          width={38}
          height={38}
          className="rounded-full object-cover"
          style={{
            marginBottom: 25,
            border: "1.5px solid #ececf2",
            background: "#f7f7fc",
            width: 38,
            height: 38,
          }}
        />
      ))}
    </aside>
  );
}

// ----------- Main Page -----------
export default function HomePage() {
  const avatars: string[] = [
    "/avatars/user1.jpg",
    "/avatars/user2.jpg",
    "/avatars/user3.jpg",
    "/avatars/user4.jpg",
    "/avatars/user5.jpg",
    "/avatars/user6.jpg",
    "/avatars/user7.jpg",
  ];

  return (
    <div
      className="bg-[#fafbfc] dark:bg-gray-900 min-h-screen flex text-[#232323] dark:text-white relative transition-colors"
      style={{ fontFamily: "Geist, Roboto, Arial, sans-serif" }}
    >
      {/* ---- Left Sidebar ---- */}
      <aside
        className="min-h-screen z-30 flex flex-col px-0 bg-white dark:bg-gray-800 border-r border-gray-200 dark:border-gray-700"
        style={{
          width: 220,
          paddingTop: 18,
          paddingLeft: 20,
          paddingRight: 0,
        }}
      >
        {/* Logo */}
        <div
          className="flex items-center font-black mb-7 text-[#232323] dark:text-white"
          style={{
            fontSize: 22,
            letterSpacing: "-1px",
            fontFamily: "Geist, Roboto, Arial, sans-serif",
          }}
        >
          <div className="mr-3">
            <ThemeToggle />
          </div>
          Netflix
          <span className="text-[#e50914] text-[26px] ml-1 font-black">.</span>
        </div>

        {/* MENU */}
        <div
          className="text-gray-500 dark:text-gray-400"
          style={{
            fontSize: 13,
            fontWeight: 500,
            marginBottom: 30,
            marginTop: 0,
            letterSpacing: "0.02em",
          }}
        >
          Menu
        </div>
        <nav className="mb-1 flex flex-col gap-1">
          <NavItem icon={<Home size={20} />} text="Browse" active />
          <NavItem icon={<Heart size={20} strokeWidth={1.7} />} text="Watchlist" />
          <NavItem icon={<Play size={20} strokeWidth={1.7} />} text="Coming soon" />
        </nav>

        {/* Social */}
        <div
          className="text-gray-500 dark:text-gray-400"
          style={{
            fontSize: 13,
            fontWeight: 500,
            marginTop: 12,
            marginBottom: 30,
            letterSpacing: "0.02em",
          }}
        >
          Social
        </div>
        <nav className="mb-1 flex flex-col gap-1">
          <NavItem icon={<Users size={20} />} text="Friends" />
          <NavItem icon={<PartyPopper size={20} />} text="Parties" />
        </nav>

        {/* General */}
        <div
          className="text-gray-500 dark:text-gray-400"
          style={{
            fontSize: 13,
            fontWeight: 500,
            marginTop: 12,
            marginBottom: 30,
            letterSpacing: "0.02em",
          }}
        >
          General
        </div>
        <nav className="mb-1 flex flex-col gap-1">
          <NavItem icon={<Settings size={20} />} text="Setting" />
          <NavItem icon={<LogOut size={20} />} text="Log out" />
        </nav>
      </aside>

      {/* ---- Right Sidebar ---- */}
      <FriendsSidebar avatars={avatars} />

      {/* ---- Main Content ---- */}
      <main className="flex-1 px-0 py-0 relative ml-0 mr-20">
        {/* ---- Top Bar ---- */}
        <header
          className="flex justify-between items-center mb-8"
          style={{
            padding: "12px 30px 10px 30px",
          }}
        >
          <button className="text-2xl px-3 py-1 rounded-full hover:bg-gray-100 dark:hover:bg-gray-700 text-gray-700 dark:text-gray-300 transition-colors">
            <ChevronLeft size={24} />
          </button>
          <div className="flex-1 max-w-md mx-6">
            <input
              type="text"
              className="w-full rounded-2xl border border-gray-200 dark:border-gray-600 px-4 py-2 bg-[#f5f5f5] dark:bg-gray-700 text-gray-900 dark:text-white outline-none focus:ring-2 focus:ring-red-500 transition-colors"
              placeholder="Search..."
              style={{
                fontFamily: "Geist, Roboto, Arial, sans-serif",
                fontSize: 15,
              }}
            />
          </div>
          <div className="flex items-center gap-4">
            <Bell size={22} className="text-gray-600 dark:text-gray-400" />
            <Image
              src={avatars[0]}
              alt="User"
              width={40}
              height={40}
              className="w-10 h-10 rounded-full border-2 border-gray-300 dark:border-gray-600"
            />
            <div>
              <div className="font-bold text-gray-900 dark:text-white">John</div>
              <div className="text-xs text-gray-400 dark:text-gray-500">Level 12</div>
            </div>
          </div>
        </header>

        {/* ---- Banner ---- */}
        <section
          className="relative rounded-2xl mb-10 overflow-hidden"
          style={{
            minHeight: 170,
            height: 220,
            display: "flex",
            alignItems: "flex-end",
            marginLeft: 30,
            marginRight: 30,
          }}
        >
          <Image
            src="/banners/daredevil.jpg"
            alt="Dare Devil Banner"
            fill
            style={{
              objectFit: "cover",
            }}
            className="absolute inset-0 z-0"
            priority
          />
          <div className="absolute inset-0 bg-gradient-to-r from-[#1c1c2c]/30 via-[#22223b]/40 to-[#22223b]/10 z-10"></div>
          <div className="relative z-20 flex flex-col justify-center h-full px-10 max-w-lg pb-7">
            <div className="flex items-center gap-2 mb-1">
              <span className="text-xs bg-red-500 dark:bg-red-600 text-white px-2 py-0.5 rounded">
                12 episode
              </span>
              <span className="text-xs text-gray-100">
                + 5 friends are watching
              </span>
              <span className="flex -space-x-2 ml-2">
                {avatars.slice(1, 4).map((src: string, i: number) => (
                  <Image
                    key={i}
                    src={src}
                    alt=""
                    width={26}
                    height={26}
                    className="w-6 h-6 rounded-full border-2 border-white"
                  />
                ))}
              </span>
            </div>
            <h2 className="text-4xl font-black text-white mb-1 drop-shadow">
              Dare Devil
            </h2>
            <div className="text-gray-200 mb-2 font-semibold drop-shadow">
              85% Watch · 3 season
            </div>
            <div className="flex gap-3">
              <div className="flex gap-2">
                <button className="bg-[#ff2e2e] hover:bg-red-600 text-white text-[13px] font-semibold px-4 py-[6px] rounded-full shadow-sm leading-none transition-colors">
                  watch
                </button>
                <button className="bg-white/15 hover:bg-white/25 w-9 h-9 rounded-full flex items-center justify-center transition-colors">
                  <Plus size={18} strokeWidth={2} className="text-white/90" />
                </button>
              </div>
            </div>
          </div>
        </section>

        {/* ---- Parties ---- */}
        <section className="mb-10" style={{ marginLeft: 30 }}>
          <h3 className="font-bold text-lg mb-3 text-gray-900 dark:text-white">Parties</h3>
          <div className="flex gap-7">
            {["Jack", "Jones", "Jimy", "Jack"].map((name: string, i: number) => (
              <div
                key={i}
                className="bg-white dark:bg-gray-800 rounded-xl shadow-md dark:shadow-gray-900/50 p-4 flex flex-col items-center w-28 border border-gray-100 dark:border-gray-700 transition-colors"
              >
                <Image
                  src={avatars[(i + 1) % avatars.length]}
                  className="w-12 h-12 rounded-full mb-2"
                  alt={name}
                  width={48}
                  height={48}
                />
                <div className="font-semibold text-gray-900 dark:text-white">{name}</div>
                <div className="text-xs text-gray-400 dark:text-gray-500 text-center">
                  {i === 2
                    ? "Its G.o.a.t Series"
                    : i === 1
                      ? "Its Good Series"
                      : "Its Great Series"}
                </div>
                <div className="flex -space-x-2 mt-2">
                  {[1, 2, 3].map((k: number) => (
                    <Image
                      key={k}
                      src={avatars[(i + k) % avatars.length]}
                      className="w-6 h-6 rounded-full border-2 border-white dark:border-gray-700"
                      alt=""
                      width={24}
                      height={24}
                    />
                  ))}
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* ---- Continue Watching ---- */}
        <section style={{ marginLeft: 30 }}>
          <h3 className="font-bold text-lg mb-3 text-gray-900 dark:text-white">Continue watching</h3>
          <div className="flex gap-7">
            {[
              { title: "8 Mile", img: "/banners/8mile.jpg" },
              { title: "Stranger Things", img: "/banners/stranger.jpg" },
              { title: "Batman", img: "/banners/batman.jpg" },
            ].map((movie: { title: string; img: string }, i: number) => (
              <div
                key={i}
                className="relative w-48 h-28 rounded-2xl overflow-hidden shadow-md dark:shadow-gray-900/50 hover:shadow-lg dark:hover:shadow-gray-900/70 transition-shadow"
              >
                <Image
                  src={movie.img}
                  className="w-full h-full object-cover"
                  alt={movie.title}
                  width={192}
                  height={112}
                />
                <div className="absolute left-3 top-2 bg-white/90 dark:bg-black/70 text-xs text-black dark:text-white px-2 py-0.5 rounded">
                  10 XP
                </div>
                <div className="absolute left-3 bottom-2 font-bold text-white drop-shadow">
                  {movie.title}
                </div>
              </div>
            ))}
          </div>
        </section>
      </main>
    </div>
  );
}