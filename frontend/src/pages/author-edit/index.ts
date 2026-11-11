export {preloadAuthorEditPage} from './model/preload';

/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadAuthorEditPage = () => import('./ui/AuthorEditPage').then((module) => module.AuthorEditPage);
