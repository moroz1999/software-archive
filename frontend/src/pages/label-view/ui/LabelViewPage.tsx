import {useParams} from 'react-router';
import {requireLabelId} from '@/entities/labels';
import {LabelView} from '@/widgets/label-view';

export function LabelViewPage() {
    const id = requireLabelId(useParams()['id']);
    return <LabelView id={id}/>;
}
