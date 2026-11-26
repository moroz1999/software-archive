import {type ReactNode, useId} from 'react';
import {useTranslation} from 'react-i18next';
import {useSkeleton} from '@/shared/lib/loading';
import {useInViewport} from '@/shared/lib/screen';
import {CardGrid} from '@/shared/ui/card-grid';
import {Stack} from '@/shared/ui/stack';
import {Heading} from '@/shared/ui/typography';
import {softwarePath} from '../config/paths';
import type {CreditedOwner} from '../model/credited-software';
import type {Software} from '../model/software';
import {useCreditedSoftware} from '../model/credited-software-context';
import {CreditedSoftwareProvider} from '../model/CreditedSoftwareProvider';
import {OwnCreditLine} from './OwnCreditLine';
import {SoftwareCard} from './SoftwareCard';
import {SoftwareCardSkeleton} from './SoftwareCardSkeleton';

const SKELETON_CARDS = [0, 1, 2, 3] as const;

export interface CreditedSoftwareListProps {
    readonly owner: CreditedOwner;
    /** What each card holds under it, outside its link, such as voting. */
    readonly cardFooter?: ((software: Software) => ReactNode) | undefined;
}

/**
 * The software a person or a group is credited on, as cards with their roles on each: low on their page, so asked
 * for once it comes into view (docs/data-loading.md). Left out when there is none.
 */
export function CreditedSoftwareList({owner, cardFooter}: CreditedSoftwareListProps) {
    const [ref, visible] = useInViewport();
    return (
        <div ref={ref}>
            <CreditedSoftwareProvider owner={owner} enabled={visible}>
                <Cards cardFooter={cardFooter}/>
            </CreditedSoftwareProvider>
        </div>
    );
}

function Cards({cardFooter}: Pick<CreditedSoftwareListProps, 'cardFooter'>) {
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
                                <SoftwareCard software={software} href={softwarePath(software.id)} footer={cardFooter?.(software)}>
                                    {software.credits.map((credit, index) => <OwnCreditLine key={index} credit={credit}/>)}
                                </SoftwareCard>
                            </li>
                        ))}
                </CardGrid>
            </Stack>
        </section>
    );
}
