/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadSoftwareNewPage = () => import('./ui/SoftwareNewPage').then((module) => module.SoftwareNewPage);
