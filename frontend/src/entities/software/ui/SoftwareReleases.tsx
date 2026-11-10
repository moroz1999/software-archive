import {type ReactNode, useId, useState} from 'react';
import {classNames} from '@/shared/lib/class-names';
import {MOBILE_QUERY, useMediaQuery} from '@/shared/lib/screen';
import {useSkeleton} from '@/shared/lib/skeleton';
import {Badge} from '@/shared/ui/badge';
import {Inline} from '@/shared/ui/inline';
import {Panel} from '@/shared/ui/panel';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Table, TableBody, TableCell, TableHead, TableHeaderCell, TableRow, type TableSort} from '@/shared/ui/table';
import {Toggle} from '@/shared/ui/toggle';
import {Heading, Text} from '@/shared/ui/typography';
import {CardGrid} from '@/shared/ui/card-grid';
import {releaseTypeText} from '../lib/release-type';
import {releaseYear} from '../lib/release-year';
import type {ReleaseType, SoftwareRelease} from '../model/release';
import {useSoftwareReleases} from '../model/software-releases-context';
import styles from './SoftwareReleases.module.css';

export interface SoftwareReleasesProps {
    /** What can be done with a release, shown with it, such as a link to edit it. */
    readonly actions?: (release: SoftwareRelease) => ReactNode;
}

type View = 'table' | 'cards';
type SortField = 'title' | 'year';

interface Sort {
    readonly field: SortField;
    readonly direction: Exclude<TableSort, 'none'>;
}

const VIEWS = [{value: 'table', label: 'Table'}, {value: 'cards', label: 'Cards'}] as const;

/** The releases of the software inside a SoftwareReleasesProvider: a table, or cards on a phone or when asked. */
export function SoftwareReleases({actions}: SoftwareReleasesProps) {
    const query = useSoftwareReleases();
    const skeleton = useSkeleton(query);

    if (skeleton || query.isPending) {
        return <SoftwareReleasesSkeleton withActions={actions !== undefined}/>;
    }
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    if (query.data.length === 0) {
        return <Text tone="muted">No releases known yet.</Text>;
    }
    return <ReleaseList releases={query.data} actions={actions}/>;
}

interface ReleaseListProps {
    readonly releases: readonly SoftwareRelease[];
    readonly actions: SoftwareReleasesProps['actions'];
}

function ReleaseList({releases, actions}: ReleaseListProps) {
    const phone = useMediaQuery(MOBILE_QUERY);
    const [view, setView] = useState<View>('table');
    const [type, setType] = useState<ReleaseType | 'all'>('all');
    const [sort, setSort] = useState<Sort | null>(null);

    const types = [...new Set(releases.map((release) => release.type))];
    const shown = sorted(type === 'all' ? releases : releases.filter((release) => release.type === type), sort);

    function sortBy(field: SortField) {
        setSort((current) => {
            if (current?.field === field) {
                return {field, direction: current.direction === 'ascending' ? 'descending' : 'ascending'};
            }
            // Titles read from A, years from the newest.
            return {field, direction: field === 'title' ? 'ascending' : 'descending'};
        });
    }

    return (
        <Stack gap="md">
            {(types.length > 1 || !phone) && (
                <Inline gap="md" wrap>
                    {types.length > 1 && (
                        <Toggle
                            label="Type"
                            options={[{value: 'all', label: 'All'}, ...types.map((value) => ({value, label: releaseTypeText(value)}))]}
                            value={type}
                            onChange={setType}
                        />
                    )}
                    {!phone && <Toggle label="View" options={VIEWS} value={view} onChange={setView} className={styles.viewToggle}/>}
                </Inline>
            )}
            {phone || view === 'cards'
                ? <ReleaseCards releases={shown} actions={actions}/>
                : <ReleaseTable releases={shown} actions={actions} sort={sort} onSort={sortBy}/>}
        </Stack>
    );
}

/** The releases in the order asked for; a release of unknown year comes last either way. */
function sorted(releases: readonly SoftwareRelease[], sort: Sort | null): readonly SoftwareRelease[] {
    if (sort === null) {
        return releases;
    }
    const sign = sort.direction === 'ascending' ? 1 : -1;
    return [...releases].sort((a, b) => {
        if (sort.field === 'title') {
            return sign * a.title.localeCompare(b.title);
        }
        if (a.releaseDate === null || b.releaseDate === null) {
            return Number(a.releaseDate === null) - Number(b.releaseDate === null);
        }
        return sign * a.releaseDate.localeCompare(b.releaseDate);
    });
}

function sortOf(sort: Sort | null, field: SortField): TableSort {
    return sort?.field === field ? sort.direction : 'none';
}

interface ReleaseHeaderProps {
    readonly withActions: boolean;
    /** Absent in the skeleton: nothing to sort yet. */
    readonly sort?: Sort | null;
    readonly onSort?: (field: SortField) => void;
}

/** The column names, the same over the data and over its skeleton; the widths are fixed so neither moves the other. */
function ReleaseHeader({withActions, sort = null, onSort}: ReleaseHeaderProps) {
    return (
        <TableHead>
            <TableRow>
                <TableHeaderCell {...sortProps(sort, 'title', onSort)}>Title</TableHeaderCell>
                <TableHeaderCell align="center" className={styles.yearColumn} {...sortProps(sort, 'year', onSort)}>Year</TableHeaderCell>
                <TableHeaderCell align="center" className={styles.versionColumn}>Version</TableHeaderCell>
                <TableHeaderCell className={styles.typeColumn}>Type</TableHeaderCell>
                <TableHeaderCell className={styles.hardwareColumn}>Hardware</TableHeaderCell>
                {withActions && <TableHeaderCell className={styles.actionsColumn}/>}
            </TableRow>
        </TableHead>
    );
}

function sortProps(sort: Sort | null, field: SortField, onSort: ((field: SortField) => void) | undefined) {
    if (onSort === undefined) {
        return {};
    }
    return {
        sort: sortOf(sort, field),
        onSort: () => {
            onSort(field);
        },
    };
}

interface ReleaseTableProps extends ReleaseListProps {
    readonly sort: Sort | null;
    readonly onSort: (field: SortField) => void;
}

function ReleaseTable({releases, actions, sort, onSort}: ReleaseTableProps) {
    return (
        <Table label="Releases" fixed>
            <ReleaseHeader sort={sort} onSort={onSort} withActions={actions !== undefined}/>
            <TableBody>
                {releases.map((release) => (
                    <TableRow key={release.id}>
                        <TableCell>
                            <Stack gap="xs">
                                <Text as="span" tone="strong">{release.title}</Text>
                                {release.description !== null && <Text as="span" variant="bodySm" tone="muted">{release.description}</Text>}
                            </Stack>
                        </TableCell>
                        <TableCell align="center">{release.releaseDate === null ? null : releaseYear(release.releaseDate)}</TableCell>
                        <TableCell align="center">{release.version}</TableCell>
                        <TableCell><TypeBadge type={release.type}/></TableCell>
                        <TableCell>
                            <Inline gap="sm" wrap>
                                {release.hardware.map((item) => <Text key={item.code} as="span">{item.name}</Text>)}
                            </Inline>
                        </TableCell>
                        {actions !== undefined && <TableCell align="end">{actions(release)}</TableCell>}
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
}

function ReleaseCards({releases, actions}: ReleaseListProps) {
    return (
        <CardGrid>
            {releases.map((release) => <ReleaseCard key={release.id} release={release} actions={actions}/>)}
        </CardGrid>
    );
}

interface ReleaseCardProps {
    readonly release: SoftwareRelease;
    readonly actions: SoftwareReleasesProps['actions'];
}

function ReleaseCard({release, actions}: ReleaseCardProps) {
    const titleId = useId();
    const facts = [
        release.releaseDate === null ? null : String(releaseYear(release.releaseDate)),
        release.version === null ? null : `Version ${release.version}`,
    ].filter((fact) => fact !== null);
    return (
        <Panel as="article" labelledBy={titleId} padding="md">
            <Stack gap="sm">
                <Inline gap="sm" justify="between" align="center" wrap>
                    <Heading id={titleId} level={3} variant="title">{release.title}</Heading>
                    <TypeBadge type={release.type}/>
                </Inline>
                {facts.length > 0 && <Text variant="bodySm" tone="muted">{facts.join(' · ')}</Text>}
                {release.description !== null && <Text>{release.description}</Text>}
                {release.hardware.length > 0 && (
                    <Inline gap="xs" wrap>
                        {release.hardware.map((item) => <Badge key={item.code} variant="secondary">{item.name}</Badge>)}
                    </Inline>
                )}
                {actions?.(release)}
            </Stack>
        </Panel>
    );
}

/** The type of a release; one nobody knows is left out. */
function TypeBadge({type}: {readonly type: ReleaseType}) {
    return type === 'unknown' ? null : <Badge variant="secondary">{releaseTypeText(type)}</Badge>;
}

const SKELETON_ROWS = [1, 2, 3] as const;

/** Loading placeholder shaped like the releases: the bar, then the table, or cards on a phone. */
function SoftwareReleasesSkeleton({withActions}: {readonly withActions: boolean}) {
    const phone = useMediaQuery(MOBILE_QUERY);
    return (
        <div data-testid="software-releases-skeleton" aria-hidden="true">
            {phone
                ? (
                    <CardGrid>
                        {SKELETON_ROWS.map((row) => <ReleaseCardSkeleton key={row} withActions={withActions}/>)}
                    </CardGrid>
                )
                : (
                    <Stack gap="md">
                        <Inline gap="md" wrap>
                            <Skeleton className={classNames(styles.toggleBone, styles.viewToggle)}/>
                        </Inline>
                        <Table label="Releases" fixed>
                            <ReleaseHeader withActions={withActions}/>
                            <TableBody>
                                {SKELETON_ROWS.map((row) => (
                                    <TableRow key={row}>
                                        <TableCell><Skeleton className={styles.titleBone}/></TableCell>
                                        <TableCell align="center"><Skeleton inline className={styles.yearBone}/></TableCell>
                                        <TableCell align="center"><Skeleton inline className={styles.versionBone}/></TableCell>
                                        <TableCell><Skeleton inline className={styles.badgeBone}/></TableCell>
                                        <TableCell>
                                            <Inline gap="sm" wrap>
                                                <Skeleton inline className={styles.hardwareBone}/>
                                                <Skeleton inline className={styles.hardwareBone}/>
                                            </Inline>
                                        </TableCell>
                                        {withActions && <TableCell align="end"><Skeleton inline className={styles.buttonBone}/></TableCell>}
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    </Stack>
                )}
        </div>
    );
}

function ReleaseCardSkeleton({withActions}: {readonly withActions: boolean}) {
    return (
        <Panel padding="md">
            <Stack gap="sm">
                <Inline gap="sm" justify="between" align="center" wrap>
                    <Skeleton className={styles.cardTitleBone}/>
                    <Skeleton inline className={styles.badgeBone}/>
                </Inline>
                <Skeleton className={styles.factsBone}/>
                <Inline gap="xs" wrap>
                    <Skeleton inline className={styles.badgeBone}/>
                    <Skeleton inline className={styles.badgeBone}/>
                </Inline>
                {withActions && <Skeleton className={styles.buttonBone}/>}
            </Stack>
        </Panel>
    );
}
