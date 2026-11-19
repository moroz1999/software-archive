import {useId} from 'react';
import type {TFunction} from 'i18next';
import {useTranslation} from 'react-i18next';
import {softwarePath} from '@/entities/software';
import {Chip} from '@/shared/ui/chip';
import {Inline} from '@/shared/ui/inline';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Heading, Text, TextLink} from '@/shared/ui/typography';
import {runTimeText} from '../lib/run-time';
import {type ImportRun, importSourceOf} from '../model/import-run';
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
            </Stack>
            {run.addedPrograms.length > 0 && (
                <Stack as="section" gap="xs" ariaLabel={t('imports.addedPrograms')}>
                    <Text variant="label" tone="muted">{t('imports.addedPrograms')}</Text>
                    <Stack as="ul" gap="xs">
                        {run.addedPrograms.map((program) => (
                            <li key={program.externalId}>
                                {program.exists
                                    ? <TextLink href={softwarePath(program.softwareId)}>{program.title}</TextLink>
                                    : <Text as="span" tone="muted">{t('imports.deleted', {title: program.title})}</Text>}
                            </li>
                        ))}
                    </Stack>
                </Stack>
            )}
            {run.failedPrograms.length > 0 && (
                <Stack as="section" gap="xs" ariaLabel={t('imports.failedPrograms')}>
                    <Text variant="label" tone="muted">{t('imports.failedPrograms')}</Text>
                    <Stack as="ul" gap="sm">
                        {run.failedPrograms.map((program) => (
                            <li key={program.externalId}>
                                <Stack gap="xs">
                                    <TextLink href={program.url}>{program.url}</TextLink>
                                    <Text variant="caption" tone="danger">{program.reason}</Text>
                                </Stack>
                            </li>
                        ))}
                    </Stack>
                </Stack>
            )}
        </Stack>
    );
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
