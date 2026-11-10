import {isRouteErrorResponse, useRouteError} from 'react-router';
import {useTranslation} from 'react-i18next';
import {NotFoundError} from '@/shared/api';
import {HOME_PATH} from '@/shared/config';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';
import {Text} from '@/shared/ui/typography';

function isNotFound(error: unknown): boolean {
    return error instanceof NotFoundError || (isRouteErrorResponse(error) && error.status === 404);
}

/**
 * The error boundary of every route: a missing entity, an unknown address and a malformed id in the URL
 * show the not found page; anything else that breaks a page shows a page of its own (docs/error-handling.md).
 */
export function RouteErrorBoundary() {
    const error = useRouteError();
    const {t} = useTranslation();

    if (isNotFound(error)) {
        return (
            <PageLayout title={t('errors.notFound.title')}>
                <Text tone="muted">{t('errors.notFound.message')}</Text>
                <Button href={HOME_PATH} color="secondary">{t('errors.notFound.home')}</Button>
            </PageLayout>
        );
    }
    return (
        <PageLayout title={t('errors.system.title')}>
            <Text tone="muted">{t('errors.page.message')}</Text>
        </PageLayout>
    );
}
