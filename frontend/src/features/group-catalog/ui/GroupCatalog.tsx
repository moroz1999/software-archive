import {groupPath} from '@/entities/groups';
import {PlaceFilters} from '@/entities/locations';
import {LabelEntriesSkeleton, LabelEntry, LetterFilter} from '@/entities/labels';
import {Grid} from '@/shared/ui/grid';
import {LoadingState} from '@/shared/ui/loading-state';
import {Pagination} from '@/shared/ui/pagination';
import {Stack} from '@/shared/ui/stack';
import {Text} from '@/shared/ui/typography';
import {groupCatalogHref} from '../model/catalog-filter';
import {useGroupCatalog} from '../model/group-catalog-context';
import {useSkeleton} from '@/shared/lib/loading';

/** The group catalog with its letters, place filters and pages, inside a GroupCatalogProvider. */
export function GroupCatalog() {
    const {filter, countries, cities, changing, show} = useGroupCatalog();
    return (
        <Stack gap="lg">
            <LetterFilter current={filter.letter} hrefOf={(letter) => groupCatalogHref({...filter, letter, page: 0})} disabled={changing}/>
            <PlaceFilters
                country={filter.country}
                city={filter.city}
                countries={countries.data}
                cities={cities.data}
                disabled={changing}
                onChange={(country, city) => {
                    show({...filter, country, city, page: 0});
                }}
            />
            <Entries/>
        </Stack>
    );
}

function Entries() {
    const {t} = useTranslation();
    const {filter, entries, changing} = useGroupCatalog();
    const skeleton = useSkeleton(entries);

    if (skeleton || entries.isPending) {
        return <div data-testid="group-catalog-skeleton"><LabelEntriesSkeleton/></div>;
    }
    if (entries.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    if (entries.data.items.length === 0) {
        return <Text tone="muted">{t('empty.groups')}</Text>;
    }
    return (
        <LoadingState loading={changing}>
            <Stack gap="lg">
                <Grid as="ul" ariaLabel={t('pages.groups')} desktopColumns={4} tabletColumns={3} mobileColumns={1} gap="md">
                    {entries.data.items.map((entry) => (
                        <li key={`${entry.groupId}-${entry.name}`}>
                            <LabelEntry name={entry.name} href={groupPath(entry.groupId)} note={entry.groupName}/>
                        </li>
                    ))}
                </Grid>
                <Pagination
                    page={entries.data.number}
                    totalPages={entries.data.totalPages}
                    hrefOf={(page) => groupCatalogHref({...filter, page})}
                    disabled={changing}
                />
            </Stack>
        </LoadingState>
    );
}
import {useTranslation} from 'react-i18next';
