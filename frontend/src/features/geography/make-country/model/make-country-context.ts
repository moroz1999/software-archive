import type {GeographyCity} from '@/entities/locations';
import {createRequiredContext} from '@/shared/lib/context';

export interface MakeCountryState {
    readonly city: GeographyCity;
    /** Makes the city a country. */
    readonly make: () => void;
    readonly making: boolean;
    readonly made: boolean;
    /** Why the city could not become a country. */
    readonly error: string | undefined;
}

/** Making one city of no known country a country, for the controls inside a MakeCountryProvider. */
export const [MakeCountryContext, useMakeCountry] = createRequiredContext<MakeCountryState>('MakeCountry');
