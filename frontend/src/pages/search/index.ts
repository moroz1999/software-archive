export {preloadSearchPage, shouldPreloadSearchPage} from './model/preload';

/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadSearchPage = () => import('./ui/SearchPage').then((module) => module.SearchPage);
