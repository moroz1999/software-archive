export {preloadAuthorNewPage} from './model/preload';

/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadAuthorNewPage = () => import('./ui/AuthorNewPage').then((module) => module.AuthorNewPage);
