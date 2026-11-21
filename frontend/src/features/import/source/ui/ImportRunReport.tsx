import {useId} from 'react';
import type {TFunction} from 'i18next';
import {useTranslation} from 'react-i18next';
import {authorPath, toAuthorId} from '@/entities/authors';
import {groupPath, toGroupId} from '@/entities/groups';
import {labelPath, toLabelId} from '@/entities/labels';
import {softwarePath} from '@/entities/software';
import {Chip} from '@/shared/ui/chip';
import {Inline} from '@/shared/ui/inline';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Heading, Text, TextLink} from '@/shared/ui/typography';
import type {ReactNode} from 'react';
import {runTimeText} from '../lib/run-time';
import {type ImportRun, type ImportRunAuthor, type ImportRunProgress, importSourceOf, SOFTWARE_SUPPLEMENTS} from '../model/import-run';
import {useSourceImport} from '../model/source-import-context';
import styles from './ImportRunReport.module.css';

/** What the import started last did: read again while it goes on. */
export function ImportRunReport() {
    const {t} = useTranslation();
    const {lastRun} = useSourceImport();
    const headingId = useId();

    return (
        <section aria-labelledby={headingId}>
            <Stack gap="md">
                <Heading level={2} variant="title" id={headingId}>{t('imports.lastRun')}</Heading>
                {lastRun.data === undefined
                    ? !lastRun.isLoadingError && <Skeleton className={styles.bone}/>
                    : lastRun.data === null
                        ? <Text tone="muted">{t('imports.noRun')}</Text>
                        : <Report run={lastRun.data}/>}
            </Stack>
        </section>
    );
}

function Report({run}: {readonly run: ImportRun}) {
    const {t} = useTranslation();
    const supplemented = run.knownPrograms.filter((program) => program.supplements.length > 0);
    const unchanged = run.knownPrograms.filter((program) => program.supplements.length === 0);
    const created = run.authors.filter((author) => author.created);
    const found = run.authors.filter((author) => !author.created);
    return (
        <Stack gap="lg">
            <Stack gap="xs">
                <Inline gap="sm" align="center" wrap>
                    <Text variant="label" tone="strong">{scopeText(run, t)}</Text>
                    <Chip size="sm">{t(`imports.states.${run.state}`)}</Chip>
                </Inline>
                <Text tone="muted">
                    {run.endedAt === null
                        ? t('imports.startedAt', {time: runTimeText(run.startedAt)})
                        : t('imports.ranFromTo', {from: runTimeText(run.startedAt), to: runTimeText(run.endedAt)})}
                </Text>
                <Text>{t('imports.counts', {added: run.added, known: run.known, skipped: run.skipped, failed: run.failed})}</Text>
                {run.state === 'running' && <Progress progress={run.progress}/>}
            </Stack>
            <ReportSection label={t('imports.addedPrograms')} items={run.addedPrograms.map((program) => ({
                key: program.externalId,
                content: <SoftwareLink title={program.title} href={softwarePath(program.softwareId)} exists={program.exists}/>,
            }))}/>
            <ReportSection label={t('imports.supplementedPrograms')} items={supplemented.map((program) => ({
                key: program.externalId,
                content: (
                    <Stack gap="xs">
                        <SoftwareLink title={program.title} href={softwarePath(program.softwareId)} exists={program.exists}/>
                        <Text variant="caption" tone="muted">
                            {SOFTWARE_SUPPLEMENTS.filter((code) => program.supplements.includes(code))
                                .map((code) => t(`imports.supplements.${code}`))
                                .join(', ')}
                        </Text>
                    </Stack>
                ),
            }))}/>
            <ReportSection label={t('imports.unchangedPrograms')} items={unchanged.map((program) => ({
                key: program.externalId,
                content: <SoftwareLink title={program.title} href={softwarePath(program.softwareId)} exists={program.exists}/>,
            }))}/>
            <ReportSection label={t('imports.skippedPrograms')} items={run.skippedPrograms.map((program) => ({
                key: program.externalId,
                content: (
                    <Stack gap="xs">
                        <TextLink href={program.url}>{program.title}</TextLink>
                        <Text variant="caption" tone="muted">{t(`imports.skipReasons.${program.reason}`, {detail: program.detail})}</Text>
                    </Stack>
                ),
            }))}/>
            <ReportSection label={t('imports.failedPrograms')} items={run.failedPrograms.map((program) => ({
                key: program.externalId,
                content: (
                    <Stack gap="xs">
                        <TextLink href={program.url}>{program.url}</TextLink>
                        <Text variant="caption" tone="danger">{program.reason}</Text>
                    </Stack>
                ),
            }))}/>
            <ReportSection label={t('imports.createdAuthors')} items={created.map((author) => ({key: author.id, content: <AuthorLine author={author}/>}))}/>
            <ReportSection label={t('imports.foundAuthors')} items={found.map((author) => ({key: author.id, content: <AuthorLine author={author}/>}))}/>
            <ReportSection label={t('imports.addedPlaces')} items={run.places.map((place, index) => ({
                key: String(index),
                content: <Text as="span">{`${t(`imports.placeKinds.${place.kind}`)} ${place.name}`}</Text>,
            }))}/>
        </Stack>
    );
}

/** Which program the running import goes to, leading to its page in the source; before the first, the list. */
function Progress({progress}: {readonly progress: ImportRunProgress}) {
    const {t} = useTranslation();
    if (progress.position === 0) {
        return <Text>{t('imports.now.list')}</Text>;
    }
    const text = progress.total === null
        ? t('imports.now.programUncounted', {position: progress.position})
        : t('imports.now.program', {position: progress.position, total: progress.total});
    return progress.url === null ? <Text>{text}</Text> : <TextLink href={progress.url}>{text}</TextLink>;
}

interface ReportItem {
    readonly key: string;
    readonly content: ReactNode;
}

/** A titled list of the report; left out while it has nothing. */
function ReportSection({label, items}: {readonly label: string; readonly items: readonly ReportItem[]}) {
    if (items.length === 0) {
        return null;
    }
    return (
        <Stack as="section" gap="xs" ariaLabel={label}>
            <Text variant="label" tone="muted">{label}</Text>
            <Stack as="ul" gap="sm">
                {items.map((item) => <li key={item.key}>{item.content}</li>)}
            </Stack>
        </Stack>
    );
}

function SoftwareLink({title, href, exists}: {readonly title: string; readonly href: string; readonly exists: boolean}) {
    const {t} = useTranslation();
    return exists ? <TextLink href={href}>{title}</TextLink> : <Text as="span" tone="muted">{t('imports.deleted', {title})}</Text>;
}

/** An author the run took, leading to its page while it is there, with what it is. */
function AuthorLine({author}: {readonly author: ImportRunAuthor}) {
    const {t} = useTranslation();
    return (
        <Inline gap="sm" align="center" wrap>
            {author.exists
                ? <TextLink href={authorHref(author)}>{author.name}</TextLink>
                : <Text as="span" tone="muted">{t('imports.deleted', {title: author.name})}</Text>}
            <Chip size="sm">{t(`imports.authorKinds.${author.kind}`)}</Chip>
        </Inline>
    );
}

function authorHref(author: ImportRunAuthor): string {
    switch (author.kind) {
        case 'author':
            return authorPath(toAuthorId(author.id));
        case 'group':
            return groupPath(toGroupId(author.id));
        case 'label':
            return labelPath(toLabelId(author.id));
    }
}

/** What the run read: the archive, and for vektor-06c.ru its section or the whole site. */
function scopeText(run: ImportRun, t: TFunction): string {
    const source = importSourceOf(run.archive);
    if (source === undefined) {
        return run.archive;
    }
    const archive = t(`imports.sources.${source}`);
    return source === 'vektor' ? `${archive} · ${run.section ?? t('imports.wholeSite')}` : archive;
}
