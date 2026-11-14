import {useNavigate, useParams} from 'react-router';
import {requireSoftwareId, SOFTWARE_LIST_PATH, softwarePath} from '@/entities/software';
import {SoftwareEditor} from '@/widgets/software-editor';

export function SoftwareEditPage() {
    const id = requireSoftwareId(useParams()['id']);
    const navigate = useNavigate();
    return (
        <SoftwareEditor
            id={id}
            onSaved={(softwareId) => {
                void navigate(softwarePath(softwareId));
            }}
            onDeleted={() => {
                void navigate(SOFTWARE_LIST_PATH);
            }}
        />
    );
}
