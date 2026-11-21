import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import {useNavigate} from 'react-router';
import {authorPath, AUTHORS_PATH, toAuthorId} from '@/entities/authors';
import {groupPath, toGroupId} from '@/entities/groups';
import {type LabelDetails, LabelDetailsProvider, labelEditPath, type LabelId, type LabelOwner, useLabelDetails} from '@/entities/labels';
import {CreditedSoftwareList} from '@/entities/software';
import {AttachLabelDialog} from '@/features/label/attach';
import {MakeAuthorDialog, MakeGroupDialog} from '@/features/label/make-owner';
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
    readonly id: LabelId;
}

/**
 * The page of a label: its name, whose it is when known, the groups it was a member of and the software it is credited
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
    return owner.type === 'author' ? authorPath(toAuthorId(owner.id)) : groupPath(toGroupId(owner.id));
}

function LoadedView() {
    const {t} = useTranslation();
    const navigate = useNavigate();
    const query = useLabelDetails();
    const skeleton = useSkeleton(query);
    // Which window the menu opened is UI state: the menu closes while the window stays.
    const [dialog, setDialog] = useState<'attach' | 'author' | 'group' | null>(null);

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
    const close = () => {
        setDialog(null);
    };
    return (
        <PageLayout
            title={label.name}
            breadcrumbs={[{label: t('pages.authors'), href: AUTHORS_PATH}, {label: label.name}]}
            menu={label.canEdit || label.canChange || label.canMakeOwner ? (
                <PopoverMenu label={t('actions.menu')}>
                    {label.canEdit && <PopoverMenuItem href={labelEditPath(label.id)}>{t('actions.edit')}</PopoverMenuItem>}
                    {label.canChange && <PopoverMenuItem onClick={() => { setDialog('attach'); }}>{t('label.attach')}</PopoverMenuItem>}
                    {label.canMakeOwner && (
                        <>
                            <PopoverMenuItem onClick={() => { setDialog('author'); }}>{t('label.makeAuthor')}</PopoverMenuItem>
                            <PopoverMenuItem onClick={() => { setDialog('group'); }}>{t('label.makeGroup')}</PopoverMenuItem>
                        </>
                    )}
                </PopoverMenu>
            ) : undefined}
        >
            <Stack gap="xl">
                <Details label={label}/>
                <CreditedSoftwareList owner={{kind: 'labels', id: label.id}}/>
                <AdditionNote addition={label.added}/>
            </Stack>
            {label.canChange && <AttachLabelDialog label={{id: label.id, name: label.name}} open={dialog === 'attach'} onClose={close} onAttached={openOwner}/>}
            {label.canMakeOwner && (
                <>
                    <MakeAuthorDialog label={label} open={dialog === 'author'} onClose={close} onMade={openOwner}/>
                    <MakeGroupDialog label={label} open={dialog === 'group'} onClose={close} onMade={openOwner}/>
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
            {label.groups.length > 0 && (
                <Stack gap="xs">
                    <Text variant="label" tone="muted">{t('group.memberOf')}</Text>
                    <Stack as="ul" gap="xs" ariaLabel={t('group.memberOf')}>
                        {label.groups.map((group) => (
                            <li key={group.id}><Text><TextLink href={groupPath(toGroupId(group.id))}>{group.name}</TextLink></Text></li>
                        ))}
                    </Stack>
                </Stack>
            )}
        </Stack>
    );
}
