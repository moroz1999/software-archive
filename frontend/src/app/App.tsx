import {type QueryClient, QueryClientProvider} from '@tanstack/react-query';
import {RouterProvider} from 'react-router';
import {PlatformSelectionProvider} from '@/entities/platforms';
import {SystemErrorDialog} from '@/widgets/system-error-dialog';
import type {AppRouter} from './router';
import {I18nProvider} from './i18n/I18nProvider';

export interface AppProps {
    /**
     * Created once, outside React: a router starts the first route's preload as soon as it exists and
     * listens to the browser history, so a second one (StrictMode renders twice) would load everything twice.
     */
    readonly router: AppRouter;
    /** Created once with the router, whose loaders fill it. */
    readonly queryClient: QueryClient;
}

export function App({router, queryClient}: AppProps) {
    return (
        <I18nProvider>
            <QueryClientProvider client={queryClient}>
                <PlatformSelectionProvider>
                    <RouterProvider router={router}/>
                </PlatformSelectionProvider>
                <SystemErrorDialog/>
            </QueryClientProvider>
        </I18nProvider>
    );
}
