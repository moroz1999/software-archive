export {preloadManagementUsersPage, shouldPreloadManagementUsersPage} from './model/preload';

/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadManagementUsersPage = () => import('./ui/ManagementUsersPage').then((module) => module.ManagementUsersPage);
