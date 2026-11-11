export {preloadCategoriesPage} from './model/preload';

/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadCategoriesPage = () => import('./ui/CategoriesPage').then((module) => module.CategoriesPage);
