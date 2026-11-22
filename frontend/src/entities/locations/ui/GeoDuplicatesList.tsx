import {useTranslation} from 'react-i18next';
import {useSkeleton} from '@/shared/lib/loading';
import {Inline} from '@/shared/ui/inline';
import {Panel} from '@/shared/ui/panel';
import {Stack} from '@/shared/ui/stack';
import {Text, TextLink} from '@/shared/ui/typography';
import {cityEditPath, countryEditPath} from '../config/paths';
import {toCityId, toCountryId} from '../model/geo';
import type {GeoDuplicate} from '../model/geography';
import {useGeoDuplicates} from '../model/geography-context';
import {GeographyTreeSkeleton} from './GeographyTreeSkeleton';

/**
 * The countries and the cities from the GeoDuplicatesProvider around that may be one, by group; each leads to its
 * editing, where it is merged, and says how many places are in it.
 */
export function GeoDuplicatesList() {
    const {t} = useTranslation();
    return (
        <Panel as="section" title={t('geography.duplicates')} titleLevel={2}>
            <Groups/>
        </Panel>
    );
}

function Groups() {
    const {t} = useTranslation();
    const query = useGeoDuplicates();
    const skeleton = useSkeleton(query);

    if (skeleton || query.isPending) {
        return <GeographyTreeSkeleton/>;
    }
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    const {countries, cities} = query.data;
    if (countries.length === 0 && cities.length === 0) {
        return <Text tone="muted">{t('geography.noDuplicates')}</Text>;
    }
    return (
        <Stack gap="md" as="ul">
            {countries.map((group) => (
                <Group key={group.map((country) => country.id).join()} duplicates={group} href={(id) => countryEditPath(toCountryId(id))}/>
            ))}
            {cities.map((group) => (
                <Group
                    key={group.cities.map((city) => city.id).join()}
                    duplicates={group.cities}
                    href={(id) => cityEditPath(toCityId(id))}
                    city
                />
            ))}
        </Stack>
    );
}

function Group({duplicates, href, city = false}: {
    readonly duplicates: readonly GeoDuplicate[];
    readonly href: (id: string) => string;
    readonly city?: boolean;
}) {
    const {t} = useTranslation();
    return (
        <Inline as="li" gap="md" wrap>
            {duplicates.map((duplicate) => (
                <Inline key={duplicate.id} gap="xs" align="baseline">
                    <TextLink href={href(duplicate.id)}>
                        {city ? `${duplicate.name}, ${duplicate.country ?? t('geography.countryNotSpecified')}` : duplicate.name}
                    </TextLink>
                    <Text variant="bodySm" tone="muted">{t('geography.places', {count: duplicate.places})}</Text>
                </Inline>
            ))}
        </Inline>
    );
}
