import {useId} from 'react';
import {useTranslation} from 'react-i18next';
import {useSkeleton} from '@/shared/lib/loading';
import {useInViewport} from '@/shared/lib/screen';
import {Badge} from '@/shared/ui/badge';
import {CardGrid} from '@/shared/ui/card-grid';
import {CardScreenshots} from '@/shared/ui/card-screenshots';
import {Inline} from '@/shared/ui/inline';
import {Panel} from '@/shared/ui/panel';
import {Stack} from '@/shared/ui/stack';
import {Heading, Text, TextLink} from '@/shared/ui/typography';
import {releasePath, softwarePath} from '../config/paths';
import {releaseDateText} from '../lib/release-date';
import type {CreditedRelease} from '../model/credited-release';
import {useCreditedReleases} from '../model/credited-releases-context';
import type {CreditedOwner} from '../model/credited-software';
import {CreditedReleasesProvider} from '../model/CreditedReleasesProvider';
import {OwnCreditLine} from './OwnCreditLine';
import {SoftwareCardSkeleton} from './SoftwareCardSkeleton';

const SKELETON_CARDS = [0, 1, 2, 3] as const;

export interface CreditedReleaseListProps {
    readonly owner: CreditedOwner;
}

/**
 * The releases a person, a group or a label is credited on, as cards with their roles on each: low on their page, so
 * asked for once it comes into view (docs/data-loading.md). Left out when there is none.
 */
export function CreditedReleaseList({owner}: CreditedReleaseListProps) {
    const [ref, visible] = useInViewport();
    return (
        <div ref={ref}>
            <CreditedReleasesProvider owner={owner} enabled={visible}>
                <Cards/>
            </CreditedReleasesProvider>
        </div>
    );
}

function Cards() {
    const {t} = useTranslation();
    const query = useCreditedReleases();
    const skeleton = useSkeleton(query);
    const headingId = useId();

    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    const loading = skeleton || query.isPending;
    if (!loading && query.data.length === 0) {
        return null;
    }
    return (
        <section aria-labelledby={headingId}>
            <Stack gap="md">
                <Heading level={2} variant="title" id={headingId}>{t('release.releases')}</Heading>
                <CardGrid>
                    {loading
                        ? SKELETON_CARDS.map((card) => <li key={card}><SoftwareCardSkeleton/></li>)
                        : query.data.map((release) => <li key={release.id}><ReleaseCard release={release}/></li>)}
                </CardGrid>
            </Stack>
        </section>
    );
}

/** A release with the software it is of, its type, date and version, and the roles of the owner on it. */
function ReleaseCard({release}: {readonly release: CreditedRelease}) {
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
                    <Heading id={titleId} level={3} variant="title">
                        <TextLink href={releasePath(release.softwareId, release.id)}>{release.title}</TextLink>
                    </Heading>
                    {release.type !== 'unknown' && <Badge variant="secondary">{t(`release.types.${release.type}`)}</Badge>}
                </Inline>
                <Text variant="bodySm">
                    {t('release.software')}: <TextLink href={softwarePath(release.softwareId)}>{release.softwareTitle}</TextLink>
                </Text>
                {facts.length > 0 && <Text variant="bodySm" tone="muted">{facts.join(' · ')}</Text>}
                {release.credits.map((credit, index) => <OwnCreditLine key={index} credit={credit}/>)}
            </Stack>
        </Panel>
    );
}
