import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Heading, Text} from '@/shared/ui/typography';
import {releaseDayText} from '../lib/release-day';
import {releaseTypeText} from '../lib/release-type';
import type {SoftwareRelease} from '../model/release';
import {useSoftwareReleases} from '../model/software-releases-context';
import {HardwareList} from './HardwareList';
import styles from './SoftwareReleases.module.css';
import {useSkeleton} from '@/shared/lib/skeleton';

/** The releases of the software inside a SoftwareReleasesProvider, each with the hardware it shows. */
export function SoftwareReleases() {
    const query = useSoftwareReleases();
    const skeleton = useSkeleton(query);

    if (skeleton || query.isPending) {
        return <div data-testid="software-releases-skeleton"><Skeleton className={styles.releaseBone}/></div>;
    }
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    if (query.data.length === 0) {
        return <Text tone="muted">No releases known yet.</Text>;
    }
    return (
        <Stack gap="lg">
            {query.data.map((release) => <Release key={release.id} release={release}/>)}
        </Stack>
    );
}

function Release({release}: {readonly release: SoftwareRelease}) {
    const facts = [
        release.version === null ? null : `Version ${release.version}`,
        releaseTypeText(release.type),
        release.releaseDate === null ? null : releaseDayText(release.releaseDate),
    ].filter((fact) => fact !== null);
    return (
        <article aria-label={release.title}>
            <Stack gap="sm">
                <Heading level={3} variant="title">{release.title}</Heading>
                <Text variant="bodySm" tone="muted">{facts.join(' · ')}</Text>
                {release.description !== null && <Text>{release.description}</Text>}
                <HardwareList hardware={release.hardware}/>
            </Stack>
        </article>
    );
}
