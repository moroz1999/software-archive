export {preloadHomePage} from './model/preload';

/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadHomePage = () => import('./ui/HomePage').then((module) => module.HomePage);
