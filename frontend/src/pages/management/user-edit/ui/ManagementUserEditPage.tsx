import {useTranslation} from 'react-i18next';
import {useNavigate, useParams} from 'react-router';
import {MANAGEMENT_USERS_PATH, requireUserId} from '@/entities/users';
import {ManagementTabs} from '@/features/user/management-navigation';
import {EditUserForm, EditUserFormSkeleton, EditUserProvider, UserDetailsProvider, useUserDetails} from '@/features/user/user-edit';
import {useSkeleton} from '@/shared/lib/loading';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';

/** An administrator edits one user: the form with Cancel and Save, both back to the users. */
export function ManagementUserEditPage() {
    const {t} = useTranslation();
    const id = requireUserId(useParams()['id']);
    return (
        <PageLayout title={t('actions.editUser')}>
            <ManagementTabs/>
            <UserDetailsProvider key={id} id={id}>
                <LoadedForm/>
            </UserDetailsProvider>
        </PageLayout>
    );
}

function LoadedForm() {
    const {t} = useTranslation();
    const navigate = useNavigate();
    const query = useUserDetails();
    const skeleton = useSkeleton(query);

    if (skeleton || query.isPending) {
        return <div data-testid="user-edit-skeleton"><EditUserFormSkeleton/></div>;
    }
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    return (
        <EditUserProvider user={query.data} onSaved={() => {
            void navigate(MANAGEMENT_USERS_PATH);
        }}>
            <EditUserForm actions={<Button href={MANAGEMENT_USERS_PATH} color="secondary">{t('actions.cancel')}</Button>}/>
        </EditUserProvider>
    );
}
