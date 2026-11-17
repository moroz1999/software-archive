export {preloadLabelNewPage} from './model/preload';

/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadLabelNewPage = () => import('./ui/LabelNewPage').then((module) => module.LabelNewPage);
