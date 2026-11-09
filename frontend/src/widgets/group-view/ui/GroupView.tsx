import type {ReactNode} from 'react';
import {authorPath, toAuthorId} from '@/entities/authors';
import {locationText, primaryFirst} from '@/entities/locations';
import {type GroupDetails, GroupDetailsProvider, type GroupId, groupPath, type GroupMember, type RelatedGroup, useGroupDetails} from '@/entities/groups';
import {Inline} from '@/shared/ui/inline';
import {PageLayout} from '@/shared/ui/page-layout';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Text, TextLink} from '@/shared/ui/typography';
import {GroupViewSkeleton} from './GroupViewSkeleton';
import styles from './GroupView.module.css';
import {useSkeleton} from '@/shared/lib/skeleton';

export interface GroupViewProps {
    readonly id: GroupId;
}

/** The page of one group: the main name over everything known about it. */
export function GroupView({id}: GroupViewProps) {
    return (
        <GroupDetailsProvider key={id} id={id}>
            <LoadedView/>
        </GroupDetailsProvider>
    );
}

function LoadedView() {
    const query = useGroupDetails();
    const skeleton = useSkeleton(query);

    if (skeleton || query.isPending) {
        return (
            <PageLayout title={<Skeleton className={styles.titleBone}/>}>
                <div data-testid="group-view-skeleton"><GroupViewSkeleton/></div>
            </PageLayout>
        );
    }
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    return (
        <PageLayout title={query.data.name}>
            <Details group={query.data}/>
        </PageLayout>
    );
}

function Details({group}: {readonly group: GroupDetails}) {
    const notes = group.notes?.split('\n').filter((line) => line.trim() !== '') ?? [];
    return (
        <Stack gap="lg">
            <Section label="Name"><Text>{group.groupName}</Text></Section>
            {group.types.length > 0 && (
                <Section label="Types">
                    <ItemList label="Types">{group.types.map((type) => <li key={type}><Text>{type}</Text></li>)}</ItemList>
                </Section>
            )}
            {group.labels.length > 0 && (
                <Section label="Labels">
                    <ItemList label="Labels">{group.labels.map((label) => <li key={label.id}><Text>{label.name}</Text></li>)}</ItemList>
                </Section>
            )}
            {group.locations.length > 0 && (
                <Section label="Locations">
                    <ItemList label="Locations">
                        {primaryFirst(group.locations).map((location, index) => <li key={index}><Text>{locationText(location)}</Text></li>)}
                    </ItemList>
                </Section>
            )}
            {group.members.length > 0 && (
                <Section label="Members">
                    <ItemList label="Members">
                        {group.members.map((member, index) => <li key={index}><Member member={member}/></li>)}
                    </ItemList>
                </Section>
            )}
            <RelatedGroups label="Member groups" groups={group.memberGroups}/>
            <RelatedGroups label="Member of" groups={group.groups}/>
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

/** The label a member was a member under, leading to the person behind it when known, with their main name when it is another one. */
function Member({member}: {readonly member: GroupMember}) {
    return (
        <Inline gap="sm" align="baseline" wrap>
            <Text>{member.personId === null ? member.name : <TextLink href={authorPath(toAuthorId(member.personId))}>{member.name}</TextLink>}</Text>
            {member.personName !== null && <>{' '}<Text variant="bodySm" tone="muted">{member.personName}</Text></>}
        </Inline>
    );
}

function RelatedGroups({label, groups}: {readonly label: string; readonly groups: readonly RelatedGroup[]}) {
    if (groups.length === 0) {
        return null;
    }
    return (
        <Section label={label}>
            <ItemList label={label}>
                {groups.map((group) => <li key={group.id}><Text><TextLink href={groupPath(group.id)}>{group.name}</TextLink></Text></li>)}
            </ItemList>
        </Section>
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
