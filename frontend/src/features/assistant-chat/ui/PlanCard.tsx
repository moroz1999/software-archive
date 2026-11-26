import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {Chip} from '@/shared/ui/chip';
import {ConfirmDialog} from '@/shared/ui/confirm-dialog';
import {Inline} from '@/shared/ui/inline';
import {Panel} from '@/shared/ui/panel';
import {Stack} from '@/shared/ui/stack';
import {Text} from '@/shared/ui/typography';
import {useAssistantChat} from '../model/assistant-chat-context';
import type {ChatPlan, EntityRef, PlanAction} from '../model/chat';
import {actionRows, actionTarget, destructiveCount, failedAt} from '../model/chat-view';
import {Segments} from './Segments';
import styles from './AssistantChat.module.css';

type Refs = Readonly<Record<string, EntityRef>>;

/** A plan the assistant proposed: its changes, what each does, how far it is made, and the editor's choice while it waits. */
export function PlanCard({plan, refs}: {readonly plan: ChatPlan; readonly refs: Refs}) {
    const {t} = useTranslation();
    const {conversation, approve, cancel, planBusy} = useAssistantChat();
    const [confirming, setConfirming] = useState(false);
    const failed = failedAt(plan);
    const destructive = destructiveCount(plan);
    const waiting = plan.status === 'proposed' && conversation?.activeRun === null;
    return (
        <Panel as="section" title={plan.title} variant="flat" surface="muted" radius="lg" padding="sm">
            <Stack gap="sm">
                <Inline>
                    <Chip size="sm" variant="filled" color={plan.status === 'failed' ? 'danger' : 'neutral'}>
                        {plan.status === 'failed' ? t('assistant.plan.status.failed', {n: failed ?? 0}) : t(`assistant.plan.status.${plan.status}`)}
                    </Chip>
                </Inline>
                <Stack as="ol" gap="sm" ariaLabel={t('assistant.plan.changes')}>
                    {plan.actions.map((action) => (
                        <PlanChange key={action.position} action={action} refs={refs} failed={failed}/>
                    ))}
                </Stack>
                {waiting && (
                    <Inline gap="sm">
                        <Button size="sm" color={destructive > 0 ? 'danger' : 'primary'} disabled={planBusy} onClick={() => {
                            setConfirming(true);
                        }}
                        >
                            {t('assistant.plan.make')}
                        </Button>
                        <Button size="sm" color="transparent" disabled={planBusy} onClick={() => {
                            cancel(plan.id);
                        }}
                        >
                            {t('assistant.plan.cancel')}
                        </Button>
                    </Inline>
                )}
            </Stack>
            <ConfirmDialog
                open={confirming}
                title={t('assistant.plan.confirmTitle')}
                confirmLabel={t('assistant.plan.confirm')}
                onCancel={() => {
                    setConfirming(false);
                }}
                onConfirm={() => {
                    setConfirming(false);
                    approve(plan.id);
                }}
            >
                {t('assistant.plan.confirmQuestion', {count: plan.actions.length})}
                {destructive > 0 && ` ${t('assistant.plan.destructive', {count: destructive})}`}
            </ConfirmDialog>
        </Panel>
    );
}

const ENTITIES = ['software', 'release', 'author', 'group', 'label', 'country', 'city', 'hardware'];

/** The fields of the write tools named in words; any other is shown by its name. */
const FIELDS = [
    'title', 'name', 'realName', 'notes', 'description', 'instructions', 'releaseDate', 'version', 'type', 'types', 'platform',
    'platforms', 'hardware', 'categoryIds', 'preservation', 'credits', 'roles', 'labels', 'locations', 'members', 'groups',
    'softwareId', 'releaseId', 'groupId', 'authorId', 'labelId', 'ownerId', 'ownerType', 'countryId', 'cityId', 'intoId',
    'urls', 'url', 'fileName', 'screenshotId', 'gallery', 'sourceUrl', 'code', 'group', 'names', 'compilationItemIds',
];
const FLAGS = ['display', 'primary'];

/** The words of the reference books for the values of a field, looked up in their order. */
const VALUE_KEYS: Readonly<Record<string, readonly string[]>> = {
    type: ['release.types'],
    preservation: ['software.preservation'],
    roles: ['roles.software', 'roles.group'],
};
const KINDS = ['create', 'update', 'delete', 'link', 'unlink', 'attach', 'detach', 'merge', 'convert', 'files', 'delete-file'];

function PlanChange({action, refs, failed}: {readonly action: PlanAction; readonly refs: Refs; readonly failed: number | null}) {
    const {t, i18n} = useTranslation();
    const words = (field: string, value: string): string | null => {
        if (field === 'flag') {
            return FLAGS.includes(value) ? t(`assistant.plan.flags.${value}`) : null;
        }
        const key = VALUE_KEYS[field]?.map((group) => `${group}.${value}`).find((one) => i18n.exists(one));
        return key === undefined ? null : t(key);
    };
    const rows = actionRows(action, refs, words);
    const label = (field: string) => (FIELDS.includes(field) ? t(`assistant.plan.fields.${field}`) : field);
    const target = actionTarget(action, refs);
    return (
        <Stack as="li" gap="xs" ariaLabel={t('assistant.plan.change', {n: action.position})}>
            <Text variant="bodySm" tone="strong">
                {`${String(action.position)}. `}
                {ENTITIES.includes(action.entityType) ? t(`assistant.plan.entities.${action.entityType}`) : action.entityType}
                {' · '}
                {KINDS.includes(action.kind) ? t(`assistant.plan.kinds.${action.kind}`) : action.kind}
                {target.length > 0 && <>{' '}<Segments segments={target}/></>}
            </Text>
            {rows.length > 0 && (
                <dl className={styles.fields}>
                    {rows.map((row) => (
                        <div key={row.field}>
                            <dt><Text variant="caption" tone="muted" as="span">{label(row.field)}</Text></dt>
                            <dd aria-label={label(row.field)} className={styles.value}>
                                <Text variant="bodySm" as="span">
                                    {row.before !== null && <><Segments segments={row.before}/>{' → '}</>}
                                    <Segments segments={row.after}/>
                                </Text>
                            </dd>
                        </div>
                    ))}
                </dl>
            )}
            <ActionStatus action={action} failed={failed}/>
        </Stack>
    );
}

function ActionStatus({action, failed}: {readonly action: PlanAction; readonly failed: number | null}) {
    const {t} = useTranslation();
    switch (action.status) {
        case 'done':
            return <Text variant="caption" tone="muted">{t('assistant.plan.action.done')}</Text>;
        case 'running':
            return <Text variant="caption" tone="muted">{t('assistant.plan.action.running')}</Text>;
        case 'failed':
            return <Text variant="bodySm" tone="danger">{action.error}</Text>;
        case 'skipped':
            return (
                <Text variant="caption" tone="muted">
                    {failed === null ? t('assistant.plan.action.skippedStopped') : t('assistant.plan.action.skippedAfter', {n: failed})}
                </Text>
            );
        default:
            return null;
    }
}
