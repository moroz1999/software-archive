export {preloadAuthorViewPage} from './model/preload';

/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadAuthorViewPage = () => import('./ui/AuthorViewPage').then((module) => module.AuthorViewPage);
