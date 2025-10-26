'use client';

import { QueryClient, QueryClientProvider, HydrationBoundary, type DehydratedState } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { useState, type ReactNode } from 'react';

interface QueryProvidersProps {
  children: ReactNode;
  dehydratedState?: DehydratedState;
}

export function QueryProviders({ children, dehydratedState }: QueryProvidersProps) {
    // Create query client instance with improved defaults
    const [queryClient] = useState(
        () =>
            new QueryClient({
                defaultOptions: {
                    queries: {
                        staleTime: 5 * 60 * 1000,    // 5 minutes
                        gcTime: 10 * 60 * 1000,      // 10 minutes (formerly cacheTime)
                        refetchOnWindowFocus: true,   // Refetch when window regains focus
                        refetchOnReconnect: true,     // Refetch when reconnecting
                        retry: 1,                     // Retry failed requests once
                    },
                    mutations: {
                        retry: 0,                     // Don't retry mutations by default
                    },
                },
            })
        );

    return (
        <QueryClientProvider client={queryClient}>
            <HydrationBoundary state={dehydratedState}>
                {children}
            </HydrationBoundary>
            <ReactQueryDevtools initialIsOpen={true} />
        </QueryClientProvider>
    );
}