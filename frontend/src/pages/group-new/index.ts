export {preloadGroupNewPage} from './model/preload';

/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadGroupNewPage = () => import('./ui/GroupNewPage').then((module) => module.GroupNewPage);
