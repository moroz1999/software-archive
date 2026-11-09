import {authorPath} from '@/entities/authors';
import {Field} from '@/shared/ui/field';
import {Grid} from '@/shared/ui/grid';
import {Inline} from '@/shared/ui/inline';
import {LoadingState} from '@/shared/ui/loading-state';
import {NavChip, NavChips} from '@/shared/ui/nav-chips';
import {Pagination} from '@/shared/ui/pagination';
import {Select} from '@/shared/ui/select';
import {Stack} from '@/shared/ui/stack';
import {Text, TextLink} from '@/shared/ui/typography';
import {useAuthorCatalog} from '../model/author-catalog-context';
import {AUTHOR_LETTERS, catalogHref, toCityId, toCountryId} from '../model/catalog-filter';
import {AuthorCatalogSkeleton} from './AuthorCatalogSkeleton';
import styles from './AuthorCatalog.module.css';

/** The author catalog with its letters, place filters and pages, inside an AuthorCatalogProvider. */
export function AuthorCatalog() {
    return (
        <Stack gap="lg">
            <Letters/>
            <PlaceFilters/>
            <Entries/>
        </Stack>
    );
}

function Letters() {
    const {filter, changing} = useAuthorCatalog();
    return (
        <NavChips label="First letter">
            <NavChip href={catalogHref({...filter, letter: null, page: 0})} active={filter.letter === null} disabled={changing}>All</NavChip>
            {AUTHOR_LETTERS.map((letter) => (
                <NavChip key={letter} href={catalogHref({...filter, letter, page: 0})} active={filter.letter === letter} disabled={changing}>
                    {letter}
                </NavChip>
            ))}
        </NavChips>
    );
}

function PlaceFilters() {
    const {filter, countries, cities, changing, show} = useAuthorCatalog();
    const countryOptions = (countries.data ?? []).map((country) => ({value: country.id, label: country.name}));
    const cityOptions = (cities.data ?? []).map((city) => ({value: city.id, label: city.name}));
    return (
        <Inline gap="md" wrap>
            <Field label="Country" className={styles.filter}>
                {(control) => (
                    <Select
                        id={control.id}
                        value={filter.country ?? ''}
                        onChange={(value) => {
                            show({...filter, country: value === '' ? null : toCountryId(value), city: null, page: 0});
                        }}
                        options={countryOptions}
                        placeholder="Any country"
                        disabled={changing || countries.data === undefined}
                    />
                )}
            </Field>
            <Field label="City" className={styles.filter}>
                {(control) => (
                    <Select
                        id={control.id}
                        value={filter.city ?? ''}
                        onChange={(value) => {
                            show({...filter, city: value === '' ? null : toCityId(value), page: 0});
                        }}
                        options={cityOptions}
                        placeholder="Any city"
                        disabled={changing || cities.data === undefined}
                    />
                )}
            </Field>
        </Inline>
    );
}

function Entries() {
    const {filter, entries, changing} = useAuthorCatalog();

    if (entries.isPending) {
        return <div data-testid="author-catalog-skeleton"><AuthorCatalogSkeleton/></div>;
    }
    if (entries.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    if (entries.data.items.length === 0) {
        return <Text tone="muted">No authors found.</Text>;
    }
    return (
        <LoadingState loading={changing}>
            <Stack gap="lg">
                <Grid as="ul" ariaLabel="Authors" desktopColumns={4} tabletColumns={3} mobileColumns={1} gap="md">
                    {entries.data.items.map((entry) => (
                        <li key={`${entry.personId}-${entry.name}`}>
                            <Stack gap="none">
                                <Text><TextLink href={authorPath(entry.personId)}>{entry.name}</TextLink></Text>
                                {entry.aliasOf !== null && <Text variant="caption" tone="muted">alias of {entry.aliasOf}</Text>}
                            </Stack>
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
