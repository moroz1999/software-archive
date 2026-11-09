export {preloadGroupsPage, shouldPreloadGroupsPage} from './model/preload';

/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadGroupsPage = () => import('./ui/GroupsPage').then((module) => module.GroupsPage);
