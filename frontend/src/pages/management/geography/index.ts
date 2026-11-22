export {preloadManagementGeographyPage} from './model/preload';

/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadManagementGeographyPage = () => import('./ui/ManagementGeographyPage').then((module) => module.ManagementGeographyPage);
