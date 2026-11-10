export {preloadReleaseEditPage} from './model/preload';

/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadReleaseEditPage = () => import('./ui/ReleaseEditPage').then((module) => module.ReleaseEditPage);
