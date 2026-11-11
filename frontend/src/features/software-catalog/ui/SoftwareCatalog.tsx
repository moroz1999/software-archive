import {useEffect, useState} from 'react';
import {useTranslation} from 'react-i18next';
import type {CategoryNode} from '@/entities/categories';
import {toCountryId} from '@/entities/locations';
import {SoftwareCard, SoftwareCardSkeleton, softwarePath} from '@/entities/software';
import {useSkeleton} from '@/shared/lib/skeleton';
import {CardGrid} from '@/shared/ui/card-grid';
import {Field} from '@/shared/ui/field';
import {Inline} from '@/shared/ui/inline';
import {Input} from '@/shared/ui/input';
import {LoadingState} from '@/shared/ui/loading-state';
import {NavChip, NavChips} from '@/shared/ui/nav-chips';
import {Pagination} from '@/shared/ui/pagination';
import {Select} from '@/shared/ui/select';
import {Stack} from '@/shared/ui/stack';
import {Text} from '@/shared/ui/typography';
import {branchTo} from '../lib/category-branch';
import type {FacetCount} from '../model/catalog';
import {catalogHref} from '../model/catalog-filter';
import {useSoftwareCatalog} from '../model/software-catalog-context';
import styles from './SoftwareCatalog.module.css';

const SKELETON_CARDS = [1, 2, 3, 4, 5, 6, 7, 8];

/** How long the keyword waits for the visitor to stop typing before it is looked for. */
const KEYWORD_DELAY_MS = 300;

/** The software catalog with its keyword, filters, categories and pages, inside a SoftwareCatalogProvider. */
export function SoftwareCatalog() {
    return (
        <Stack gap="lg">
            <Filters/>
            <Categories/>
            <Cards/>
        </Stack>
    );
}

function trimmed(text: string): string | null {
    const keyword = text.trim();
    return keyword === '' ? null : keyword;
}

/**
 * The keyword is looked for once the visitor stops typing; another keyword of the address replaces the typed one. Never
 * locked, so a pause in the middle of a word does not take the field away.
 */
function KeywordFilter() {
    const {t} = useTranslation();
    const {filter, show} = useSoftwareCatalog();
    const [text, setText] = useState(filter.keyword ?? '');
    const [shown, setShown] = useState(filter.keyword);
    if (shown !== filter.keyword) {
        setShown(filter.keyword);
        if (trimmed(text) !== filter.keyword) {
            setText(filter.keyword ?? '');
        }
    }

    useEffect(() => {
        const keyword = trimmed(text);
        if (keyword === filter.keyword) {
            return undefined;
        }
        const timer = setTimeout(() => {
            show({...filter, keyword, page: 0});
        }, KEYWORD_DELAY_MS);
        return () => {
            clearTimeout(timer);
        };
    }, [text, filter, show]);

    return (
        <Field label={t('filters.search')} className={styles.filter}>
            {(control) => <Input id={control.id} type="search" value={text} onChange={setText}/>}
        </Field>
    );
}

/** The values of a filter with their counts, the chosen one kept even when it leaves nothing. */
function withChosen<T>(counts: readonly FacetCount<T>[], chosen: T | null): readonly FacetCount<T>[] {
    return chosen === null || counts.some((count) => count.value === chosen) ? counts : [{value: chosen, count: 0}, ...counts];
}

function Filters() {
    const {t} = useTranslation();
    const {filter, catalog, countries, changing, show} = useSoftwareCatalog();
    const facets = catalog.data?.facets;
    const years = [...withChosen(facets?.years ?? [], filter.year)].sort((a, b) => a.value - b.value);
    const names = new Map((countries.data ?? []).map((country) => [country.id, country.name]));
    const countryOptions = withChosen(facets?.countries ?? [], filter.country).flatMap(({value, count}) => {
        const name = names.get(value);
        return name === undefined ? [] : [{value, label: t('filters.withCount', {label: name, count})}];
    });
    return (
        <Inline gap="md" wrap>
            <KeywordFilter/>
            <Field label={t('filters.year')} className={styles.filter}>
                {(control) => (
                    <Select
                        id={control.id}
                        value={filter.year === null ? '' : String(filter.year)}
                        onChange={(value) => {
                            show({...filter, year: value === '' ? null : Number(value), page: 0});
                        }}
                        options={years.map(({value, count}) => ({value: String(value), label: t('filters.withCount', {label: value, count})}))}
                        placeholder={t('filters.anyYear')}
                        disabled={changing || facets === undefined}
                    />
                )}
            </Field>
            <Field label={t('filters.country')} className={styles.filter}>
                {(control) => (
                    <Select
                        id={control.id}
                        value={filter.country ?? ''}
                        onChange={(value) => {
                            show({...filter, country: value === '' ? null : toCountryId(value), page: 0});
                        }}
                        options={countryOptions}
                        placeholder={t('filters.anyCountry')}
                        disabled={changing || facets === undefined || countries.data === undefined}
                    />
                )}
            </Field>
        </Inline>
    );
}

/** The path down to the chosen category, and the subcategories that have software, with their counts. */
function Categories() {
    const {t} = useTranslation();
    const {filter, catalog, categories, changing} = useSoftwareCatalog();
    const tree = categories.data;
    const facets = catalog.data?.facets;
    if (tree === undefined || facets === undefined) {
        return null;
    }
    const branch = filter.category === null ? [] : branchTo(tree, filter.category);
    const below: readonly CategoryNode[] = filter.category === null ? tree : branch.at(-1)?.children ?? [];
    const counts = new Map(facets.categories.map(({value, count}) => [value, count]));
    const offered = below.flatMap((node) => {
        const count = counts.get(node.id) ?? 0;
        return count > 0 ? [{node, count}] : [];
    });
    const hrefOf = (node: CategoryNode | null) => catalogHref({...filter, category: node?.id ?? null, page: 0});
    return (
        <Stack gap="sm">
            {branch.length > 0 && (
                <NavChips label={t('filters.category')}>
                    <NavChip href={hrefOf(null)} disabled={changing}>{t('filters.allCategories')}</NavChip>
                    {branch.map((node) => (
                        <NavChip key={node.id} href={hrefOf(node)} active={node.id === filter.category} disabled={changing}>{node.title}</NavChip>
                    ))}
                </NavChips>
            )}
            {offered.length > 0 && (
                <NavChips label={t('filters.subcategories')}>
                    {offered.map(({node, count}) => (
                        <NavChip key={node.id} href={hrefOf(node)} disabled={changing}>
                            {t('filters.withCount', {label: node.title, count})}
                        </NavChip>
                    ))}
                </NavChips>
            )}
        </Stack>
    );
}

function Cards() {
    const {t} = useTranslation();
    const {filter, catalog, changing} = useSoftwareCatalog();
    const skeleton = useSkeleton(catalog);

    if (skeleton || catalog.isPending) {
        return (
            <div data-testid="software-catalog-skeleton">
                <CardGrid>
                    {SKELETON_CARDS.map((card) => <li key={card}><SoftwareCardSkeleton/></li>)}
                </CardGrid>
            </div>
        );
    }
    if (catalog.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    const {page} = catalog.data;
    if (page.items.length === 0) {
        return <Text tone="muted">{t('empty.nothingFound')}</Text>;
    }
    return (
        <LoadingState loading={changing}>
            <Stack gap="lg">
                <CardGrid>
                    {page.items.map((software) => (
                        <li key={software.id}>
                            <SoftwareCard software={software} href={softwarePath(software.id)}/>
                        </li>
                    ))}
                </CardGrid>
                <Pagination page={page.number} totalPages={page.totalPages} hrefOf={(number) => catalogHref({...filter, page: number})} disabled={changing}/>
            </Stack>
        </LoadingState>
    );
}
