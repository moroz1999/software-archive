import {useNavigate} from 'react-router';
import {groupPath} from '@/entities/groups';
import {GroupCreator} from '@/widgets/group-editor';

export function GroupNewPage() {
    const navigate = useNavigate();
    return (
        <GroupCreator onCreated={(id) => {
            void navigate(groupPath(id));
        }}/>
    );
}
