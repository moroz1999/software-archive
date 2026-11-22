import {type ReactNode, useId} from 'react';
import type {TFunction} from 'i18next';
import {Trans, useTranslation} from 'react-i18next';
import {authorPath, toAuthorId} from '@/entities/authors';
import {groupPath, toGroupId} from '@/entities/groups';
import {labelPath, toLabelId} from '@/entities/labels';
import {fileSizeText, type SoftwareId, softwarePath} from '@/entities/software';
import {Chip} from '@/shared/ui/chip';
import {Inline} from '@/shared/ui/inline';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Heading, Text, TextLink} from '@/shared/ui/typography';
import {logTimeText, runTimeText} from '../lib/run-time';
import {type ImportLogEntry, type ImportRun, type ImportRunAuthor, type ImportRunProgress, importSourceOf, SOFTWARE_SUPPLEMENTS} from '../model/import-run';
import {useSourceImport} from '../model/source-import-context';
import styles from './ImportRunReport.module.css';

/** What the import started last did, step by step: read again while it goes on. */
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
            {run.log.length > 0 && (
                <Stack as="ol" gap="xs" ariaLabel={t('imports.log')}>
                    {run.log.map((line) => (
                        <Stack as="li" key={line.seq} gap="xs">
                            <Inline gap="sm" wrap>
                                {line.at !== null && (
                                    <Text as="span" variant="caption" tone="muted"><time dateTime={line.at}>{logTimeText(line.at)}</time></Text>
                                )}
                                <Text as="span"><LogEntryText entry={line.entry}/></Text>
                            </Inline>
                        </Stack>
                    ))}
                </Stack>
            )}
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

/** What a line of the log tells, leading to what it names: software, a page of the source, an author. */
function LogEntryText({entry}: {readonly entry: ImportLogEntry}) {
    const {t} = useTranslation();
    switch (entry.kind) {
        case 'list':
            return (
                <Trans
                    i18nKey={entry.page.pages === null ? 'imports.lines.listUncounted' : 'imports.lines.list'}
                    values={{number: entry.page.number, pages: entry.page.pages, programs: entry.page.programs}}
                    components={{page: <LinkTo href={entry.page.url}/>}}
                />
            );
        case 'download':
            return (
                <Trans
                    i18nKey={`imports.lines.${entry.download.file}`}
                    values={{title: entry.download.title, name: entry.download.name, size: fileSizeText(entry.download.size)}}
                    components={{program: <span/>}}
                />
            );
        case 'added':
            return <SoftwareLine program={entry.program} i18nKey="imports.lines.added"/>;
        case 'known':
            return entry.program.supplements.length === 0
                ? <SoftwareLine program={entry.program} i18nKey="imports.lines.unchanged"/>
                : (
                    <SoftwareLine
                        program={entry.program}
                        i18nKey="imports.lines.supplemented"
                        supplements={SOFTWARE_SUPPLEMENTS.filter((code) => entry.program.supplements.includes(code))
                            .map((code) => t(`imports.supplements.${code}`))
                            .join(', ')}
                    />
                );
        case 'skipped':
            return (
                <Trans
                    i18nKey={`imports.lines.skipped.${entry.program.reason}`}
                    values={{title: entry.program.title, detail: entry.program.detail}}
                    components={{program: <LinkTo href={entry.program.url}/>}}
                />
            );
        case 'failed':
            return (
                <Trans
                    i18nKey="imports.lines.failed"
                    values={{url: entry.program.url, reason: entry.program.reason}}
                    components={{page: <LinkTo href={entry.program.url}/>}}
                />
            );
        case 'author':
            return (
                <Trans
                    i18nKey={`imports.lines.${entry.author.created ? 'created' : 'found'}.${entry.author.kind}`}
                    values={{name: entry.author.exists ? entry.author.name : t('imports.deleted', {title: entry.author.name})}}
                    components={{author: entry.author.exists ? <LinkTo href={authorHref(entry.author)}/> : <span/>}}
                />
            );
        case 'place':
            return t(`imports.lines.place.${entry.place.kind}`, {name: entry.place.name});
    }
}

interface SoftwareLineProps {
    readonly program: {readonly softwareId: SoftwareId; readonly title: string; readonly exists: boolean};
    readonly i18nKey: 'imports.lines.added' | 'imports.lines.unchanged' | 'imports.lines.supplemented';
    readonly supplements?: string;
}

/** A line naming software of the archive, leading to it while it is there. */
function SoftwareLine({program, i18nKey, supplements}: SoftwareLineProps) {
    const {t} = useTranslation();
    return (
        <Trans
            i18nKey={i18nKey}
            values={{title: program.exists ? program.title : t('imports.deleted', {title: program.title}), supplements}}
            components={{program: program.exists ? <LinkTo href={softwarePath(program.softwareId)}/> : <span/>}}
        />
    );
}

/** A link a line of the log puts around the words its text marks: the text gives the words. */
function LinkTo({href, children}: {readonly href: string; readonly children?: ReactNode}) {
    return <TextLink href={href}>{children}</TextLink>;
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
