import type {ReactNode} from 'react';
import {useTranslation} from 'react-i18next';
import {type AuthorDetails, AuthorDetailsProvider, authorEditPath, type AuthorId, AUTHORS_PATH, useAuthorDetails} from '@/entities/authors';
import {locationText, primaryFirst} from '@/entities/locations';
import {groupPath, toGroupId} from '@/entities/groups';
import {labelPath, toLabelId} from '@/entities/labels';
import {CreditedReleaseList, CreditedSoftwareList} from '@/entities/software';
import {SoftwareCardVote} from '@/features/software/vote';
import {PopoverMenu, PopoverMenuItem} from '@/shared/ui/popover-menu';
import {Inline} from '@/shared/ui/inline';
import {AdditionNote} from '@/shared/ui/addition-note';
import {PageLayout} from '@/shared/ui/page-layout';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Text, TextLink} from '@/shared/ui/typography';
import {AuthorViewSkeleton} from './AuthorViewSkeleton';
import styles from './AuthorView.module.css';
import {useSkeleton} from '@/shared/lib/loading';

export interface AuthorViewProps {
    readonly id: AuthorId;
}

/** The page of one author: the main name over everything known about them. */
export function AuthorView({id}: AuthorViewProps) {
    return (
        <AuthorDetailsProvider key={id} id={id}>
            <LoadedView/>
        </AuthorDetailsProvider>
    );
}

function LoadedView() {
    const {t} = useTranslation();
    const query = useAuthorDetails();
    const skeleton = useSkeleton(query);

    if (skeleton || query.isPending) {
        return (
            <PageLayout title={<Skeleton className={styles.titleBone}/>}>
                <div data-testid="author-view-skeleton"><AuthorViewSkeleton/></div>
            </PageLayout>
        );
    }
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    return (
        <PageLayout title={query.data.name} breadcrumbs={[{label: t('pages.authors'), href: AUTHORS_PATH}, {label: query.data.name}]} menu={query.data.canChange ? (
                <PopoverMenu label={t('actions.menu')}>
                    <PopoverMenuItem href={authorEditPath(query.data.id)}>{t('actions.edit')}</PopoverMenuItem>
                </PopoverMenu>
            ) : undefined}>
            <Stack gap="xl">
                <Details author={query.data}/>
                <CreditedSoftwareList owner={{kind: 'authors', id: query.data.id}} cardFooter={(software) => <SoftwareCardVote software={software}/>}/>
                <CreditedReleaseList owner={{kind: 'authors', id: query.data.id}}/>
                <AdditionNote addition={query.data.added}/>
            </Stack>
        </PageLayout>
    );
}

function Details({author}: {readonly author: AuthorDetails}) {
    const {t} = useTranslation();
    const notes = author.notes?.split('\n').filter((line) => line.trim() !== '') ?? [];
    return (
        <Stack gap="lg">
            {author.realName !== null && (
                <Section label={t('author.realName')}><Text>{author.realName}</Text></Section>
            )}
            {author.labels.length > 0 && (
                <Section label={t('author.labels')}>
                    <ItemList label={t('author.labels')}>
                        {author.labels.map((label) => <li key={label.id}><Text><TextLink href={labelPath(toLabelId(label.id))}>{label.name}</TextLink></Text></li>)}
                    </ItemList>
                </Section>
            )}
            {author.locations.length > 0 && (
                <Section label={t('author.locations')}>
                    <ItemList label={t('author.locations')}>
                        {primaryFirst(author.locations).map((location, index) => <li key={index}><Text>{locationText(location)}</Text></li>)}
                    </ItemList>
                </Section>
            )}
            {author.groups.length > 0 && (
                <Section label={t('author.groups')}>
                    <ItemList label={t('author.groups')}>
                        {author.groups.map((group) => (
                            <li key={`${group.id}-${group.as ?? ''}`}>
                                <Inline gap="sm" align="baseline" wrap>
                                    <Text><TextLink href={groupPath(toGroupId(group.id))}>{group.name}</TextLink></Text>
                                    {group.as !== null && <>{' '}<Text variant="bodySm" tone="muted">{t('author.as', {name: group.as})}</Text></>}
                                </Inline>
                            </li>
                        ))}
                    </ItemList>
                </Section>
            )}
            {notes.length > 0 && (
                <Section label={t('author.notes')}>
                    <Stack gap="sm">
                        {notes.map((line, index) => <Text key={index}>{line}</Text>)}
                    </Stack>
                </Section>
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

function ItemList({label, children}: {readonly label: string; readonly children: ReactNode}) {
    return <Stack as="ul" gap="xs" ariaLabel={label}>{children}</Stack>;
}
