export {preloadAuthorsPage, shouldPreloadAuthorsPage} from './model/preload';

/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadAuthorsPage = () => import('./ui/AuthorsPage').then((module) => module.AuthorsPage);
