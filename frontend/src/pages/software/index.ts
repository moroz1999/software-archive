export {preloadSoftwarePage} from './model/preload';

/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadSoftwarePage = () => import('./ui/SoftwarePage').then((module) => module.SoftwarePage);
