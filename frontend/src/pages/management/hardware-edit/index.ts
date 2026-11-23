export {preloadHardwareEditPage, preloadHardwareGroupEditPage} from './model/preload';

/** The page components, loaded on first use: never export them statically, or they land in the main bundle. */
export const loadHardwareGroupEditPage = () => import('./ui/HardwareEditPage').then((module) => module.HardwareGroupEditPage);
export const loadHardwareEditPage = () => import('./ui/HardwareEditPage').then((module) => module.HardwareEditPage);
