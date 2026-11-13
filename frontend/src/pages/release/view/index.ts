export {preloadReleaseViewPage} from './model/preload';

/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadReleaseViewPage = () => import('./ui/ReleaseViewPage').then((module) => module.ReleaseViewPage);
