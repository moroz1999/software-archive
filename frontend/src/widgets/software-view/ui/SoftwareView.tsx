import {type ReactNode, useId} from 'react';
import {
    HardwareList,
    releaseDayText,
    type SoftwareDetails,
    SoftwareDetailsProvider,
    softwareEditPath,
    type SoftwareId,
    SoftwareReleases,
    SoftwareReleasesProvider,
    useSoftwareDetails,
} from '@/entities/software';
import {useInViewport} from '@/shared/lib/in-viewport';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Heading, Text} from '@/shared/ui/typography';
import {SoftwareViewSkeleton} from './SoftwareViewSkeleton';
import styles from './SoftwareView.module.css';
import {useSkeleton} from '@/shared/lib/skeleton';

export interface SoftwareViewProps {
    readonly id: SoftwareId;
}

/** The page of one software: its title over everything known about it, its releases below. */
export function SoftwareView({id}: SoftwareViewProps) {
    return (
        <SoftwareDetailsProvider key={id} id={id}>
            <LoadedView id={id}/>
        </SoftwareDetailsProvider>
    );
}

function LoadedView({id}: SoftwareViewProps) {
    const query = useSoftwareDetails();
    const skeleton = useSkeleton(query);

    if (skeleton || query.isPending) {
        return (
            <PageLayout title={<Skeleton className={styles.titleBone}/>}>
                <div data-testid="software-view-skeleton"><SoftwareViewSkeleton/></div>
            </PageLayout>
        );
    }
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    return (
        <PageLayout title={query.data.title} actions={<Button href={softwareEditPath(id)} color="secondary">Edit</Button>}>
            <Stack gap="xl">
                <Details software={query.data}/>
                <Releases id={id}/>
            </Stack>
        </PageLayout>
    );
}

function Details({software}: {readonly software: SoftwareDetails}) {
    const lines = software.description?.split('\n').filter((line) => line.trim() !== '') ?? [];
    return (
        <Stack gap="lg">
            <Section label="Platform"><Text>{software.platform.name}</Text></Section>
            {software.releaseDate !== null && (
                <Section label="Released"><Text>{releaseDayText(software.releaseDate)}</Text></Section>
            )}
            {software.hardware.length > 0 && (
                <Stack as="section" gap="xs" ariaLabel="Hardware">
                    <Text variant="label" tone="muted">Hardware</Text>
                    <HardwareList hardware={software.hardware}/>
                </Stack>
            )}
            {lines.length > 0 && (
                <Stack gap="sm">
                    {lines.map((line, index) => <Text key={index}>{line}</Text>)}
                </Stack>
            )}
        </Stack>
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

/** The releases are lower on the page: they are asked for once the block comes into view (docs/data-loading.md). */
function Releases({id}: SoftwareViewProps) {
    const [ref, visible] = useInViewport();
    const headingId = useId();
    return (
        <section ref={ref} aria-labelledby={headingId}>
            <Stack gap="md">
                <Heading level={2} variant="title" id={headingId}>Releases</Heading>
                <SoftwareReleasesProvider id={id} enabled={visible}>
                    <SoftwareReleases/>
                </SoftwareReleasesProvider>
            </Stack>
        </section>
    );
}
