import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import type {GrantedGroup, UserEntry} from '@/entities/users';
import {Button} from '@/shared/ui/button';
import {Checkbox} from '@/shared/ui/checkbox';
import {Dialog} from '@/shared/ui/dialog';
import {Stack} from '@/shared/ui/stack';
import {Text} from '@/shared/ui/typography';
import {useChangeUserGroups} from '../model/change-user-groups-context';
import {ChangeUserGroupsProvider} from '../model/ChangeUserGroupsProvider';

const GRANTED: readonly GrantedGroup[] = ['admin', 'moderator'];

const GROUP_KEYS: Readonly<Record<GrantedGroup, 'users.groups.admin' | 'users.groups.moderator'>> = {
    admin: 'users.groups.admin',
    moderator: 'users.groups.moderator',
};

export interface ChangeUserGroupsButtonProps {
    readonly user: UserEntry;
}

/** Gives a confirmed user the groups chosen in a dialog; an unconfirmed account gets none. */
export function ChangeUserGroupsButton({user}: ChangeUserGroupsButtonProps) {
    // Whether the dialog is open is its own UI state, not business state.
    const [open, setOpen] = useState(false);
    if (!user.confirmed) {
        return null;
    }
    return (
        <ChangeUserGroupsProvider user={user} onChanged={() => {
            setOpen(false);
        }}>
            <OpenButton onOpen={() => {
                setOpen(true);
            }}/>
            {open && <GroupsDialog onClose={() => {
                setOpen(false);
            }}/>}
        </ChangeUserGroupsProvider>
    );
}

function OpenButton({onOpen}: {readonly onOpen: () => void}) {
    const {t} = useTranslation();
    const {user, reset} = useChangeUserGroups();
    return (
        <Button size="sm" color="outlined" ariaLabel={t('users.changeGroupsOf', {name: user.name})} ariaHasPopup="dialog" onClick={() => {
            reset();
            onOpen();
        }}>
            {t('users.changeGroups')}
        </Button>
    );
}

/** The groups given on purpose as boxes, those of the user checked when it opens. */
function GroupsDialog({onClose}: {readonly onClose: () => void}) {
    const {t} = useTranslation();
    const {user, save, saving, error} = useChangeUserGroups();
    // The boxes being checked are the dialog's own state until saved.
    const [chosen, setChosen] = useState<ReadonlySet<GrantedGroup>>(() => new Set(GRANTED.filter((group) => user.groups.includes(group))));
    const close = saving ? noop : onClose;
    return (
        <Dialog
            open
            title={t('users.groupsOf', {name: user.name})}
            onClose={close}
            actions={(
                <>
                    <Button color="secondary" disabled={saving} onClick={onClose}>{t('actions.cancel')}</Button>
                    <Button loading={saving} onClick={() => {
                        save(GRANTED.filter((group) => chosen.has(group)));
                    }}>
                        {t('actions.save')}
                    </Button>
                </>
            )}
        >
            <Stack gap="md">
                {GRANTED.map((group) => (
                    <Checkbox
                        key={group}
                        label={t(GROUP_KEYS[group])}
                        checked={chosen.has(group)}
                        disabled={saving}
                        onChange={(on) => {
                            setChosen((current) => {
                                const next = new Set(current);
                                if (on) {
                                    next.add(group);
                                } else {
                                    next.delete(group);
                                }
                                return next;
                            });
                        }}
                    />
                ))}
                <Text variant="caption" tone="muted">{t('users.memberNote')}</Text>
                {error !== undefined && <Text role="alert" tone="danger">{error}</Text>}
            </Stack>
        </Dialog>
    );
}

function noop() {
    // The dialog stays while the groups are being saved.
}
