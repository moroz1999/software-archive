import type {ReactNode} from 'react';
import {useTranslation} from 'react-i18next';
import {authorPath, toAuthorId} from '@/entities/authors';
import {labelPath, toLabelId} from '@/entities/labels';
import {locationText, primaryFirst} from '@/entities/locations';
import {groupEditPath, type GroupDetails, GroupDetailsProvider, type GroupId, groupPath, type GroupMember, type RelatedGroup, useGroupDetails} from '@/entities/groups';
import {CreditedSoftwareList} from '@/entities/software';
import {Button} from '@/shared/ui/button';
import {Chip} from '@/shared/ui/chip';
import {Inline} from '@/shared/ui/inline';
import {PageLayout} from '@/shared/ui/page-layout';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Text, TextLink} from '@/shared/ui/typography';
import {GroupViewSkeleton} from './GroupViewSkeleton';
import styles from './GroupView.module.css';
import {useSkeleton} from '@/shared/lib/loading';

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
    const {t} = useTranslation();
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
        <PageLayout title={query.data.name} actions={query.data.canChange && <Button href={groupEditPath(query.data.id)} color="secondary">{t('actions.edit')}</Button>}>
            <Stack gap="xl">
                <Details group={query.data}/>
                <CreditedSoftwareList owner={{kind: 'groups', id: query.data.id}}/>
            </Stack>
        </PageLayout>
    );
}

function Details({group}: {readonly group: GroupDetails}) {
    const {t} = useTranslation();
    const notes = group.notes?.split('\n').filter((line) => line.trim() !== '') ?? [];
    return (
        <Stack gap="lg">
            {group.types.length > 0 && (
                <Section label={t('group.types')}>
                    <ItemList label={t('group.types')}>{group.types.map((type) => <li key={type}><Text>{type}</Text></li>)}</ItemList>
                </Section>
            )}
            {group.labels.length > 0 && (
                <Section label={t('group.labels')}>
                    <ItemList label={t('group.labels')}>{group.labels.map((label) => <li key={label.id}><Text><TextLink href={labelPath(toLabelId(label.id))}>{label.name}</TextLink></Text></li>)}</ItemList>
                </Section>
            )}
            {group.locations.length > 0 && (
                <Section label={t('group.locations')}>
                    <ItemList label={t('group.locations')}>
                        {primaryFirst(group.locations).map((location, index) => <li key={index}><Text>{locationText(location)}</Text></li>)}
                    </ItemList>
                </Section>
            )}
            {group.members.length > 0 && (
                <Section label={t('group.members')}>
                    <ItemList label={t('group.members')}>
                        {group.members.map((member, index) => <li key={index}><Member member={member}/></li>)}
                    </ItemList>
                </Section>
            )}
            <RelatedGroups label={t('group.memberGroups')} groups={group.memberGroups}/>
            <RelatedGroups label={t('group.memberOf')} groups={group.groups}/>
            {notes.length > 0 && (
                <Section label={t('group.notes')}>
                    <Stack gap="sm">
                        {notes.map((line, index) => <Text key={index}>{line}</Text>)}
                    </Stack>
                </Section>
            )}
        </Stack>
    );
}

/** Where a member leads: a label to its own page, a person recorded by themselves to the person. */
function memberHref(member: GroupMember): string | null {
    if (member.id === null) {
        return null;
    }
    return member.type === 'author-label' ? labelPath(toLabelId(member.id)) : authorPath(toAuthorId(member.id));
}

/** The label a member was a member under, leading to its own page, with the person's main name leading to the person when it is another one. */
function Member({member}: {readonly member: GroupMember}) {
    const {t} = useTranslation();
    const href = memberHref(member);
    const dates = [member.startDate, member.endDate].some((date) => date !== null)
        ? `${member.startDate ?? '…'} – ${member.endDate ?? '…'}`
        : null;
    return (
        <Inline gap="sm" align="baseline" wrap>
            <Text>{href === null ? member.name : <TextLink href={href}>{member.name}</TextLink>}</Text>
            {member.personName !== null && member.personId !== null && (
                <>{' '}<Text variant="bodySm" tone="muted"><TextLink href={authorPath(toAuthorId(member.personId))}>{member.personName}</TextLink></Text></>
            )}
            {/* An unknown role tells nothing to a reader. */}
            {member.roles.filter((role) => role !== 'unknown').map((role) => <Chip key={role} size="sm">{t(`roles.group.${role}`)}</Chip>)}
            {dates !== null && <Text variant="bodySm" tone="muted">{dates}</Text>}
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
