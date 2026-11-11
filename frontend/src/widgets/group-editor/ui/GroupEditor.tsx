import {useTranslation} from 'react-i18next';
import {GroupDetailsProvider, GroupForm, GroupFormSkeleton, type GroupId, groupPath, useGroupDetails} from '@/entities/groups';
import {DeleteGroupButton} from '@/features/group-delete';
import {EditGroupProvider} from '@/features/group-edit';
import {useSkeleton} from '@/shared/lib/skeleton';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';
import {GroupLabels, GroupLocations} from './GroupFields';

export interface GroupEditorProps {
    readonly id: GroupId;
    /** Called once the group is saved. */
    readonly onSaved: () => void;
    /** Called once the group is deleted. */
    readonly onDeleted: () => void;
}

/** The page of editing one group: Delete next to the title, the form with Cancel and Save under it. */
export function GroupEditor(props: GroupEditorProps) {
    return (
        <GroupDetailsProvider key={props.id} id={props.id}>
            <LoadedEditor {...props}/>
        </GroupDetailsProvider>
    );
}

function LoadedEditor({id, onSaved, onDeleted}: GroupEditorProps) {
    const {t} = useTranslation();
    const query = useGroupDetails();
    const skeleton = useSkeleton(query);

    if (skeleton || query.isPending) {
        return (
            <PageLayout title={t('actions.editGroup')}>
                <div data-testid="group-editor-skeleton"><GroupFormSkeleton/></div>
            </PageLayout>
        );
    }
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    return (
        <PageLayout title={t('actions.editGroup')} actions={<DeleteGroupButton group={query.data} onDeleted={onDeleted}/>}>
            <EditGroupProvider group={query.data} onSaved={onSaved}>
                <GroupForm
                    submitLabel={t('actions.save')}
                    labels={<GroupLabels/>}
                    locations={<GroupLocations/>}
                    actions={<Button href={groupPath(id)} color="secondary">{t('actions.cancel')}</Button>}
                />
            </EditGroupProvider>
        </PageLayout>
    );
}
