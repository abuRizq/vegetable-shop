import ReturnHome from "@/shared/ui/return-home";


export default function RootLayout({
    children,
}: Readonly<{
    children: React.ReactNode;
}>) {
    return (
        <div className="flex w-full min-h-screen bg-gray-50 dark:bg-gray-900 content-transition">
            <ReturnHome />
            {children}
        </div>
    );
}
