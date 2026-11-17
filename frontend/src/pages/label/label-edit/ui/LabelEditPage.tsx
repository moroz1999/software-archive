import {useNavigate, useParams} from 'react-router';
import {AUTHORS_PATH} from '@/entities/authors';
import {labelPath, requireLabelId} from '@/entities/labels';
import {LabelEditor} from '@/widgets/label-editor';

export function LabelEditPage() {
    const id = requireLabelId(useParams()['id']);
    const navigate = useNavigate();
    return (
        <LabelEditor
            id={id}
            onSaved={() => {
                void navigate(labelPath(id));
            }}
            onDeleted={() => {
                void navigate(AUTHORS_PATH);
            }}
        />
    );
}
