import {useNavigate} from 'react-router';
import {labelPath} from '@/entities/labels';
import {LabelCreator} from '@/widgets/label-editor';

export function LabelNewPage() {
    const navigate = useNavigate();
    return (
        <LabelCreator onCreated={(id) => {
            void navigate(labelPath(id));
        }}/>
    );
}
