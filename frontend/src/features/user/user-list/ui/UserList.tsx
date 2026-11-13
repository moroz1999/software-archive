import {type ReactNode, type SyntheticEvent, useState} from 'react';
import {useTranslation} from 'react-i18next';
import type {UserEntry, UserGroup} from '@/entities/users';
import {useSkeleton} from '@/shared/lib/loading';
import {Badge} from '@/shared/ui/badge';
import {Button} from '@/shared/ui/button';
import {Field} from '@/shared/ui/field';
import {Inline} from '@/shared/ui/inline';
import {Input} from '@/shared/ui/input';
import {LoadingState} from '@/shared/ui/loading-state';
import {Pagination} from '@/shared/ui/pagination';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Table, TableBody, TableCell, TableHead, TableHeaderCell, TableRow} from '@/shared/ui/table';
import {Text} from '@/shared/ui/typography';
import {registeredDayText} from '../lib/registered-day';
import {useUserList} from '../model/user-list-context';
import {userListHref} from '../model/user-list-filter';
import styles from './UserList.module.css';

const SKELETON_ROWS = [1, 2, 3, 4, 5, 6];

const GROUP_KEYS: Readonly<Record<UserGroup, 'users.groups.admin' | 'users.groups.moderator' | 'users.groups.member'>> = {
    admin: 'users.groups.admin',
    moderator: 'users.groups.moderator',
    member: 'users.groups.member',
};

export interface UserListProps {
    /** What can be done with a user, at the end of their row. */
    readonly actions?: (user: UserEntry) => ReactNode;
}

/** The users with their search and pages, inside a UserListProvider. */
export function UserList({actions}: UserListProps) {
    const {filter} = useUserList();
    return (
        <Stack gap="lg">
            {/* A new search in the address starts the field anew. */}
            <Search key={filter.query ?? ''}/>
            <Users actions={actions}/>
        </Stack>
    );
}

/** The field of the search; sending it goes to the first page of its users. */
function Search() {
    const {t} = useTranslation();
    const {filter, changing, show} = useUserList();
    const [typed, setTyped] = useState(filter.query ?? '');
    const onSubmit = (event: SyntheticEvent) => {
        event.preventDefault();
        const query = typed.trim();
        show({query: query === '' ? null : query, page: 0});
    };
    return (
        <form noValidate onSubmit={onSubmit} role="search">
            <Inline gap="sm" align="end">
                <Field label={t('users.search')}>
                    {(control) => (
                        <Input id={control.id} type="search" value={typed} onChange={setTyped} placeholder={t('users.searchPlaceholder')}/>
                    )}
                </Field>
                <Button type="submit" disabled={changing}>{t('users.find')}</Button>
            </Inline>
        </form>
    );
}

function UsersSkeleton() {
    return (
        <div data-testid="user-list-skeleton">
            <Stack gap="sm">{SKELETON_ROWS.map((row) => <Skeleton key={row} className={styles.skeletonRow}/>)}</Stack>
        </div>
    );
}

function Users({actions}: {readonly actions: UserListProps['actions']}) {
    const {t, i18n} = useTranslation();
    const {filter, users, changing} = useUserList();
    const skeleton = useSkeleton(users);

    if (skeleton || users.isPending) {
        return <UsersSkeleton/>;
    }
    if (users.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    if (users.data.items.length === 0) {
        return <Text tone="muted">{t('empty.users')}</Text>;
    }
    return (
        <LoadingState loading={changing}>
            <Stack gap="lg">
                <Table label={t('pages.users')}>
                    <TableHead>
                        <TableRow>
                            <TableHeaderCell>{t('users.columns.name')}</TableHeaderCell>
                            <TableHeaderCell>{t('users.columns.email')}</TableHeaderCell>
                            <TableHeaderCell>{t('users.columns.registered')}</TableHeaderCell>
                            <TableHeaderCell>{t('users.columns.groups')}</TableHeaderCell>
                            {actions !== undefined && <TableHeaderCell/>}
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {users.data.items.map((user) => (
                            <TableRow key={user.id}>
                                <TableCell>
                                    <Inline gap="sm" align="center" wrap>
                                        <Text as="span">{user.name}</Text>
                                        {' '}
                                        {!user.confirmed && <Badge variant="secondary">{t('users.unconfirmed')}</Badge>}
                                    </Inline>
                                </TableCell>
                                <TableCell>{user.email}</TableCell>
                                <TableCell>{registeredDayText(user.registeredAt, i18n.language)}</TableCell>
                                <TableCell>
                                    <Inline gap="xs" wrap>
                                        {user.groups.flatMap((group) => [' ', <Badge key={group}>{t(GROUP_KEYS[group])}</Badge>])}
                                    </Inline>
                                </TableCell>
                                {actions !== undefined && <TableCell align="end">{actions(user)}</TableCell>}
                            </TableRow>
                        ))}
                    </TableBody>
                </Table>
                <Pagination
                    page={users.data.number}
                    totalPages={users.data.totalPages}
                    hrefOf={(page) => userListHref({...filter, page})}
                    disabled={changing}
                />
            </Stack>
        </LoadingState>
    );
}
