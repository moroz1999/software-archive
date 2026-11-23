export {preloadManagementHardwarePage} from './model/preload';

/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadManagementHardwarePage = () => import('./ui/ManagementHardwarePage').then((module) => module.ManagementHardwarePage);
