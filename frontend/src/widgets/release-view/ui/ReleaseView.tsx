import type {ReactNode} from 'react';
import {useTranslation} from 'react-i18next';
import {
    HardwareList,
    releaseDayText,
    releaseEditPath,
    type ReleaseId,
    ScreenshotsProvider,
    type SoftwareId,
    SoftwareDetailsProvider,
    softwarePath,
    SoftwareReleasesProvider,
    SoftwareScreenshots,
    useSoftwareDetails,
    useSoftwareReleases,
} from '@/entities/software';
import {NotFoundError} from '@/shared/api';
import {useSkeleton} from '@/shared/lib/loading';
import {useInViewport} from '@/shared/lib/screen';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Text, TextLink} from '@/shared/ui/typography';
import styles from './ReleaseView.module.css';

export interface ReleaseViewProps {
    readonly softwareId: SoftwareId;
    readonly releaseId: ReleaseId;
}

/** One release in the context of the software it belongs to. */
export function ReleaseView({softwareId, releaseId}: ReleaseViewProps) {
    return (
        <SoftwareDetailsProvider key={`${softwareId}:${releaseId}`} id={softwareId}>
            <SoftwareReleasesProvider id={softwareId} enabled>
                <LoadedView softwareId={softwareId} releaseId={releaseId}/>
            </SoftwareReleasesProvider>
        </SoftwareDetailsProvider>
    );
}

function LoadedView({softwareId, releaseId}: ReleaseViewProps) {
    const {t, i18n} = useTranslation();
    const software = useSoftwareDetails();
    const releases = useSoftwareReleases();
    const softwareSkeleton = useSkeleton(software);
    const releasesSkeleton = useSkeleton(releases);

    if (softwareSkeleton || releasesSkeleton || software.isPending || releases.isPending) {
        return <ReleaseViewSkeleton/>;
    }
    if (software.isLoadingError || releases.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    const release = releases.data.find((item) => item.id === releaseId);
    if (release === undefined) {
        throw new NotFoundError('release.not-found');
    }
    const lines = release.description?.split('\n').filter((line) => line.trim() !== '') ?? [];
    return (
        <PageLayout
            title={release.title}
            actions={release.canChange && <Button href={releaseEditPath(softwareId, releaseId)} color="secondary">{t('actions.edit')}</Button>}
        >
            <Stack gap="xl">
                <Stack gap="lg">
                    <Section label={t('release.software')}>
                        <Text><TextLink href={softwarePath(softwareId)}>{software.data.title}</TextLink></Text>
                    </Section>
                    {release.releaseDate !== null && (
                        <Section label={t('software.released')}>
                            <Text>{releaseDayText(release.releaseDate, i18n.resolvedLanguage ?? 'en')}</Text>
                        </Section>
                    )}
                    {release.version !== null && <Section label={t('fields.version')}><Text>{release.version}</Text></Section>}
                    <Section label={t('fields.type')}><Text>{t(`release.types.${release.type}`)}</Text></Section>
                    {release.hardware.length > 0 && (
                        <Stack as="section" gap="xs" ariaLabel={t('fields.hardware')}>
                            <Text variant="label" tone="muted">{t('fields.hardware')}</Text>
                            <HardwareList hardware={release.hardware}/>
                        </Stack>
                    )}
                    {lines.length > 0 && (
                        <Stack gap="sm">
                            {lines.map((line, index) => <Text key={index}>{line}</Text>)}
                        </Stack>
                    )}
                </Stack>
                <ReleaseScreenshots softwareId={softwareId} releaseId={releaseId} title={release.title}/>
            </Stack>
        </PageLayout>
    );
}

function Section({label, children}: {readonly label: string; readonly children: ReactNode}) {
    return (
        <Stack gap="xs">
            <Text variant="label" tone="muted">{label}</Text>
            {children}
        </Stack>
    );
}

function ReleaseScreenshots({softwareId, releaseId, title}: ReleaseViewProps & {readonly title: string}) {
    const [ref, visible] = useInViewport();
    return (
        <div ref={ref}>
            <ScreenshotsProvider softwareId={softwareId} releaseId={releaseId} enabled={visible}>
                <SoftwareScreenshots title={title}/>
            </ScreenshotsProvider>
        </div>
    );
}

function ReleaseViewSkeleton() {
    const title = <Skeleton className={styles.titleBone}/>;
    return (
        <PageLayout title={title}>
            <div data-testid="release-view-skeleton">
                <Stack gap="lg">
                    {[1, 2, 3, 4].map((section) => (
                        <Stack key={section} gap="xs">
                            <Skeleton className={styles.labelBone}/>
                            <Skeleton className={styles.lineBone}/>
                        </Stack>
                    ))}
                    <Stack gap="sm">
                        <Skeleton className={styles.textBone}/>
                        <Skeleton className={styles.textBone}/>
                    </Stack>
                </Stack>
            </div>
        </PageLayout>
    );
}
