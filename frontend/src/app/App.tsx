import {type QueryClient, QueryClientProvider} from '@tanstack/react-query';
import {useEffect, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {RouterProvider} from 'react-router';
import {PlatformSelectionProvider} from '@/entities/platforms';
import {CurrentUserProvider} from '@/entities/users';
import {subscribeSessionExpiry} from '@/shared/api';
import {Button} from '@/shared/ui/button';
import {Dialog} from '@/shared/ui/dialog';
import {Text} from '@/shared/ui/typography';
import {SystemErrorDialog} from '@/widgets/system-error-dialog';
import type {AppRouter} from './router';
import {ContentLanguageSync} from './i18n/ContentLanguageSync';
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
                <ContentLanguageSync/>
                <CurrentUserProvider>
                    <PlatformSelectionProvider>
                        <RouterProvider router={router}/>
                    </PlatformSelectionProvider>
                </CurrentUserProvider>
                <SystemErrorDialog/>
                <SessionExpiredDialog/>
            </QueryClientProvider>
        </I18nProvider>
    );
}

function SessionExpiredDialog() {
    const {t} = useTranslation();
    const [open, setOpen] = useState(false);

    useEffect(() => subscribeSessionExpiry(() => {
        setOpen(true);
    }), []);

    const close = () => {
        setOpen(false);
    };

    return (
        <Dialog open={open} title={t('account.sessionExpired')} onClose={close} role="alertdialog"
            actions={<Button onClick={close}>{t('actions.ok')}</Button>}
        >
            <Text>{t('account.sessionExpiredMessage')}</Text>
        </Dialog>
    );
}
