export {preloadCategoryEditPage} from './model/preload';

/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadCategoryEditPage = () => import('./ui/CategoryEditPage').then((module) => module.CategoryEditPage);
