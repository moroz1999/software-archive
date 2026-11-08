import {RouterProvider} from 'react-router';
import {SystemErrorDialog} from '../widgets/system-error-dialog';
import type {AppRouter} from './router';

export interface AppProps {
    /**
     * Created once, outside React: a router starts the first route's preload as soon as it exists and
     * listens to the browser history, so a second one (StrictMode renders twice) would load everything twice.
     */
    readonly router: AppRouter;
}

export function App({router}: AppProps) {
    return (
        <>
            <RouterProvider router={router}/>
            <SystemErrorDialog/>
        </>
    );
}
