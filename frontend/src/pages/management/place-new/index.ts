export {preloadCityNewPage} from './model/preload';

/** The page components, loaded on first use: never export them statically, or they land in the main bundle. */
export const loadCountryNewPage = () => import('./ui/PlaceNewPage').then((module) => module.CountryNewPage);
export const loadCityNewPage = () => import('./ui/PlaceNewPage').then((module) => module.CityNewPage);
