export {preloadSoftwareViewPage} from './model/preload';

/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadSoftwareViewPage = () => import('./ui/SoftwareViewPage').then((module) => module.SoftwareViewPage);
