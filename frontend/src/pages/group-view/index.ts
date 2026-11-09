export {preloadGroupViewPage} from './model/preload';

/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadGroupViewPage = () => import('./ui/GroupViewPage').then((module) => module.GroupViewPage);
