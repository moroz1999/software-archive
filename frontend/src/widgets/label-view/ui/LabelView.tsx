import {useTranslation} from 'react-i18next';
import {useNavigate} from 'react-router';
import {authorPath, toAuthorId} from '@/entities/authors';
import {groupPath, toGroupId} from '@/entities/groups';
import {type LabelDetails, LabelDetailsProvider, type LabelId, type LabelOwner, useLabelDetails} from '@/entities/labels';
import {CreditedSoftwareList} from '@/entities/software';
import {AttachLabelButton} from '@/features/label/attach';
import {useSkeleton} from '@/shared/lib/loading';
import {Inline} from '@/shared/ui/inline';
import {PageLayout} from '@/shared/ui/page-layout';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Text, TextLink} from '@/shared/ui/typography';
import styles from './LabelView.module.css';

export interface LabelViewProps {
    readonly id: LabelId;
}

/**
 * The page of a label: its name, whose it is when known, the groups it was a member of and the software it is credited
 * on; a label of nobody waits there for a person who knows whose it is to attach it to its owner.
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
    const navigate = useNavigate();
    const query = useLabelDetails();
    const skeleton = useSkeleton(query);

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
    return (
        <PageLayout
            title={label.name}
            actions={label.canChange && (
                <AttachLabelButton label={{id: label.id, name: label.name}} onAttached={(owner) => {
                    void navigate(ownerPath(owner));
                }}/>
            )}
        >
            <Stack gap="xl">
                <Details label={label}/>
                <CreditedSoftwareList owner={{kind: 'labels', id: label.id}}/>
            </Stack>
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
