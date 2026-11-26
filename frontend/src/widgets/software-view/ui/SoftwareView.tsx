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
    SoftwareCreditList,
    SoftwareReleases,
    SoftwareReleasesProvider,
    type Gallery,
    SoftwareScreenshots,
    ScreenshotsProvider,
    useSoftwareDetails,
} from '@/entities/software';
import {toCategoryId} from '@/entities/categories';
import {catalogHrefOf, softwareBreadcrumbs} from '@/features/software/catalog';
import {EditScreenshotsProvider, MoveScreenshotButtons, ScreenshotsChangeError} from '@/features/software/screenshots';
import {SoftwareRating, SoftwareRatingProvider, SoftwareVotes, SoftwareVotesProvider} from '@/features/software/vote';
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
                <SoftwareRatingProvider id={id}><SoftwareRating/></SoftwareRatingProvider>
                {GALLERIES.map((gallery) => <Screenshots key={gallery} id={id} title={query.data.title} canChange={query.data.canChange} gallery={gallery}/>)}
                <Releases id={id}/>
                <Votes id={id}/>
                <AdditionNote addition={query.data.added}/>
            </Stack>
        </PageLayout>
    );
}

function Details({software}: {readonly software: SoftwareDetails}) {
    const {t} = useTranslation();
    const lines = linesOf(software.description);
    const instructions = linesOf(software.instructions);
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
                    <SoftwareCreditList credits={software.credits ?? []} creditHref={creditHref}/>
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
            {instructions.length > 0 && (
                <Stack as="section" gap="sm" ariaLabel={t('fields.instructions')}>
                    <Text variant="label" tone="muted">{t('fields.instructions')}</Text>
                    {instructions.map((line, index) => <Text key={index}>{line}</Text>)}
                </Stack>
            )}
        </Stack>
    );
}

/** Who voted on the software, asked for once the block comes into view. */
function Votes({id}: SoftwareViewProps) {
    const [ref, visible] = useInViewport();
    return <SoftwareVotesProvider id={id} enabled={visible}><SoftwareVotes viewportRef={ref}/></SoftwareVotesProvider>;
}

/** The lines of a text that hold something; none of none. */
function linesOf(text: string | null | undefined): readonly string[] {
    return text?.split('\n').filter((line) => line.trim() !== '') ?? [];
}

/** Where a credited name leads: the person or the group, a label to its own page. */
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

/** The galleries of the software, the screenshots first (docs/domain/software.md#pictures). */
const GALLERIES: readonly Gallery[] = ['screenshots', 'maps', 'covers', 'adverts'];

/**
 * The pictures of a gallery come below the details: they are asked for once the block comes into view
 * (docs/data-loading.md). Whoever may change the software moves them right on their thumbnails.
 */
function Screenshots({id, title, canChange, gallery}: SoftwareViewProps & {readonly title: string; readonly canChange: boolean; readonly gallery: Gallery}) {
    const [ref, visible] = useInViewport();
    return (
        <ScreenshotsProvider softwareId={id} gallery={gallery} enabled={visible}>
            {canChange ? (
                <EditScreenshotsProvider softwareId={id} gallery={gallery} enabled={visible}>
                    <SoftwareScreenshots title={title} gallery={gallery} viewportRef={ref} controls={(index) => <MoveScreenshotButtons index={index}/>} note={<ScreenshotsChangeError/>}/>
                </EditScreenshotsProvider>
            ) : <SoftwareScreenshots title={title} gallery={gallery} viewportRef={ref}/>}
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
                    <SoftwareReleases softwareId={id} creditHref={creditHref} actions={(release) => release.canChange && (
                        <Button href={releaseEditPath(id, release.id)} color="secondary" ariaLabel={t('release.editNamed', {title: release.title})}>{t('actions.edit')}</Button>
                    )}/>
                </SoftwareReleasesProvider>
            </Stack>
        </section>
    );
}
