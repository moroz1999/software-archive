import {useNavigate, useParams} from 'react-router';
import {groupPath, GROUPS_PATH, requireGroupId} from '@/entities/groups';
import {GroupEditor} from '@/widgets/group-editor';

export function GroupEditPage() {
    const id = requireGroupId(useParams()['id']);
    const navigate = useNavigate();
    return (
        <GroupEditor
            id={id}
            onSaved={() => {
                void navigate(groupPath(id));
            }}
            onDeleted={() => {
                void navigate(GROUPS_PATH);
            }}
        />
    );
}
