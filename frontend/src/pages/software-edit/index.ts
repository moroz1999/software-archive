export {preloadSoftwareEditPage} from './model/preload';

/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadSoftwareEditPage = () => import('./ui/SoftwareEditPage').then((module) => module.SoftwareEditPage);
