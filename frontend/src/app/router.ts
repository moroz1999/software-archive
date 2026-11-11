import type {QueryClient} from '@tanstack/react-query';
import {createBrowserRouter, type RouteObject} from 'react-router';
import {AUTHOR_EDIT_ROUTE, AUTHOR_NEW_PATH, AUTHOR_ROUTE, AUTHORS_PATH} from '@/entities/authors';
import {CATEGORIES_PATH, CATEGORY_EDIT_ROUTE, CATEGORY_NEW_PATH} from '@/entities/categories';
import {GROUP_EDIT_ROUTE, GROUP_NEW_PATH, GROUP_ROUTE, GROUPS_PATH} from '@/entities/groups';
import {RELEASE_EDIT_ROUTE, RELEASE_NEW_ROUTE, SOFTWARE_EDIT_ROUTE, SOFTWARE_LIST_PATH, SOFTWARE_NEW_PATH, SOFTWARE_ROUTE} from '@/entities/software';
import {loadAuthorEditPage, preloadAuthorEditPage} from '@/pages/author-edit';
import {loadAuthorNewPage, preloadAuthorNewPage} from '@/pages/author-new';
import {loadAuthorViewPage, preloadAuthorViewPage} from '@/pages/author-view';
import {loadAuthorsPage, preloadAuthorsPage, shouldPreloadAuthorsPage} from '@/pages/authors';
import {loadCategoriesPage, preloadCategoriesPage} from '@/pages/categories';
import {loadCategoryEditPage, preloadCategoryEditPage} from '@/pages/category-edit';
import {loadCategoryNewPage, preloadCategoryNewPage} from '@/pages/category-new';
import {loadGroupEditPage, preloadGroupEditPage} from '@/pages/group-edit';
import {loadGroupNewPage, preloadGroupNewPage} from '@/pages/group-new';
import {loadGroupViewPage, preloadGroupViewPage} from '@/pages/group-view';
import {loadGroupsPage, preloadGroupsPage, shouldPreloadGroupsPage} from '@/pages/groups';
import {loadHomePage, preloadHomePage} from '@/pages/home';
import {loadReleaseEditPage, preloadReleaseEditPage} from '@/pages/release-edit';
import {loadReleaseNewPage, preloadReleaseNewPage} from '@/pages/release-new';
import {loadSoftwarePage, preloadSoftwarePage, shouldPreloadSoftwarePage} from '@/pages/software';
import {SOFTWARE_CATEGORY_ROUTE} from '@/features/software-catalog';
import {loadSoftwareEditPage, preloadSoftwareEditPage} from '@/pages/software-edit';
import {loadSoftwareNewPage} from '@/pages/software-new';
import {loadSoftwareViewPage, preloadSoftwareViewPage} from '@/pages/software-view';
import {NotFoundError} from '@/shared/api';
import {HOME_PATH} from '@/shared/config';
import {RouteErrorBoundary} from '@/widgets/route-error';
import {AppLayout} from './AppLayout';

/** Nothing is shown while the first route preloads: the preload window is short (docs/data-loading.md). */
const nothing = () => null;

/** An address the app does not know: the not found page, under the main menu like every page. */
const unknownAddress = () => {
    throw new NotFoundError('page.not-found');
};

/**
 * Every page is its own chunk, loaded lazily. The loader stays in the main bundle, so the data calls of a
 * route start together with the download of its page, not after it (docs/data-loading.md).
 * Built anew for every router: React Router remembers a resolved lazy page on the route object itself.
 * The pages share one error boundary inside the frame with the main menu; an unknown address reaches it too.
 */
const routes = (queryClient: QueryClient): RouteObject[] => [{Component: AppLayout, HydrateFallback: nothing, children: [
    {ErrorBoundary: RouteErrorBoundary, HydrateFallback: nothing, children: [
        {path: HOME_PATH, loader: preloadHomePage(queryClient), lazy: {Component: loadHomePage}, HydrateFallback: nothing},
        ...[SOFTWARE_LIST_PATH, SOFTWARE_CATEGORY_ROUTE].map((path) => ({
            path,
            loader: preloadSoftwarePage(queryClient),
            shouldRevalidate: shouldPreloadSoftwarePage,
            lazy: {Component: loadSoftwarePage},
            HydrateFallback: nothing,
        })),
        {path: SOFTWARE_NEW_PATH, lazy: {Component: loadSoftwareNewPage}, HydrateFallback: nothing},
        {path: SOFTWARE_ROUTE, loader: preloadSoftwareViewPage(queryClient), lazy: {Component: loadSoftwareViewPage}, HydrateFallback: nothing},
        {path: SOFTWARE_EDIT_ROUTE, loader: preloadSoftwareEditPage(queryClient), lazy: {Component: loadSoftwareEditPage}, HydrateFallback: nothing},
        {path: RELEASE_NEW_ROUTE, loader: preloadReleaseNewPage(queryClient), lazy: {Component: loadReleaseNewPage}, HydrateFallback: nothing},
        {path: RELEASE_EDIT_ROUTE, loader: preloadReleaseEditPage(queryClient), lazy: {Component: loadReleaseEditPage}, HydrateFallback: nothing},
        {
            path: AUTHORS_PATH,
            loader: preloadAuthorsPage(queryClient),
            shouldRevalidate: shouldPreloadAuthorsPage,
            lazy: {Component: loadAuthorsPage},
            HydrateFallback: nothing,
        },
        {path: AUTHOR_NEW_PATH, loader: preloadAuthorNewPage(queryClient), lazy: {Component: loadAuthorNewPage}, HydrateFallback: nothing},
        {path: AUTHOR_ROUTE, loader: preloadAuthorViewPage(queryClient), lazy: {Component: loadAuthorViewPage}, HydrateFallback: nothing},
        {path: AUTHOR_EDIT_ROUTE, loader: preloadAuthorEditPage(queryClient), lazy: {Component: loadAuthorEditPage}, HydrateFallback: nothing},
        {
            path: GROUPS_PATH,
            loader: preloadGroupsPage(queryClient),
            shouldRevalidate: shouldPreloadGroupsPage,
            lazy: {Component: loadGroupsPage},
            HydrateFallback: nothing,
        },
        {path: GROUP_NEW_PATH, loader: preloadGroupNewPage(queryClient), lazy: {Component: loadGroupNewPage}, HydrateFallback: nothing},
        {path: GROUP_ROUTE, loader: preloadGroupViewPage(queryClient), lazy: {Component: loadGroupViewPage}, HydrateFallback: nothing},
        {path: GROUP_EDIT_ROUTE, loader: preloadGroupEditPage(queryClient), lazy: {Component: loadGroupEditPage}, HydrateFallback: nothing},
        {path: CATEGORIES_PATH, loader: preloadCategoriesPage(queryClient), lazy: {Component: loadCategoriesPage}, HydrateFallback: nothing},
        {path: CATEGORY_NEW_PATH, loader: preloadCategoryNewPage(queryClient), lazy: {Component: loadCategoryNewPage}, HydrateFallback: nothing},
        {path: CATEGORY_EDIT_ROUTE, loader: preloadCategoryEditPage(queryClient), lazy: {Component: loadCategoryEditPage}, HydrateFallback: nothing},
        {path: '*', loader: unknownAddress, Component: nothing},
    ]},
]}];

export type AppRouter = ReturnType<typeof createBrowserRouter>;

/**
 * Call once per page load: every router preloads the first route and follows the browser history.
 * Its loaders fill the query client that the app's blocks read.
 */
export function createAppRouter(queryClient: QueryClient): AppRouter {
    return createBrowserRouter(routes(queryClient));
}
