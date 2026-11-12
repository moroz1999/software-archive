import {authorPath} from '@/entities/authors';
import {PlaceFilters} from '@/entities/locations';
import {LabelEntriesSkeleton, LabelEntry, LetterFilter} from '@/entities/labels';
import {Grid} from '@/shared/ui/grid';
import {LoadingState} from '@/shared/ui/loading-state';
import {Pagination} from '@/shared/ui/pagination';
import {Stack} from '@/shared/ui/stack';
import {Text} from '@/shared/ui/typography';
import {useAuthorCatalog} from '../model/author-catalog-context';
import type {AuthorCatalogEntry} from '../model/catalog';
import {catalogHref} from '../model/catalog-filter';
import {useSkeleton} from '@/shared/lib/loading';

/** The author catalog with its letters, place filters and pages, inside an AuthorCatalogProvider. */
export function AuthorCatalog() {
    const {filter, countries, cities, changing, show} = useAuthorCatalog();
    return (
        <Stack gap="lg">
            <LetterFilter current={filter.letter} hrefOf={(letter) => catalogHref({...filter, letter, page: 0})} disabled={changing}/>
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

/** Next to an entry: the real name of the person, or for a label of a person without one, the main name. */
function noteOf(entry: AuthorCatalogEntry, labelOf: (name: string) => string): string | null {
    if (entry.realName !== null) {
        return entry.realName;
    }
    return entry.labelOf === null ? null : labelOf(entry.labelOf);
}

function Entries() {
    const {t} = useTranslation();
    const {filter, entries, changing} = useAuthorCatalog();
    const skeleton = useSkeleton(entries);

    if (skeleton || entries.isPending) {
        return <div data-testid="author-catalog-skeleton"><LabelEntriesSkeleton/></div>;
    }
    if (entries.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    if (entries.data.items.length === 0) {
        return <Text tone="muted">{t('empty.authors')}</Text>;
    }
    return (
        <LoadingState loading={changing}>
            <Stack gap="lg">
                <Grid as="ul" ariaLabel={t('pages.authors')} desktopColumns={4} tabletColumns={3} mobileColumns={1} gap="md">
                    {entries.data.items.map((entry) => (
                        <li key={`${entry.personId}-${entry.name}`}>
                            <LabelEntry name={entry.name} href={authorPath(entry.personId)} note={noteOf(entry, (name) => t('author.labelOf', {name}))}/>
                        </li>
                    ))}
                </Grid>
                <Pagination
                    page={entries.data.number}
                    totalPages={entries.data.totalPages}
                    hrefOf={(page) => catalogHref({...filter, page})}
                    disabled={changing}
                />
            </Stack>
        </LoadingState>
    );
}
import {useTranslation} from 'react-i18next';
