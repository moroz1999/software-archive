import {Fragment, type ReactNode, useId} from 'react';
import {useTranslation} from 'react-i18next';
import {authorPath, toAuthorId} from '@/entities/authors';
import {labelPath, toLabelId} from '@/entities/labels';
import {groupPath, toGroupId} from '@/entities/groups';
import {
    HardwareList,
    releaseDateText,
    releaseEditPath,
    releaseYear,
    releaseNewPath,
    type SoftwareCategory,
    type SoftwareCredit,
    type SoftwareDetails,
    SoftwareDetailsProvider,
    softwareEditPath,
    type SoftwareId,
    SoftwareReleases,
    SoftwareReleasesProvider,
    SoftwareScreenshots,
    ScreenshotsProvider,
    useSoftwareDetails,
} from '@/entities/software';
import {toCategoryId} from '@/entities/categories';
import {catalogHrefOf, softwareBreadcrumbs} from '@/features/software/catalog';
import {useInViewport} from '@/shared/lib/screen';
import {Button} from '@/shared/ui/button';
import {PopoverMenu, PopoverMenuItem} from '@/shared/ui/popover-menu';
import {Chip} from '@/shared/ui/chip';
import {Inline} from '@/shared/ui/inline';
import {AdditionNote} from '@/shared/ui/addition-note';
import {PageLayout} from '@/shared/ui/page-layout';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Heading, Text, TextLink} from '@/shared/ui/typography';
import {SoftwareViewSkeleton} from './SoftwareViewSkeleton';
import styles from './SoftwareView.module.css';
import {useSkeleton} from '@/shared/lib/loading';
import {useAbility} from '@/entities/users';

export interface SoftwareViewProps {
    readonly id: SoftwareId;
}

/** The page of one software: its title over everything known about it, its screenshots and its releases below. */
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
        <PageLayout title={query.data.title} tabTitle={`${query.data.title} (${query.data.platform.name})`} breadcrumbs={softwareBreadcrumbs(query.data, t('pages.software'))} menu={query.data.canChange ? (
                <PopoverMenu label={t('actions.menu')}>
                    <PopoverMenuItem href={softwareEditPath(id)}>{t('actions.edit')}</PopoverMenuItem>
                </PopoverMenu>
            ) : undefined}>
            <Stack gap="xl">
                <Details software={query.data}/>
                <Screenshots id={id} title={query.data.title}/>
                <Releases id={id}/>
                <AdditionNote addition={query.data.added}/>
            </Stack>
        </PageLayout>
    );
}

function Details({software}: {readonly software: SoftwareDetails}) {
    const {t} = useTranslation();
    const lines = software.description?.split('\n').filter((line) => line.trim() !== '') ?? [];
    return (
        <Stack gap="lg">
            <Section label={t('fields.platform')}><Text>{software.platform.name}</Text></Section>
            {software.releaseDate !== null && (
                <Section label={t('fields.releaseDate')}>
                    <Text>
                        <TextLink href={catalogHrefOf({year: releaseYear(software.releaseDate)})}>
                            {releaseDateText(software.releaseDate)}
                        </TextLink>
                    </Text>
                </Section>
            )}
            {software.preservation !== 'unknown' && (
                <Section label={t('fields.preservation')}><Text>{t(`software.preservation.${software.preservation}`)}</Text></Section>
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
            {(software.credits?.length ?? 0) > 0 && (
                <Section label={t('fields.credits')}>
                    <Stack as="ul" gap="xs" ariaLabel={t('fields.credits')}>
                        {software.credits?.map((credit) => (
                            <li key={`${credit.type}:${credit.id}`}>
                                <Inline gap="xs" align="center" wrap>
                                    <CreditName credit={credit}/>
                                    {/* An unknown role tells nothing to a reader. */}
                                    {credit.roles.filter((role) => role !== 'unknown').map((role) => <Chip key={role} size="sm">{t(`roles.software.${role}`)}</Chip>)}
                                </Inline>
                            </li>
                        ))}
                    </Stack>
                </Section>
            )}
            {(software.sources?.length ?? 0) > 0 && (
                <Stack as="section" gap="xs" ariaLabel={t('software.source')}>
                    <Text variant="label" tone="muted">{t('software.source')}</Text>
                    <Stack as="ul" gap="xs">
                        {software.sources?.map((source) => <li key={source.url}><TextLink href={source.url}>{source.archive}</TextLink></li>)}
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

/** The name of whoever is credited, leading to the person or the group, a label to its own page. */
function CreditName({credit}: {readonly credit: SoftwareCredit}) {
    const href = creditHref(credit);
    return <Text>{href === null ? credit.title : <TextLink href={href}>{credit.title}</TextLink>}</Text>;
}

function creditHref(credit: SoftwareCredit): string | null {
    if (credit.type === 'author-label' || credit.type === 'group-label') {
        return labelPath(toLabelId(credit.id));
    }
    if (credit.ownerId === undefined || credit.ownerId === null) {
        return null;
    }
    return credit.type === 'author' ? authorPath(toAuthorId(credit.ownerId)) : groupPath(toGroupId(credit.ownerId));
}

/** A category of the software with its branch from the top of the tree down to it, each step leading to its catalog. */
function Branch({category}: {readonly category: SoftwareCategory}) {
    return (
        <Inline as="ul" gap="xs" align="center" wrap ariaLabel={category.branch.map((step) => step.title).join(' › ')}>
            {category.branch.map((step, index) => (
                <Fragment key={step.id}>
                    {index > 0 && <li aria-hidden="true"><Text as="span" tone="muted">›</Text></li>}
                    <li><Chip href={catalogHrefOf({category: toCategoryId(step.id)})}>{step.title}</Chip></li>
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

/** The screenshots come below the details: they are asked for once the block comes into view (docs/data-loading.md). */
function Screenshots({id, title}: SoftwareViewProps & {readonly title: string}) {
    const [ref, visible] = useInViewport();
    return (
        <ScreenshotsProvider softwareId={id} enabled={visible}>
            <SoftwareScreenshots title={title} viewportRef={ref}/>
        </ScreenshotsProvider>
    );
}

/** The releases are lower on the page: they are asked for once the block comes into view (docs/data-loading.md). */
function Releases({id}: SoftwareViewProps) {
    const {t} = useTranslation();
    const addsEntries = useAbility('add-entries');
    const [ref, visible] = useInViewport();
    const headingId = useId();
    return (
        <section ref={ref} aria-labelledby={headingId}>
            <Stack gap="md">
                <Inline gap="sm" justify="between" wrap>
                    <Heading level={2} variant="title" id={headingId}>{t('release.releases')}</Heading>
                    {addsEntries && <Button href={releaseNewPath(id)} color="secondary">{t('actions.addRelease')}</Button>}
                </Inline>
                <SoftwareReleasesProvider id={id} enabled={visible}>
                    <SoftwareReleases softwareId={id} actions={(release) => release.canChange && (
                        <Button href={releaseEditPath(id, release.id)} color="secondary" ariaLabel={t('release.editNamed', {title: release.title})}>{t('actions.edit')}</Button>
                    )}/>
                </SoftwareReleasesProvider>
            </Stack>
        </section>
    );
}
