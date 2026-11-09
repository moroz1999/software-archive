import {useParams} from 'react-router';
import {requireSoftwareId} from '@/entities/software';
import {SoftwareView} from '@/widgets/software-view';

export function SoftwareViewPage() {
    const id = requireSoftwareId(useParams()['id']);
    return <SoftwareView id={id}/>;
}
