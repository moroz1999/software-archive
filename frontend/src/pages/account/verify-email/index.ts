/** The page component, loaded on first use: never export it statically, or it lands in the main bundle. */
export const loadVerifyEmailPage = () => import('./ui/VerifyEmailPage').then((module) => module.VerifyEmailPage);
