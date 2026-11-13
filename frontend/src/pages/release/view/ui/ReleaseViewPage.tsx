import {useParams} from 'react-router';
import {requireReleaseId, requireSoftwareId} from '@/entities/software';
import {ReleaseView} from '@/widgets/release-view';

export function ReleaseViewPage() {
    const params = useParams();
    const softwareId = requireSoftwareId(params['id']);
    const releaseId = requireReleaseId(params['releaseId']);
    return <ReleaseView softwareId={softwareId} releaseId={releaseId}/>;
}
