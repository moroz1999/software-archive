import {useTranslation} from 'react-i18next';
import {managementUserEditPath} from '@/entities/users';
import {ManagementTabs} from '@/features/user/management-navigation';
import {UserList, UserListProvider} from '@/features/user/user-list';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';

/** The users of the archive, kept by an administrator. */
export function ManagementUsersPage() {
    const {t} = useTranslation();
    return (
        <PageLayout title={t('pages.users')}>
            <ManagementTabs/>
            <UserListProvider>
                <UserList actions={(user) => (
                    <Button size="sm" color="outlined" href={managementUserEditPath(user.id)} ariaLabel={t('users.editOf', {name: user.name})}>
                        {t('actions.edit')}
                    </Button>
                )}/>
            </UserListProvider>
        </PageLayout>
    );
}
