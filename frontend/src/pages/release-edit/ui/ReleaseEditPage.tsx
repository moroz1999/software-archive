import {useNavigate, useParams} from 'react-router';
import {requireReleaseId, requireSoftwareId, softwarePath} from '@/entities/software';
import {ReleaseEditor} from '@/widgets/release-editor';

export function ReleaseEditPage() {
    const params = useParams();
    const id = requireSoftwareId(params['id']);
    const releaseId = requireReleaseId(params['releaseId']);
    const navigate = useNavigate();
    return (
        <ReleaseEditor softwareId={id} releaseId={releaseId} onDone={() => {
            void navigate(softwarePath(id));
        }}/>
    );
}
