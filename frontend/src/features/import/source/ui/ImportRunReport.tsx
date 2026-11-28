import {type ReactNode, useId} from 'react';
import {softwareSupplementValues} from '@/shared/api';
import type {TFunction} from 'i18next';
import {Trans, useTranslation} from 'react-i18next';
import {authorPath} from '@/entities/authors';
import {groupPath} from '@/entities/groups';
import {labelPath} from '@/entities/labels';
import {fileSizeText, softwarePath} from '@/entities/software';
import {Chip} from '@/shared/ui/chip';
import {Inline} from '@/shared/ui/inline';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Heading, Text, TextLink} from '@/shared/ui/typography';
import {logTimeText, runTimeText} from '../lib/run-time';
import {bkCatalogSectionOf, type ImportLogLine, type ImportRun, type ImportRunAuthor, type ImportRunProgress, importSourceOf} from '../model/import-run';
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
                {run.warnings > 0 && <Text tone="strong">{t('imports.warnings', {count: run.warnings})}</Text>}
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
                                <Text as="span"><LogEntryText line={line}/></Text>
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
function LogEntryText({line}: {readonly line: ImportLogLine}) {
    const {t} = useTranslation();
    switch (line.kind) {
        case 'list':
            return (
                <Trans
                    i18nKey={line.entry.pages === null ? 'imports.lines.listUncounted' : 'imports.lines.list'}
                    values={{number: line.entry.number, pages: line.entry.pages, programs: line.entry.programs}}
                    components={{page: <LinkTo href={line.entry.url}/>}}
                />
            );
        case 'download':
            return (
                <Trans
                    i18nKey={`imports.lines.${line.entry.file}`}
                    values={{title: line.entry.title, name: line.entry.name, size: fileSizeText(line.entry.size)}}
                    components={{program: <span/>}}
                />
            );
        case 'added':
            return <SoftwareLine program={line.entry} i18nKey="imports.lines.added"/>;
        case 'known':
            return line.entry.supplements.length === 0
                ? <SoftwareLine program={line.entry} i18nKey="imports.lines.unchanged"/>
                : (
                    <SoftwareLine
                        program={line.entry}
                        i18nKey="imports.lines.supplemented"
                        supplements={softwareSupplementValues.filter((code) => line.entry.supplements.includes(code))
                            .map((code) => t(`imports.supplements.${code}`))
                            .join(', ')}
                    />
                );
        case 'skipped':
            return (
                <Trans
                    i18nKey={`imports.lines.skipped.${line.entry.reason}`}
                    values={{title: line.entry.title, detail: line.entry.detail}}
                    components={{program: <LinkTo href={line.entry.url}/>}}
                />
            );
        case 'warning':
            return (
                <Inline as="span" gap="sm" align="center" wrap>
                    <Chip size="sm">{t('imports.warning')}</Chip>
                    <span>
                        <Trans
                            i18nKey={`imports.lines.warning.${line.entry.kind}`}
                            values={{title: line.entry.title, value: line.entry.value}}
                            components={{program: <LinkTo href={line.entry.url}/>}}
                        />
                    </span>
                </Inline>
            );
        case 'failed':
            return (
                <Trans
                    i18nKey="imports.lines.failed"
                    values={{url: line.entry.url, reason: line.entry.reason}}
                    components={{page: <LinkTo href={line.entry.url}/>}}
                />
            );
        case 'author':
            return (
                <Trans
                    i18nKey={`imports.lines.${line.entry.created ? 'created' : 'found'}.${line.entry.kind}`}
                    values={{name: line.entry.exists ? line.entry.name : t('imports.deleted', {title: line.entry.name})}}
                    components={{author: line.entry.exists ? <LinkTo href={authorHref(line.entry)}/> : <span/>}}
                />
            );
        case 'place':
            return t(`imports.lines.place.${line.entry.kind}`, {name: line.entry.name});
    }
}

interface SoftwareLineProps {
    readonly program: {readonly softwareId: string; readonly title: string; readonly exists: boolean};
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
            return authorPath(author.id);
        case 'group':
            return groupPath(author.id);
        case 'label':
            return labelPath(author.id);
    }
}

/** What the run read: the archive, and for vektor-06c.ru and bk-catalog its section or the whole of it. */
function scopeText(run: ImportRun, t: TFunction): string {
    const source = importSourceOf(run.archive);
    if (source === undefined) {
        return run.archive;
    }
    const archive = t(`imports.sources.${source}`);
    switch (source) {
        case 'vektor':
            return `${archive} · ${run.section ?? t('imports.wholeSite')}`;
        case 'bkCatalog': {
            const section = run.section === null ? undefined : bkCatalogSectionOf(run.section);
            return `${archive} · ${section === undefined ? run.section ?? t('imports.wholeCatalog') : t(`imports.bkCatalogSections.${section}`)}`;
        }
        case 'basis':
            return archive;
    }
}
