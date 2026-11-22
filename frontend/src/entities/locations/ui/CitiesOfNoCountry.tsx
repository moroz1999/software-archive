import type {ReactNode} from 'react';
import {useTranslation} from 'react-i18next';
import {useSkeleton} from '@/shared/lib/loading';
import {Inline} from '@/shared/ui/inline';
import {Panel} from '@/shared/ui/panel';
import {Stack} from '@/shared/ui/stack';
import {Text, TextLink} from '@/shared/ui/typography';
import {cityEditPath} from '../config/paths';
import type {GeographyCity} from '../model/geography';
import {useCitiesOfNoCountry} from '../model/geography-context';
import {GeographyTreeSkeleton} from './GeographyTreeSkeleton';

export interface CitiesOfNoCountryProps {
    /** What can be done with a city, after its name: putting it in a country. */
    readonly action: (city: GeographyCity) => ReactNode;
}

/**
 * The cities of no known country from the CitiesOfNoCountryProvider around, each leading to its editing and saying how
 * many places are in it.
 */
export function CitiesOfNoCountry({action}: CitiesOfNoCountryProps) {
    const {t} = useTranslation();
    return (
        <Panel as="section" title={t('geography.citiesOfNoCountry')} titleLevel={2}>
            <Cities action={action}/>
        </Panel>
    );
}

function Cities({action}: CitiesOfNoCountryProps) {
    const {t} = useTranslation();
    const query = useCitiesOfNoCountry();
    const skeleton = useSkeleton(query);

    if (skeleton || query.isPending) {
        return <GeographyTreeSkeleton/>;
    }
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    if (query.data.length === 0) {
        return <Text tone="muted">{t('geography.everyCityInCountry')}</Text>;
    }
    return (
        <Stack gap="sm" as="ul">
            {query.data.map((city) => (
                <Inline key={city.id} as="li" gap="md" align="center" wrap>
                    <TextLink href={cityEditPath(city.id)}>{city.name}</TextLink>
                    <Text variant="bodySm" tone="muted">{t('geography.places', {count: city.places})}</Text>
                    {action(city)}
                </Inline>
            ))}
        </Stack>
    );
}
