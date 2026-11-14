import {useId} from 'react';
import {useTranslation} from 'react-i18next';
import {useSkeleton} from '@/shared/lib/loading';
import {useInViewport} from '@/shared/lib/screen';
import {CardGrid} from '@/shared/ui/card-grid';
import {Chip} from '@/shared/ui/chip';
import {Inline} from '@/shared/ui/inline';
import {Stack} from '@/shared/ui/stack';
import {Heading, Text} from '@/shared/ui/typography';
import {softwarePath} from '../config/paths';
import type {CreditedOwner, OwnCredit} from '../model/credited-software';
import {useCreditedSoftware} from '../model/credited-software-context';
import {CreditedSoftwareProvider} from '../model/CreditedSoftwareProvider';
import {SoftwareCard} from './SoftwareCard';
import {SoftwareCardSkeleton} from './SoftwareCardSkeleton';

const SKELETON_CARDS = [0, 1, 2, 3] as const;

export interface CreditedSoftwareListProps {
    readonly owner: CreditedOwner;
}

/**
 * The software a person or a group is credited on, as cards with their roles on each: low on their page, so asked
 * for once it comes into view (docs/data-loading.md). Left out when there is none.
 */
export function CreditedSoftwareList({owner}: CreditedSoftwareListProps) {
    const [ref, visible] = useInViewport();
    return (
        <div ref={ref}>
            <CreditedSoftwareProvider owner={owner} enabled={visible}>
                <Cards/>
            </CreditedSoftwareProvider>
        </div>
    );
}

function Cards() {
    const {t} = useTranslation();
    const query = useCreditedSoftware();
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
                <Heading level={2} variant="title" id={headingId}>{t('software.software')}</Heading>
                <CardGrid>
                    {loading
                        ? SKELETON_CARDS.map((card) => <li key={card}><SoftwareCardSkeleton/></li>)
                        : query.data.map((software) => (
                            <li key={software.id}>
                                <SoftwareCard software={software} href={softwarePath(software.id)}>
                                    {software.credits.map((credit, index) => <Credit key={index} credit={credit}/>)}
                                </SoftwareCard>
                            </li>
                        ))}
                </CardGrid>
            </Stack>
        </section>
    );
}

/** The roles of one credit, an unknown one left out, after the label it was under when that is not the main name. */
function Credit({credit}: {readonly credit: OwnCredit}) {
    const {t} = useTranslation();
    const roles = credit.roles.filter((role) => role !== 'unknown');
    if (credit.as === null && roles.length === 0) {
        return null;
    }
    return (
        <Inline gap="xs" align="center" wrap>
            {credit.as !== null && <Text variant="bodySm" tone="muted">{t('software.creditedAs', {name: credit.as})}</Text>}
            {roles.map((role) => <Chip key={role} size="sm">{t(`roles.software.${role}`)}</Chip>)}
        </Inline>
    );
}
