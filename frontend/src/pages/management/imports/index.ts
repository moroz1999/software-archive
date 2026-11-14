export {preloadManagementImportsPage} from './model/preload';

/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadManagementImportsPage = () => import('./ui/ManagementImportsPage').then((module) => module.ManagementImportsPage);
