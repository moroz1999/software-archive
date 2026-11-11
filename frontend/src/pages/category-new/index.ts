export {preloadCategoryNewPage} from './model/preload';

/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadCategoryNewPage = () => import('./ui/CategoryNewPage').then((module) => module.CategoryNewPage);
