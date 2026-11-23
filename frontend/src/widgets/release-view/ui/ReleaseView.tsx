import type {ReactNode} from 'react';
import {useTranslation} from 'react-i18next';
import {
    HardwareList,
    fileSizeText,
    releaseDateText,
    releaseEditPath,
    type ReleaseFile,
    ReleaseFilesProvider,
    type ReleaseId,
    ScreenshotsProvider,
    type SoftwareId,
    SoftwareDetailsProvider,
    softwarePath,
    SoftwareReleasesProvider,
    SoftwareScreenshots,
    useSoftwareDetails,
    useReleaseFiles,
    useSoftwareReleases,
} from '@/entities/software';
import {ReparseReleaseFilesButton} from '@/features/release/reparse-files';
import {softwareBreadcrumbs} from '@/features/software/catalog';
import {EditScreenshotsProvider, MoveScreenshotButtons, ScreenshotsChangeError} from '@/features/software/screenshots';
import {NotFoundError} from '@/shared/api';
import {useSkeleton} from '@/shared/lib/loading';
import {useInViewport} from '@/shared/lib/screen';
import {Button} from '@/shared/ui/button';
import {PopoverMenu, PopoverMenuItem} from '@/shared/ui/popover-menu';
import {Badge} from '@/shared/ui/badge';
import {AdditionNote} from '@/shared/ui/addition-note';
import {Inline} from '@/shared/ui/inline';
import {PageLayout} from '@/shared/ui/page-layout';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Text, TextLink} from '@/shared/ui/typography';
import {Tree, type TreeItem} from '@/shared/ui/tree';
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
    const {t} = useTranslation();
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
        <ReleaseFilesProvider softwareId={softwareId} releaseId={releaseId} enabled={release.archive != null}>
            <PageLayout
                title={release.title}
                tabTitle={`${release.title} (${software.data.platform.name})`}
                breadcrumbs={[...softwareBreadcrumbs(software.data, t('pages.software')), {label: release.title}]}
                menu={release.canChange ? (
                    <PopoverMenu label={t('actions.menu')}>
                        <PopoverMenuItem href={releaseEditPath(softwareId, releaseId)}>{t('actions.edit')}</PopoverMenuItem>
                    </PopoverMenu>
                ) : undefined}
            >
                <Stack gap="xl">
                <Stack gap="lg">
                    <Section label={t('release.software')}>
                        <Text><TextLink href={softwarePath(softwareId)}>{software.data.title}</TextLink></Text>
                    </Section>
                    {release.releaseDate !== null && (
                        <Section label={t('fields.releaseDate')}>
                            <Text>{releaseDateText(release.releaseDate)}</Text>
                        </Section>
                    )}
                    {release.version !== null && <Section label={t('fields.version')}><Text>{release.version}</Text></Section>}
                    <Section label={t('fields.type')}><Text>{t(`release.types.${release.type}`)}</Text></Section>
                    {release.archive != null && (
                        <>
                            <Section label={t('release.archive')}>
                                <Stack gap="xs" align="start">
                                    <Text>{release.archive.fileName}</Text>
                                    <Text variant="bodySm" tone="muted">{fileSizeText(release.archive.size)}</Text>
                                    <Button href={release.archive.url} target="_self" color="primary">{t('release.downloadArchive')}</Button>
                                </Stack>
                            </Section>
                            <ReleaseFileStructure softwareId={softwareId} releaseId={releaseId} canChange={release.canChange}/>
                        </>
                    )}
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
                    <ReleaseScreenshots softwareId={softwareId} releaseId={releaseId} title={release.title} canChange={release.canChange}/>
                    <AdditionNote addition={release.added}/>
                </Stack>
            </PageLayout>
        </ReleaseFilesProvider>
    );
}

function ReleaseFileStructure({softwareId, releaseId, canChange}: ReleaseViewProps & {readonly canChange: boolean}) {
    const {t} = useTranslation();
    const files = useReleaseFiles();
    const skeleton = useSkeleton(files);
    if (skeleton || files.isPending) {
        return <Skeleton className={styles.lineBone}/>;
    }
    if (files.isLoadingError) {
        return null;
    }
    return (
        <Section label={t('release.fileStructure')}>
            <ReleaseFileTree root={files.data}/>
            {canChange && <ReparseReleaseFilesButton softwareId={softwareId} releaseId={releaseId}/>}
        </Section>
    );
}

function ReleaseFileTree({root}: {readonly root: ReleaseFile | null}) {
    const {t} = useTranslation();
    if (root === null) {
        return <Text tone="muted">{t('release.structurePending')}</Text>;
    }
    if (root.unreadable) {
        return <Text tone="muted">{t('release.fileUnreadable')}</Text>;
    }
    const item = toTreeItem(root, t('release.executable'));
    return (
        <Tree
            items={[item]}
            ariaLabel={t('release.fileStructure')}
            expandLabel={(name) => t('release.expandFile', {name})}
            collapseLabel={(name) => t('release.collapseFile', {name})}
            initiallyExpanded={[item.id]}
        />
    );
}

function toTreeItem(file: ReleaseFile, executableLabel: string): TreeItem {
    return {
        id: file.id,
        name: file.name,
        content: (
            <Inline gap="xs" align="center">
                <Text>{file.name}</Text>
                {file.executable && <Badge variant="secondary">{executableLabel}</Badge>}
            </Inline>
        ),
        children: file.children.map((child) => toTreeItem(child, executableLabel)),
    };
}

function Section({label, children}: {readonly label: string; readonly children: ReactNode}) {
    return (
        <Stack gap="xs">
            <Text variant="label" tone="muted">{label}</Text>
            {children}
        </Stack>
    );
}

/** Whoever may change the release moves its screenshots right on their thumbnails. */
function ReleaseScreenshots({softwareId, releaseId, title, canChange}: ReleaseViewProps & {readonly title: string; readonly canChange: boolean}) {
    const [ref, visible] = useInViewport();
    return (
        <ScreenshotsProvider softwareId={softwareId} releaseId={releaseId} enabled={visible}>
            {canChange ? (
                <EditScreenshotsProvider softwareId={softwareId} releaseId={releaseId} enabled={visible}>
                    <SoftwareScreenshots title={title} viewportRef={ref} controls={(index) => <MoveScreenshotButtons index={index}/>} note={<ScreenshotsChangeError/>}/>
                </EditScreenshotsProvider>
            ) : <SoftwareScreenshots title={title} viewportRef={ref}/>}
        </ScreenshotsProvider>
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
