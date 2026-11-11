import {useTranslation} from 'react-i18next';
import {GroupForm, type GroupId, GROUPS_PATH} from '@/entities/groups';
import {CreateGroupProvider} from '@/features/group-create';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';
import {GroupLabels, GroupLocations} from './GroupFields';

export interface GroupCreatorProps {
    /** Called with the new group's id once it is added. */
    readonly onCreated: (id: GroupId) => void;
}

/** The page of adding a group: its name, types, labels, locations and notes; Cancel returns to the catalog. */
export function GroupCreator({onCreated}: GroupCreatorProps) {
    const {t} = useTranslation();
    return (
        <PageLayout title={t('actions.addGroup')}>
            <CreateGroupProvider onCreated={onCreated}>
                <GroupForm
                    submitLabel={t('actions.addGroup')}
                    labels={<GroupLabels/>}
                    locations={<GroupLocations/>}
                    actions={<Button href={GROUPS_PATH} color="secondary">{t('actions.cancel')}</Button>}
                />
            </CreateGroupProvider>
        </PageLayout>
    );
}
