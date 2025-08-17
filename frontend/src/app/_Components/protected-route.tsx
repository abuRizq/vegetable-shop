
"use client";

import { useRouter } from "next/router";
import { useEffect } from "react";
import { useAuth } from "../hooks/useAuth";

interface Iprops {
    children: React.ReactNode;
    fallback?: React.ReactNode;
    redirectTo?: string;
}

export const ProtectedRoute: React.FC<Iprops> = ({
    children,
    fallback = <div>Checking authentication...</div>,
    redirectTo = '/login',
}) => {
    const { isAuthenticated, isLoading } = useAuth();
    const router = useRouter();

    useEffect(() => {
        if (!isLoading && !isAuthenticated) {
            router.push(redirectTo);
        }
    }, [isAuthenticated, isLoading, router, redirectTo]);

    if (isLoading) {
        return <>{fallback}</>;
    }

    if (!isAuthenticated) {
        return null; // Will redirect
    }

    return <>{children}</>;
};
