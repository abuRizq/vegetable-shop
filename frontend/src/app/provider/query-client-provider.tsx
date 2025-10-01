'use client';

import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { useState } from 'react';

// Optional: Uncomment these imports if you want to enable persistence
// import { PersistQueryClientProvider } from '@tanstack/react-query-persist-client';
// import { createSyncStoragePersister } from '@tanstack/query-sync-storage-persister';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function QueryProviders({ children, dehydratedState }: any) {
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

    // Optional: Create persister for localStorage persistence
    // Uncomment this if you installed the persistence packages:
    // npm install @tanstack/react-query-persist-client @tanstack/query-sync-storage-persister
    //
    // const [persister] = useState(() =>
    //     createSyncStoragePersister({
    //         storage: typeof window !== 'undefined' ? window.localStorage : undefined,
    //     })
    // );

    // If using persistence, replace QueryClientProvider with PersistQueryClientProvider:
    // return (
    //     <PersistQueryClientProvider
    //         client={queryClient}
    //         persistOptions={{ persister }}
    //     >
    //         {children}
    //         <ReactQueryDevtools initialIsOpen={false} />
    //     </PersistQueryClientProvider>
    // );

    // Standard provider (without persistence)
    return (
        <QueryClientProvider client={queryClient}>
            {children}
            <ReactQueryDevtools initialIsOpen={false} />
        </QueryClientProvider>
    );
}