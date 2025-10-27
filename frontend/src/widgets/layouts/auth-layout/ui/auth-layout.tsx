import ReturntoHome from "@/shared/ui/return-home";

function AuthLayout({
    children,
}: Readonly<{
    children: React.ReactNode;
}>) {
    return (
        <div className="flex w-full min-h-screen bg-gray-50 dark:bg-gray-900 content-transition">
            <ReturntoHome />
            {children}
        </div>
    );
}

export {
    AuthLayout
}