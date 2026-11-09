import {isRouteErrorResponse, useRouteError} from 'react-router';
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

    if (isNotFound(error)) {
        return (
            <PageLayout title="Not found">
                <Text tone="muted">This page does not exist or has been deleted.</Text>
                <Button href={HOME_PATH} color="secondary">Go to the home page</Button>
            </PageLayout>
        );
    }
    return (
        <PageLayout title="Something went wrong">
            <Text tone="muted">This page could not be shown. Please reload it or try again later.</Text>
        </PageLayout>
    );
}
