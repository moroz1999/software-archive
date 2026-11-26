import {type ReactNode, useId} from 'react';
import {useTranslation} from 'react-i18next';
import {groupPath} from '@/entities/groups';
import {type Software, SoftwareCard, SoftwareCardSkeleton, softwarePath} from '@/entities/software';
import {useSkeleton} from '@/shared/lib/loading';
import {Button} from '@/shared/ui/button';
import {CardGrid} from '@/shared/ui/card-grid';
import {Checkbox} from '@/shared/ui/checkbox';
import {Inline} from '@/shared/ui/inline';
import {LoadingState} from '@/shared/ui/loading-state';
import {Pagination} from '@/shared/ui/pagination';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Table, TableBody, TableCell, TableHead, TableHeaderCell, TableRow} from '@/shared/ui/table';
import {Heading, Text, TextLink} from '@/shared/ui/typography';
import {FOUND_KINDS, foundAuthorHref, type FoundKind, type FoundSet} from '../model/found';
import {useFullSearch} from '../model/full-search-context';
import {chosenKinds, fullSearchHref} from '../model/full-search-filter';
import styles from './FullSearch.module.css';

const SKELETON_CARDS = [1, 2, 3, 4];
const SKELETON_ROWS = [1, 2, 3, 4, 5, 6];

const KIND_KEYS: Readonly<Record<FoundKind, 'search.kinds.author' | 'search.kinds.group' | 'search.kinds.software'>> = {
    author: 'search.kinds.author',
    group: 'search.kinds.group',
    software: 'search.kinds.software',
};

const TITLE_KEYS: Readonly<Record<FoundKind, 'search.authors' | 'search.groups' | 'search.software'>> = {
    author: 'search.authors',
    group: 'search.groups',
    software: 'search.software',
};

/** The full search: the kinds to search, a page of the one list of the matches and its pages; inside a FullSearchProvider. */
export interface FullSearchProps {
    /** What each card holds under it, outside its link, such as voting. */
    readonly cardFooter?: ((software: Software) => ReactNode) | undefined;
}

export function FullSearch({cardFooter}: FullSearchProps = {}) {
    const {filter} = useFullSearch();
    if (filter.keyword === null) {
        return null;
    }
    return (
        <Stack gap="xl">
            <Kinds/>
            <Results cardFooter={cardFooter}/>
        </Stack>
    );
}

/** A box for every kind, every kind checked at first; the last one checked stays. Another choice starts from page 1. */
function Kinds() {
    const {t} = useTranslation();
    const {filter, changing, show} = useFullSearch();
    const checked = new Set<FoundKind>(filter.types.length === 0 ? FOUND_KINDS : filter.types);
    const choose = (kinds: Iterable<FoundKind>) => {
        show({...filter, types: chosenKinds(kinds), page: 0});
    };
    return (
        <Inline gap="md" align="center" wrap>
            <Text as="span" variant="caption" tone="muted">{t('search.kindsLabel')}</Text>
            {FOUND_KINDS.map((kind) => (
                <Checkbox
                    key={kind}
                    label={t(KIND_KEYS[kind])}
                    checked={checked.has(kind)}
                    disabled={changing || (checked.has(kind) && checked.size === 1)}
                    onChange={(on) => {
                        choose(FOUND_KINDS.filter((other) => (other === kind ? on : checked.has(other))));
                    }}
                />
            ))}
            <Button
                size="sm"
                color="outlined"
                disabled={changing}
                onClick={() => {
                    choose(FOUND_KINDS);
                }}
            >
                {t('search.selectAll')}
            </Button>
        </Inline>
    );
}

/** The matches of one kind on the page, titled with the count of all of them. */
function Section({title, children}: {readonly title: string; readonly children: ReactNode}) {
    const id = useId();
    return (
        <section aria-labelledby={id}>
            <Stack gap="md">
                <Heading level={2} id={id} variant="title">{title}</Heading>
                {children}
            </Stack>
        </section>
    );
}

function Matches({set, cardFooter}: {readonly set: FoundSet} & FullSearchProps) {
    const {t} = useTranslation();
    switch (set.kind) {
        case 'author':
            return (
                <Table label={t(TITLE_KEYS.author, {count: set.total})}>
                    <TableHead>
                        <TableRow>
                            <TableHeaderCell>{t('search.columns.name')}</TableHeaderCell>
                            <TableHeaderCell>{t('search.columns.realName')}</TableHeaderCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {set.items.map((author) => (
                            <TableRow key={author.id}>
                                <TableCell><TextLink href={foundAuthorHref(author)}>{author.name}</TextLink></TableCell>
                                <TableCell>{author.kind === 'person' ? author.realName : null}</TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            );
        case 'group':
            return (
                <Table label={t(TITLE_KEYS.group, {count: set.total})}>
                    <TableHead>
                        <TableRow>
                            <TableHeaderCell>{t('search.columns.name')}</TableHeaderCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {set.items.map((group) => (
                            <TableRow key={group.id}>
                                <TableCell><TextLink href={groupPath(group.id)}>{group.name}</TextLink></TableCell>
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
            );
        case 'software':
            return (
                <CardGrid>
                    {set.items.map((software) => <li key={software.id}><SoftwareCard software={software} href={softwarePath(software.id)} footer={cardFooter?.(software)}/></li>)}
                </CardGrid>
            );
    }
}

function ResultsSkeleton() {
    return (
        <div data-testid="full-search-skeleton">
            <Stack gap="xl">
                <Stack gap="sm">{SKELETON_ROWS.map((row) => <Skeleton key={row} className={styles.row}/>)}</Stack>
                <CardGrid>{SKELETON_CARDS.map((card) => <li key={card}><SoftwareCardSkeleton/></li>)}</CardGrid>
            </Stack>
        </div>
    );
}

function Results({cardFooter}: FullSearchProps) {
    const {t} = useTranslation();
    const {filter, result, changing} = useFullSearch();
    const skeleton = useSkeleton(result);
    if (skeleton || result.isPending) {
        return result.isFetching || skeleton ? <ResultsSkeleton/> : null;
    }
    if (result.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    const {sets, page} = result.data;
    if (sets.length === 0) {
        return <Text tone="muted">{t('empty.nothingFound')}</Text>;
    }
    return (
        <LoadingState loading={changing}>
            <Stack gap="xl">
                {sets.map((set) => (
                    <Section key={set.kind} title={t(TITLE_KEYS[set.kind], {count: set.total})}>
                        <Matches set={set} cardFooter={cardFooter}/>
                    </Section>
                ))}
                <Pagination page={page.number} totalPages={page.totalPages} hrefOf={(number) => fullSearchHref({...filter, page: number})} disabled={changing}/>
            </Stack>
        </LoadingState>
    );
}
