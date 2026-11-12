import {useEffect, useState} from 'react';
import {useTranslation} from 'react-i18next';
import type {CategoryId, CategoryNode} from '@/entities/categories';
import {toCountryId} from '@/entities/locations';
import {SoftwareCard, SoftwareCardSkeleton, softwarePath} from '@/entities/software';
import {useSkeleton} from '@/shared/lib/loading';
import {CardGrid} from '@/shared/ui/card-grid';
import {Field} from '@/shared/ui/field';
import {Inline} from '@/shared/ui/inline';
import {Input} from '@/shared/ui/input';
import {LoadingState} from '@/shared/ui/loading-state';
import {Pagination} from '@/shared/ui/pagination';
import {Select} from '@/shared/ui/select';
import {SidebarLayout} from '@/shared/ui/sidebar-layout';
import {Stack} from '@/shared/ui/stack';
import {Text, TextLink} from '@/shared/ui/typography';
import {Tree, type TreeItem} from '@/shared/ui/tree';
import {branchTo} from '../lib/category-branch';
import type {FacetCount} from '../model/catalog';
import {catalogHref} from '../model/catalog-filter';
import {useSoftwareCatalog} from '../model/software-catalog-context';
import styles from './SoftwareCatalog.module.css';

const SKELETON_CARDS = [1, 2, 3, 4, 5, 6, 7, 8];

/** How long the keyword waits for the visitor to stop typing before it is looked for. */
const KEYWORD_DELAY_MS = 300;

/** The software catalog: the categories to go through beside the keyword, the filters, the cards and the pages; inside a SoftwareCatalogProvider. */
export function SoftwareCatalog() {
    return (
        <SidebarLayout aside={<Categories/>}>
            <Stack gap="lg">
                <Filters/>
                <Cards/>
            </Stack>
        </SidebarLayout>
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

function toItem(node: CategoryNode, hrefOf: (node: CategoryNode | null) => string, current: CategoryId | null, changing: boolean): TreeItem {
    return {
        id: node.id,
        name: node.title,
        content: <CategoryLink href={hrefOf(node)} current={node.id === current} changing={changing}>{node.title}</CategoryLink>,
        children: node.children.map((child) => toItem(child, hrefOf, current, changing)),
    };
}

/** A category of the tree; locked, it is shown but leads nowhere. */
function CategoryLink({href, current, changing, children}: {readonly href: string; readonly current: boolean; readonly changing: boolean; readonly children: string}) {
    return changing
        ? <Text as="span" tone={current ? 'strong' : 'muted'}>{children}</Text>
        : <TextLink href={href} current={current}>{children}</TextLink>;
}

/**
 * The way through the catalog: the whole category tree, folded but for the path to the current category. A category
 * keeps the keyword and the filters and starts from the first page; nothing is counted.
 */
function Categories() {
    const {t} = useTranslation();
    const {filter, categories, changing} = useSoftwareCatalog();
    const tree = categories.data;
    if (tree === undefined) {
        return null;
    }
    const hrefOf = (node: CategoryNode | null) => catalogHref({...filter, category: node?.id ?? null, page: 0});
    const path = filter.category === null ? [] : branchTo(tree, filter.category).map((node) => node.id);
    return (
        <nav aria-label={t('navigation.categories')}>
            <Stack gap="sm">
                <CategoryLink href={hrefOf(null)} current={filter.category === null} changing={changing}>{t('filters.allCategories')}</CategoryLink>
                <Tree
                    items={tree.map((node) => toItem(node, hrefOf, filter.category, changing))}
                    ariaLabel={t('navigation.categories')}
                    expandLabel={(name) => t('category.expand', {name})}
                    collapseLabel={(name) => t('category.collapse', {name})}
                    initiallyExpanded={path}
                />
            </Stack>
        </nav>
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
