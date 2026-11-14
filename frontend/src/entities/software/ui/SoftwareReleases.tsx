import {type ReactNode, useId, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {classNames} from '@/shared/lib/class-names';
import {MOBILE_QUERY, useMediaQuery} from '@/shared/lib/screen';
import {useSkeleton} from '@/shared/lib/loading';
import {Badge} from '@/shared/ui/badge';
import {Button} from '@/shared/ui/button';
import {Inline} from '@/shared/ui/inline';
import {Panel} from '@/shared/ui/panel';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Table, TableBody, TableCell, TableHead, TableHeaderCell, TableRow, type TableSort} from '@/shared/ui/table';
import {Toggle} from '@/shared/ui/toggle';
import {Heading, Text, TextLink} from '@/shared/ui/typography';
import {CardGrid} from '@/shared/ui/card-grid';
import {CardScreenshots} from '@/shared/ui/card-screenshots';
import {releasePath} from '../config/paths';
import {releaseDateText} from '../lib/release-date';
import type {ReleaseType, SoftwareRelease} from '../model/release';
import {fileSizeText, type ReleaseArchive} from '../model/release-archive';
import type {SoftwareId} from '../model/software';
import {useSoftwareReleases} from '../model/software-releases-context';
import styles from './SoftwareReleases.module.css';

export interface SoftwareReleasesProps {
    /** The software whose releases these are: when given, every release title leads to its page. */
    readonly softwareId?: SoftwareId;
    /** What can be done with a release, shown with it, such as a link to edit it. */
    readonly actions?: (release: SoftwareRelease) => ReactNode;
}

type View = 'table' | 'cards';
type SortField = 'title' | 'date';

interface Sort {
    readonly field: SortField;
    readonly direction: Exclude<TableSort, 'none'>;
}

/** The releases of the software inside a SoftwareReleasesProvider: a table, or cards on a phone or when asked. */
export function SoftwareReleases({softwareId, actions}: SoftwareReleasesProps) {
    const {t} = useTranslation();
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
        return <Text tone="muted">{t('empty.releases')}</Text>;
    }
    return <ReleaseList softwareId={softwareId} releases={query.data} actions={actions}/>;
}

interface ReleaseListProps {
    readonly softwareId: SoftwareId | undefined;
    readonly releases: readonly SoftwareRelease[];
    readonly actions: SoftwareReleasesProps['actions'];
}

function ReleaseList({softwareId, releases, actions}: ReleaseListProps) {
    const {t} = useTranslation();
    const phone = useMediaQuery(MOBILE_QUERY);
    const [view, setView] = useState<View>('table');
    const [type, setType] = useState<ReleaseType | 'all'>('all');
    const [sort, setSort] = useState<Sort | null>(null);
    const views = [{value: 'table', label: t('release.table')}, {value: 'cards', label: t('release.cards')}] as const;

    const types = [...new Set(releases.map((release) => release.type))];
    const shown = sorted(type === 'all' ? releases : releases.filter((release) => release.type === type), sort);

    function sortBy(field: SortField) {
        setSort((current) => {
            if (current?.field === field) {
                return {field, direction: current.direction === 'ascending' ? 'descending' : 'ascending'};
            }
            // Titles read from A, dates from the newest.
            return {field, direction: field === 'title' ? 'ascending' : 'descending'};
        });
    }

    return (
        <Stack gap="md">
            {(types.length > 1 || !phone) && (
                <Inline gap="md" wrap>
                    {types.length > 1 && (
                        <Toggle
                            label={t('fields.type')}
                            options={[{value: 'all', label: t('release.allTypes')}, ...types.map((value) => ({value, label: t(`release.types.${value}`)}))]}
                            value={type}
                            onChange={setType}
                        />
                    )}
                    {!phone && (
                        <Toggle
                            label={t('release.view')}
                            options={views}
                            value={view}
                            onChange={setView}
                            className={styles.viewToggle}
                        />
                    )}
                </Inline>
            )}
            {phone || view === 'cards'
                ? <ReleaseCards softwareId={softwareId} releases={shown} actions={actions}/>
                : <ReleaseTable softwareId={softwareId} releases={shown} actions={actions} sort={sort} onSort={sortBy}/>}
        </Stack>
    );
}

/** The releases in the order asked for; a release of unknown date comes last either way. */
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
    const {t} = useTranslation();
    return (
        <TableHead>
            <TableRow>
                <TableHeaderCell {...sortProps(sort, 'title', onSort)}>{t('fields.title')}</TableHeaderCell>
                <TableHeaderCell align="center" className={styles.dateColumn} {...sortProps(sort, 'date', onSort)}>{t('fields.releaseDate')}</TableHeaderCell>
                <TableHeaderCell align="center" className={styles.versionColumn}>{t('fields.version')}</TableHeaderCell>
                <TableHeaderCell className={styles.typeColumn}>{t('fields.type')}</TableHeaderCell>
                <TableHeaderCell className={styles.hardwareColumn}>{t('fields.hardware')}</TableHeaderCell>
                <TableHeaderCell className={styles.archiveColumn}>{t('release.archive')}</TableHeaderCell>
                {withActions && <TableHeaderCell/>}
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

function ReleaseTable({softwareId, releases, actions, sort, onSort}: ReleaseTableProps) {
    const {t} = useTranslation();
    return (
        <Table label={t('release.releases')}>
            <ReleaseHeader sort={sort} onSort={onSort} withActions={actions !== undefined}/>
            <TableBody>
                {releases.map((release) => (
                    <TableRow key={release.id}>
                        <TableCell>
                            <Inline gap="md" align="center">
                                {release.screenshots[0] !== undefined && (
                                    <img src={release.screenshots[0].url} alt={release.title} loading="lazy" className={styles.thumbnail}/>
                                )}
                                <Stack gap="xs">
                                    <Text as="span" tone="strong"><ReleaseTitle softwareId={softwareId} release={release}/></Text>
                                    {release.description !== null && <Text as="span" variant="bodySm" tone="muted">{release.description}</Text>}
                                </Stack>
                            </Inline>
                        </TableCell>
                        <TableCell align="center">{release.releaseDate === null ? null : releaseDateText(release.releaseDate)}</TableCell>
                        <TableCell align="center">{release.version}</TableCell>
                        <TableCell><TypeBadge type={release.type}/></TableCell>
                        <TableCell>
                            <Inline gap="sm" wrap>
                                {release.hardware.map((item) => <Text key={item.code} as="span">{item.name}</Text>)}
                            </Inline>
                        </TableCell>
                        <TableCell>{release.archive != null && <Download release={release} archive={release.archive}/>}</TableCell>
                        {actions !== undefined && <TableCell align="end">{actions(release)}</TableCell>}
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
}

/** The archive of a release to download, with its size. */
function Download({release, archive}: {readonly release: SoftwareRelease; readonly archive: ReleaseArchive}) {
    const {t} = useTranslation();
    return (
        <Inline gap="sm" align="center" wrap>
            <Button href={archive.url} target="_self" color="secondary" ariaLabel={t('release.downloadNamed', {title: release.title})}>{t('release.download')}</Button>
            <Text as="span" variant="bodySm" tone="muted">{fileSizeText(archive.size)}</Text>
        </Inline>
    );
}

function ReleaseCards({softwareId, releases, actions}: ReleaseListProps) {
    return (
        <CardGrid>
            {releases.map((release) => <ReleaseCard key={release.id} softwareId={softwareId} release={release} actions={actions}/>)}
        </CardGrid>
    );
}

interface ReleaseCardProps {
    readonly softwareId: SoftwareId | undefined;
    readonly release: SoftwareRelease;
    readonly actions: SoftwareReleasesProps['actions'];
}

function ReleaseCard({softwareId, release, actions}: ReleaseCardProps) {
    const {t} = useTranslation();
    const titleId = useId();
    const facts = [
        release.releaseDate === null ? null : releaseDateText(release.releaseDate),
        release.version === null ? null : t('release.versionFact', {version: release.version}),
    ].filter((fact) => fact !== null);
    return (
        <Panel as="article" labelledBy={titleId} padding="md">
            <Stack gap="sm">
                {release.screenshots.length > 0 && (
                    <CardScreenshots urls={release.screenshots.map((screenshot) => screenshot.url)} alt={release.title}/>
                )}
                <Inline gap="sm" justify="between" align="center" wrap>
                    <Heading id={titleId} level={3} variant="title"><ReleaseTitle softwareId={softwareId} release={release}/></Heading>
                    <TypeBadge type={release.type}/>
                </Inline>
                {facts.length > 0 && <Text variant="bodySm" tone="muted">{facts.join(' · ')}</Text>}
                {release.description !== null && <Text>{release.description}</Text>}
                {release.hardware.length > 0 && (
                    <Inline gap="xs" wrap>
                        {release.hardware.map((item) => <Badge key={item.code} variant="secondary">{item.name}</Badge>)}
                    </Inline>
                )}
                {release.archive != null && <Download release={release} archive={release.archive}/>}
                {actions?.(release)}
            </Stack>
        </Panel>
    );
}

function ReleaseTitle({softwareId, release}: {readonly softwareId: SoftwareId | undefined; readonly release: SoftwareRelease}) {
    return softwareId === undefined ? release.title : <TextLink href={releasePath(softwareId, release.id)}>{release.title}</TextLink>;
}

/** The type of a release; one nobody knows is left out. */
function TypeBadge({type}: {readonly type: ReleaseType}) {
    const {t} = useTranslation();
    return type === 'unknown' ? null : <Badge variant="secondary">{t(`release.types.${type}`)}</Badge>;
}

const SKELETON_ROWS = [1, 2, 3] as const;

/** Loading placeholder shaped like the releases: the bar, then the table, or cards on a phone. */
function SoftwareReleasesSkeleton({withActions}: {readonly withActions: boolean}) {
    const phone = useMediaQuery(MOBILE_QUERY);
    const {t} = useTranslation();
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
                        <Table label={t('release.releases')}>
                            <ReleaseHeader withActions={withActions}/>
                            <TableBody>
                                {SKELETON_ROWS.map((row) => (
                                    <TableRow key={row}>
                                        <TableCell><Skeleton className={styles.titleBone}/></TableCell>
                                        <TableCell align="center"><Skeleton inline className={styles.dateBone}/></TableCell>
                                        <TableCell align="center"><Skeleton inline className={styles.versionBone}/></TableCell>
                                        <TableCell><Skeleton inline className={styles.badgeBone}/></TableCell>
                                        <TableCell>
                                            <Inline gap="sm" wrap>
                                                <Skeleton inline className={styles.hardwareBone}/>
                                                <Skeleton inline className={styles.hardwareBone}/>
                                            </Inline>
                                        </TableCell>
                                        <TableCell/>
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
