import {useTranslation} from 'react-i18next';
import {useSkeleton} from '@/shared/lib/loading';
import {Inline} from '@/shared/ui/inline';
import {Tree, type TreeItem} from '@/shared/ui/tree';
import {Text, TextLink} from '@/shared/ui/typography';
import {cityEditPath, countryEditPath} from '../config/paths';
import type {GeographyCountry} from '../model/geography';
import {useGeography} from '../model/geography-context';
import {GeographyTreeSkeleton} from './GeographyTreeSkeleton';

/**
 * Every country with its cities from the GeographyProvider around, folded to the countries; each leads to its editing
 * and says how many places of people, groups and labels are in it.
 */
export function GeographyTree() {
    const {t} = useTranslation();
    const query = useGeography();
    const skeleton = useSkeleton(query);

    if (skeleton || query.isPending) {
        return <div data-testid="geography-tree-skeleton"><GeographyTreeSkeleton/></div>;
    }
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    if (query.data.length === 0) {
        return <Text tone="muted">{t('geography.empty')}</Text>;
    }
    const entry = (name: string, href: string, places: number) => (
        <Inline gap="sm" align="baseline">
            <TextLink href={href}>{name}</TextLink>
            <Text variant="bodySm" tone="muted">{t('geography.places', {count: places})}</Text>
        </Inline>
    );
    const toItem = (country: GeographyCountry): TreeItem => ({
        id: country.id,
        name: country.name,
        content: entry(country.name, countryEditPath(country.id), country.places),
        children: country.cities.map((city) => ({
            id: city.id,
            name: city.name,
            content: entry(city.name, cityEditPath(city.id), city.places),
            children: [],
        })),
    });
    return (
        <Tree
            items={query.data.map(toItem)}
            ariaLabel={t('pages.geography')}
            expandLabel={(name) => t('geography.expand', {name})}
            collapseLabel={(name) => t('geography.collapse', {name})}
        />
    );
}
