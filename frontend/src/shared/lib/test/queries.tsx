import type {ReactElement} from 'react';
import {render} from '@testing-library/react';
import {type QueryClient, QueryClientProvider} from '@tanstack/react-query';
import {createQueryClient} from '@/shared/api';

/** Renders with a query client of its own, so no test sees the cache of another. */
export function renderWithQueries(ui: ReactElement, queryClient: QueryClient = createQueryClient()): {queryClient: QueryClient} {
    render(<QueryClientProvider client={queryClient}>{ui}</QueryClientProvider>);
    return {queryClient};
}
