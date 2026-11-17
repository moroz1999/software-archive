export {preloadLabelEditPage} from './model/preload';

/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadLabelEditPage = () => import('./ui/LabelEditPage').then((module) => module.LabelEditPage);
