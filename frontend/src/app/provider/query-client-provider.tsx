'use client';

import { HydrationBoundary, QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { useState } from 'react';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function QueryProviders({ children, dehydratedState }: any) {
    // Create query client instance
    const [queryClient] = useState(
        () =>
            new QueryClient({
                defaultOptions: {
                    queries: {
                        staleTime: 5 * 60 * 1000,    // 5 minutes
                        gcTime: 10 * 60 * 1000,      // 10 minutes
                    },
                },
            })
        );      
    return (
        <QueryClientProvider client={queryClient}>
            {children}  
            <HydrationBoundary state={dehydratedState}></HydrationBoundary>
            <ReactQueryDevtools initialIsOpen={false} />
        </QueryClientProvider>
    );
}