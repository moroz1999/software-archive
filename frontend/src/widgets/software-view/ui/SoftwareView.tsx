import {
    releaseDayText,
    type SoftwareDetails,
    SoftwareDetailsProvider,
    softwareEditPath,
    type SoftwareId,
    useSoftwareDetails,
} from '@/entities/software';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Text} from '@/shared/ui/typography';
import {SoftwareViewSkeleton} from './SoftwareViewSkeleton';
import styles from './SoftwareView.module.css';

export interface SoftwareViewProps {
    readonly id: SoftwareId;
}

/** The page of one software: its title over everything known about it. */
export function SoftwareView({id}: SoftwareViewProps) {
    return (
        <SoftwareDetailsProvider key={id} id={id}>
            <LoadedView id={id}/>
        </SoftwareDetailsProvider>
    );
}

function LoadedView({id}: SoftwareViewProps) {
    const query = useSoftwareDetails();

    if (query.isPending) {
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
            <Details software={query.data}/>
        </PageLayout>
    );
}

function Details({software}: {readonly software: SoftwareDetails}) {
    const lines = software.description?.split('\n').filter((line) => line.trim() !== '') ?? [];
    return (
        <Stack gap="lg">
            {software.releaseDate !== null && (
                <Stack gap="xs">
                    <Text variant="label" tone="muted">Released</Text>
                    <Text>{releaseDayText(software.releaseDate)}</Text>
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
