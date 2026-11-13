/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadRegisterPage = () => import('./ui/RegisterPage').then((module) => module.RegisterPage);
