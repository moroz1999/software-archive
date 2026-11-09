import type {QueryClient} from '@tanstack/react-query';
import {createBrowserRouter, type RouteObject} from 'react-router';
import {SOFTWARE_EDIT_ROUTE, SOFTWARE_LIST_PATH, SOFTWARE_NEW_PATH, SOFTWARE_ROUTE} from '@/entities/software';
import {loadSoftwarePage, preloadSoftwarePage} from '@/pages/software';
import {loadSoftwareEditPage, preloadSoftwareEditPage} from '@/pages/software-edit';
import {loadSoftwareNewPage} from '@/pages/software-new';
import {loadSoftwareViewPage, preloadSoftwareViewPage} from '@/pages/software-view';
import {RouteErrorBoundary} from '@/widgets/route-error';

/** Nothing is shown while the first route preloads: the preload window is short (docs/data-loading.md). */
const nothing = () => null;

/**
 * Every page is its own chunk, loaded lazily. The loader stays in the main bundle, so the data calls of a
 * route start together with the download of its page, not after it (docs/data-loading.md).
 * Built anew for every router: React Router remembers a resolved lazy page on the route object itself.
 * The pages share one error boundary: it also answers addresses the app does not know.
 */
const routes = (queryClient: QueryClient): RouteObject[] => [{ErrorBoundary: RouteErrorBoundary, HydrateFallback: nothing, children: [
    {path: SOFTWARE_LIST_PATH, loader: preloadSoftwarePage(queryClient), lazy: {Component: loadSoftwarePage}, HydrateFallback: nothing},
    {path: SOFTWARE_NEW_PATH, lazy: {Component: loadSoftwareNewPage}, HydrateFallback: nothing},
    {path: SOFTWARE_ROUTE, loader: preloadSoftwareViewPage(queryClient), lazy: {Component: loadSoftwareViewPage}, HydrateFallback: nothing},
    {path: SOFTWARE_EDIT_ROUTE, loader: preloadSoftwareEditPage(queryClient), lazy: {Component: loadSoftwareEditPage}, HydrateFallback: nothing},
]}];

export type AppRouter = ReturnType<typeof createBrowserRouter>;

/**
 * Call once per page load: every router preloads the first route and follows the browser history.
 * Its loaders fill the query client that the app's blocks read.
 */
export function createAppRouter(queryClient: QueryClient): AppRouter {
    return createBrowserRouter(routes(queryClient));
}
