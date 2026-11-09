import type {ReactNode} from 'react';
import {type AuthorDetails, AuthorDetailsProvider, type AuthorId, useAuthorDetails} from '@/entities/authors';
import {locationText, primaryFirst} from '@/entities/locations';
import {groupPath, toGroupId} from '@/entities/groups';
import {Inline} from '@/shared/ui/inline';
import {PageLayout} from '@/shared/ui/page-layout';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Text, TextLink} from '@/shared/ui/typography';
import {AuthorViewSkeleton} from './AuthorViewSkeleton';
import styles from './AuthorView.module.css';

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
    const query = useAuthorDetails();

    if (query.isPending) {
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
        <PageLayout title={query.data.name}>
            <Details author={query.data}/>
        </PageLayout>
    );
}

function Details({author}: {readonly author: AuthorDetails}) {
    const notes = author.notes?.split('\n').filter((line) => line.trim() !== '') ?? [];
    return (
        <Stack gap="lg">
            {author.realName !== null && (
                <Section label="Real name"><Text>{author.realName}</Text></Section>
            )}
            {author.labels.length > 0 && (
                <Section label="Labels">
                    <ItemList label="Labels">
                        {author.labels.map((label) => <li key={label.id}><Text>{label.name}</Text></li>)}
                    </ItemList>
                </Section>
            )}
            {author.locations.length > 0 && (
                <Section label="Locations">
                    <ItemList label="Locations">
                        {primaryFirst(author.locations).map((location, index) => <li key={index}><Text>{locationText(location)}</Text></li>)}
                    </ItemList>
                </Section>
            )}
            {author.groups.length > 0 && (
                <Section label="Groups">
                    <ItemList label="Groups">
                        {author.groups.map((group) => (
                            <li key={`${group.id}-${group.as ?? ''}`}>
                                <Inline gap="sm" align="baseline" wrap>
                                    <Text><TextLink href={groupPath(toGroupId(group.id))}>{group.name}</TextLink></Text>
                                    {group.as !== null && <>{' '}<Text variant="bodySm" tone="muted">as {group.as}</Text></>}
                                </Inline>
                            </li>
                        ))}
                    </ItemList>
                </Section>
            )}
            {notes.length > 0 && (
                <Section label="Notes">
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
