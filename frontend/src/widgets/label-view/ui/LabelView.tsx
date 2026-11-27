import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import {useNavigate} from 'react-router';
import {authorPath, AUTHORS_PATH} from '@/entities/authors';
import {groupPath} from '@/entities/groups';
import {type LabelDetails, LabelDetailsProvider, labelEditPath, type LabelOwner, labelPath, useLabelDetails} from '@/entities/labels';
import {locationText, primaryFirst} from '@/entities/locations';
import {CreditedReleaseList, CreditedSoftwareList} from '@/entities/software';
import {SoftwareCard} from '@/features/software/vote';
import {AttachLabelDialog} from '@/features/label/attach';
import {MakeAuthorDialog, MakeGroupDialog} from '@/features/label/make-owner';
import {AttachLabelToGroupDialog, MergeLabelIntoAuthorDialog, MergeLabelIntoGroupDialog, MergeLabelIntoLabelDialog} from '@/features/label/hand-over';
import {useSkeleton} from '@/shared/lib/loading';
import {Inline} from '@/shared/ui/inline';
import {AdditionNote} from '@/shared/ui/addition-note';
import {PageLayout} from '@/shared/ui/page-layout';
import {PopoverMenu, PopoverMenuItem} from '@/shared/ui/popover-menu';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Text, TextLink} from '@/shared/ui/typography';
import styles from './LabelView.module.css';

export interface LabelViewProps {
    readonly id: string;
}

/**
 * The page of a label: its name, whose it is when known, where it is located, the groups it was a member of and the software it is credited
 * on; a label of nobody waits there for a person who knows whose it is to attach it to its owner, and any label can
 * become a new person or group of its own, or be edited by whoever may.
 */
export function LabelView({id}: LabelViewProps) {
    return (
        <LabelDetailsProvider key={id} id={id}>
            <LoadedView/>
        </LabelDetailsProvider>
    );
}

function ownerPath(owner: LabelOwner): string {
    return owner.type === 'author' ? authorPath(owner.id) : groupPath(owner.id);
}

function LoadedView() {
    const {t} = useTranslation();
    const navigate = useNavigate();
    const query = useLabelDetails();
    const skeleton = useSkeleton(query);
    // Which window the menu opened is UI state: the menu closes while the window stays.
    const [dialog, setDialog] = useState<'attach' | 'author' | 'group' | 'merge' | 'give' | 'merge-author' | 'merge-label' | null>(null);

    if (skeleton || query.isPending) {
        return (
            <PageLayout title={<Skeleton className={styles.titleBone}/>}>
                <div data-testid="label-view-skeleton">
                    <Stack gap="xs">
                        <Skeleton className={styles.lineBone}/>
                        <Skeleton className={styles.lineBone}/>
                    </Stack>
                </div>
            </PageLayout>
        );
    }
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    const label = query.data;
    const openOwner = (owner: LabelOwner) => {
        void navigate(ownerPath(owner));
    };
    const openGroup = (group: string) => {
        void navigate(groupPath(group));
    };
    const openAuthor = (author: string) => {
        void navigate(authorPath(author));
    };
    const openLabel = (other: string) => {
        void navigate(labelPath(other));
    };
    const close = () => {
        setDialog(null);
    };
    return (
        <PageLayout
            title={label.name}
            breadcrumbs={[{label: t('pages.authors'), href: AUTHORS_PATH}, {label: label.name}]}
            menu={label.canEdit || label.canChange || label.canMakeOwner || label.canHandOver ? (
                <PopoverMenu label={t('actions.menu')}>
                    {label.canEdit && <PopoverMenuItem href={labelEditPath(label.id)}>{t('actions.edit')}</PopoverMenuItem>}
                    {label.canChange && <PopoverMenuItem onClick={() => { setDialog('attach'); }}>{t('label.attach')}</PopoverMenuItem>}
                    {label.canMakeOwner && (
                        <>
                            <PopoverMenuItem onClick={() => { setDialog('author'); }}>{t('label.makeAuthor')}</PopoverMenuItem>
                            <PopoverMenuItem onClick={() => { setDialog('group'); }}>{t('label.makeGroup')}</PopoverMenuItem>
                        </>
                    )}
                    {label.canHandOver && (
                        <>
                            <PopoverMenuItem onClick={() => { setDialog('give'); }}>{t('label.giveToGroup')}</PopoverMenuItem>
                            <PopoverMenuItem onClick={() => { setDialog('merge'); }}>{t('label.merge')}</PopoverMenuItem>
                            <PopoverMenuItem onClick={() => { setDialog('merge-author'); }}>{t('label.mergeIntoAuthor')}</PopoverMenuItem>
                            <PopoverMenuItem onClick={() => { setDialog('merge-label'); }}>{t('label.mergeIntoLabel')}</PopoverMenuItem>
                        </>
                    )}
                </PopoverMenu>
            ) : undefined}
        >
            <Stack gap="xl">
                <Details label={label}/>
                <CreditedSoftwareList owner={{kind: 'labels', id: label.id}} Card={SoftwareCard}/>
                <CreditedReleaseList owner={{kind: 'labels', id: label.id}}/>
                <AdditionNote addition={label.added}/>
            </Stack>
            {label.canChange && <AttachLabelDialog label={{id: label.id, name: label.name}} open={dialog === 'attach'} onClose={close} onAttached={openOwner}/>}
            {label.canMakeOwner && (
                <>
                    <MakeAuthorDialog label={label} open={dialog === 'author'} onClose={close} onMade={openOwner}/>
                    <MakeGroupDialog label={label} open={dialog === 'group'} onClose={close} onMade={openOwner}/>
                </>
            )}
            {label.canHandOver && (
                <>
                    <AttachLabelToGroupDialog label={{id: label.id, name: label.name}} open={dialog === 'give'} onClose={close} onAttached={openGroup}/>
                    <MergeLabelIntoGroupDialog label={{id: label.id, name: label.name}} open={dialog === 'merge'} onClose={close} onMerged={openGroup}/>
                    <MergeLabelIntoAuthorDialog label={{id: label.id, name: label.name}} open={dialog === 'merge-author'} onClose={close} onMerged={openAuthor}/>
                    <MergeLabelIntoLabelDialog label={{id: label.id, name: label.name}} open={dialog === 'merge-label'} onClose={close} onMerged={openLabel}/>
                </>
            )}
        </PageLayout>
    );
}

function Details({label}: {readonly label: LabelDetails}) {
    const {t} = useTranslation();
    return (
        <Stack gap="lg">
            {label.owner === null
                ? <Text tone="muted">{t('label.ownerUnknown')}</Text>
                : (
                    <Stack gap="xs">
                        <Text variant="label" tone="muted">{t('label.owner')}</Text>
                        <Inline gap="sm" align="baseline" wrap>
                            <Text><TextLink href={ownerPath(label.owner)}>{label.owner.name}</TextLink></Text>
                            {label.owner.note !== null && <>{' '}<Text variant="bodySm" tone="muted">{label.owner.note}</Text></>}
                        </Inline>
                    </Stack>
                )}
            {label.locations.length > 0 && (
                <Stack gap="xs">
                    <Text variant="label" tone="muted">{t('label.locations')}</Text>
                    <Stack as="ul" gap="xs" ariaLabel={t('label.locations')}>
                        {primaryFirst(label.locations).map((location, index) => <li key={index}><Text>{locationText(location)}</Text></li>)}
                    </Stack>
                </Stack>
            )}
            {label.groups.length > 0 && (
                <Stack gap="xs">
                    <Text variant="label" tone="muted">{t('group.memberOf')}</Text>
                    <Stack as="ul" gap="xs" ariaLabel={t('group.memberOf')}>
                        {label.groups.map((group) => (
                            <li key={group.id}><Text><TextLink href={groupPath(group.id)}>{group.name}</TextLink></Text></li>
                        ))}
                    </Stack>
                </Stack>
            )}
        </Stack>
    );
}
