export {preloadHardwareNewPage} from './model/preload';

/** The page components, loaded on first use: never export them statically, or they land in the main bundle. */
export const loadHardwareGroupNewPage = () => import('./ui/HardwareNewPage').then((module) => module.HardwareGroupNewPage);
export const loadHardwareNewPage = () => import('./ui/HardwareNewPage').then((module) => module.HardwareNewPage);
