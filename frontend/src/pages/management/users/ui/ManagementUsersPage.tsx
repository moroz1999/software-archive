import {useTranslation} from 'react-i18next';
import {ChangeUserGroupsButton} from '@/features/user/change-groups';
import {ManagementTabs} from '@/features/user/management-navigation';
import {UserList, UserListProvider} from '@/features/user/user-list';
import {PageLayout} from '@/shared/ui/page-layout';

/** The users of the archive, kept by an administrator. */
export function ManagementUsersPage() {
    const {t} = useTranslation();
    return (
        <PageLayout title={t('pages.users')}>
            <ManagementTabs/>
            <UserListProvider>
                <UserList actions={(user) => <ChangeUserGroupsButton user={user}/>}/>
            </UserListProvider>
        </PageLayout>
    );
}
