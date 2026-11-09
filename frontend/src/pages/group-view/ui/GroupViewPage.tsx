import {useParams} from 'react-router';
import {requireGroupId} from '@/entities/groups';
import {GroupView} from '@/widgets/group-view';

export function GroupViewPage() {
    const id = requireGroupId(useParams()['id']);
    return <GroupView id={id}/>;
}
