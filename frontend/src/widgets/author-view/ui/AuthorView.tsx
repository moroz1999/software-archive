import type {ReactNode} from 'react';
import {type AuthorDetails, AuthorDetailsProvider, type AuthorId, type AuthorLocation, useAuthorDetails} from '@/entities/authors';
import {PageLayout} from '@/shared/ui/page-layout';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Text} from '@/shared/ui/typography';
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

function locationText(location: AuthorLocation): string {
    return [location.city, location.country].filter((part) => part !== null).join(', ');
}

/** The primary location leads; the others keep their order. */
function primaryFirst(locations: readonly AuthorLocation[]): readonly AuthorLocation[] {
    return [...locations.filter((location) => location.primary), ...locations.filter((location) => !location.primary)];
}

function Details({author}: {readonly author: AuthorDetails}) {
    const notes = author.notes?.split('\n').filter((line) => line.trim() !== '') ?? [];
    return (
        <Stack gap="lg">
            {author.realName !== null && (
                <Section label="Real name"><Text>{author.realName}</Text></Section>
            )}
            {author.aliases.length > 0 && (
                <Section label="Aliases">
                    <NamedList label="Aliases" items={author.aliases.map((alias) => ({key: alias.id, text: alias.name}))}/>
                </Section>
            )}
            {author.locations.length > 0 && (
                <Section label="Locations">
                    <NamedList
                        label="Locations"
                        items={primaryFirst(author.locations).map((location, index) => ({key: String(index), text: locationText(location)}))}
                    />
                </Section>
            )}
            {author.groups.length > 0 && (
                <Section label="Groups">
                    <NamedList label="Groups" items={author.groups.map((group) => ({key: group.id, text: group.name}))}/>
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

interface NamedItem {
    readonly key: string;
    readonly text: string;
}

function NamedList({label, items}: {readonly label: string; readonly items: readonly NamedItem[]}) {
    return (
        <Stack as="ul" gap="xs" ariaLabel={label}>
            {items.map((item) => <li key={item.key}><Text>{item.text}</Text></li>)}
        </Stack>
    );
}
