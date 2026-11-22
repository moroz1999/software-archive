import {useId} from 'react';
import {useTranslation} from 'react-i18next';
import {type GeographyCity, useCountries} from '@/entities/locations';
import {Button} from '@/shared/ui/button';
import {Inline} from '@/shared/ui/inline';
import {Select} from '@/shared/ui/select';
import {Text} from '@/shared/ui/typography';
import {usePutInCountry} from '../model/put-in-country-context';
import {PutInCountryProvider} from '../model/PutInCountryProvider';

/** Puts a city of no known country in a country chosen from the CountriesProvider around. */
export function PutCityInCountry({city}: {readonly city: GeographyCity}) {
    return (
        <PutInCountryProvider city={city}>
            <CountryChoice/>
        </PutInCountryProvider>
    );
}

function CountryChoice() {
    const {t} = useTranslation();
    const id = useId();
    const countries = useCountries();
    const {city, country, choose, put, putting, error} = usePutInCountry();

    return (
        <Inline gap="sm" align="center" wrap>
            <Select
                id={id}
                value={country}
                onChange={choose}
                options={(countries.data ?? []).map((option) => ({value: option.id, label: option.name}))}
                placeholder={t('geography.chooseCountry')}
                ariaLabel={t('geography.countryFor', {name: city.name})}
                invalid={error !== undefined}
                disabled={putting || countries.data === undefined}
            />
            <Button size="sm" onClick={put} loading={putting} disabled={country === ''} ariaLabel={t('geography.putIn', {name: city.name})}>
                {t('geography.putInCountry')}
            </Button>
            {error !== undefined && <Text variant="bodySm" tone="danger" role="alert">{error}</Text>}
        </Inline>
    );
}
