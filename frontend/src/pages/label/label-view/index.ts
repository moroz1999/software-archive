export {preloadLabelViewPage} from './model/preload';

/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadLabelViewPage = () => import('./ui/LabelViewPage').then((module) => module.LabelViewPage);
