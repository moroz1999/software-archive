export {preloadReleaseNewPage} from './model/preload';

/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadReleaseNewPage = () => import('./ui/ReleaseNewPage').then((module) => module.ReleaseNewPage);
