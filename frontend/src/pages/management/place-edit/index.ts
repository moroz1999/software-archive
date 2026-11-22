export {preloadCityEditPage, preloadCountryEditPage} from './model/preload';

/** The page components, loaded on first use: never export them statically, or they land in the main bundle. */
export const loadCountryEditPage = () => import('./ui/PlaceEditPage').then((module) => module.CountryEditPage);
export const loadCityEditPage = () => import('./ui/PlaceEditPage').then((module) => module.CityEditPage);
