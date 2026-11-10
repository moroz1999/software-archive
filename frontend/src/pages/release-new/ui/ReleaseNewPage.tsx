import {useNavigate, useParams} from 'react-router';
import {requireSoftwareId, softwarePath} from '@/entities/software';
import {ReleaseCreator} from '@/widgets/release-editor';

export function ReleaseNewPage() {
    const id = requireSoftwareId(useParams()['id']);
    const navigate = useNavigate();
    return (
        <ReleaseCreator softwareId={id} onDone={() => {
            void navigate(softwarePath(id));
        }}/>
    );
}
