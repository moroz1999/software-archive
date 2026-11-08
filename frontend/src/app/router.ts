import {createBrowserRouter, type RouteObject} from 'react-router';
import {SOFTWARE_EDIT_ROUTE, SOFTWARE_LIST_PATH, SOFTWARE_NEW_PATH} from '../entities/software';
import {preloadSoftwarePage, SoftwarePage} from '../pages/software';
import {preloadSoftwareEditPage, SoftwareEditPage} from '../pages/software-edit';
import {SoftwareNewPage} from '../pages/software-new';

/** Nothing is shown while the first route preloads: the preload window is short (docs/data-loading.md). */
const nothing = () => null;

/** Every route with server data preloads the calls of the blocks at the top of its view. */
const routes: RouteObject[] = [
    {path: SOFTWARE_LIST_PATH, loader: preloadSoftwarePage, Component: SoftwarePage, HydrateFallback: nothing},
    {path: SOFTWARE_NEW_PATH, Component: SoftwareNewPage, HydrateFallback: nothing},
    {path: SOFTWARE_EDIT_ROUTE, loader: preloadSoftwareEditPage, Component: SoftwareEditPage, HydrateFallback: nothing},
];

export type AppRouter = ReturnType<typeof createBrowserRouter>;

/** Call once per page load: every router preloads the first route and follows the browser history. */
export function createAppRouter(): AppRouter {
    return createBrowserRouter(routes);
}
