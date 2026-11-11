import {Fragment, type ReactNode, useId} from 'react';
import {useTranslation} from 'react-i18next';
import {
    HardwareList,
    releaseDayText,
    releaseEditPath,
    releaseNewPath,
    type SoftwareCategory,
    type SoftwareDetails,
    SoftwareDetailsProvider,
    softwareEditPath,
    type SoftwareId,
    SoftwareReleases,
    SoftwareReleasesProvider,
    useSoftwareDetails,
} from '@/entities/software';
import {useInViewport} from '@/shared/lib/screen';
import {Button} from '@/shared/ui/button';
import {Chip} from '@/shared/ui/chip';
import {Inline} from '@/shared/ui/inline';
import {PageLayout} from '@/shared/ui/page-layout';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Heading, Text} from '@/shared/ui/typography';
import {SoftwareViewSkeleton} from './SoftwareViewSkeleton';
import styles from './SoftwareView.module.css';
import {useSkeleton} from '@/shared/lib/loading';

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
    const {t} = useTranslation();
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
        <PageLayout title={query.data.title} actions={<Button href={softwareEditPath(id)} color="secondary">{t('actions.edit')}</Button>}>
            <Stack gap="xl">
                <Details software={query.data}/>
                <Releases id={id}/>
            </Stack>
        </PageLayout>
    );
}

function Details({software}: {readonly software: SoftwareDetails}) {
    const {t, i18n} = useTranslation();
    const lines = software.description?.split('\n').filter((line) => line.trim() !== '') ?? [];
    return (
        <Stack gap="lg">
            <Section label={t('fields.platform')}><Text>{software.platform.name}</Text></Section>
            {software.releaseDate !== null && (
                <Section label={t('software.released')}><Text>{releaseDayText(software.releaseDate, i18n.resolvedLanguage ?? 'en')}</Text></Section>
            )}
            {software.hardware.length > 0 && (
                <Stack as="section" gap="xs" ariaLabel={t('fields.hardware')}>
                    <Text variant="label" tone="muted">{t('fields.hardware')}</Text>
                    <HardwareList hardware={software.hardware}/>
                </Stack>
            )}
            {software.categories.length > 0 && (
                <Stack as="section" gap="xs" ariaLabel={t('fields.categories')}>
                    <Text variant="label" tone="muted">{t('fields.categories')}</Text>
                    <Stack as="ul" gap="xs">
                        {software.categories.map((category) => <li key={category.id}><Branch category={category}/></li>)}
                    </Stack>
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

/** A category of the software with its branch from the top of the tree down to it. */
function Branch({category}: {readonly category: SoftwareCategory}) {
    return (
        <Inline as="ul" gap="xs" align="center" wrap ariaLabel={category.branch.map((step) => step.title).join(' › ')}>
            {category.branch.map((step, index) => (
                <Fragment key={step.id}>
                    {index > 0 && <li aria-hidden="true"><Text as="span" tone="muted">›</Text></li>}
                    <li><Chip>{step.title}</Chip></li>
                </Fragment>
            ))}
        </Inline>
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
    const {t} = useTranslation();
    const [ref, visible] = useInViewport();
    const headingId = useId();
    return (
        <section ref={ref} aria-labelledby={headingId}>
            <Stack gap="md">
                <Inline gap="sm" justify="between" wrap>
                    <Heading level={2} variant="title" id={headingId}>{t('release.releases')}</Heading>
                    <Button href={releaseNewPath(id)} color="secondary">{t('actions.addRelease')}</Button>
                </Inline>
                <SoftwareReleasesProvider id={id} enabled={visible}>
                    <SoftwareReleases actions={(release) => (
                        <Button href={releaseEditPath(id, release.id)} color="secondary" ariaLabel={t('release.editNamed', {title: release.title})}>{t('actions.edit')}</Button>
                    )}/>
                </SoftwareReleasesProvider>
            </Stack>
        </section>
    );
}
