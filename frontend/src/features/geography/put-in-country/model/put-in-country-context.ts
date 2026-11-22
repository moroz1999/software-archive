import type {GeographyCity} from '@/entities/locations';
import {createRequiredContext} from '@/shared/lib/context';

export interface PutInCountryState {
    readonly city: GeographyCity;
    /** The id of the country chosen, empty while none is. */
    readonly country: string;
    readonly choose: (country: string) => void;
    /** Puts the city in the country chosen. */
    readonly put: () => void;
    readonly putting: boolean;
    /** Why the city could not be put in the country. */
    readonly error: string | undefined;
}

/** Putting one city in a country, for the controls inside a PutInCountryProvider. */
export const [PutInCountryContext, usePutInCountry] = createRequiredContext<PutInCountryState>('PutInCountry');
