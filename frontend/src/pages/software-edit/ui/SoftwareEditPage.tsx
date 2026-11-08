import {useNavigate, useParams} from 'react-router';
import {SOFTWARE_LIST_PATH} from '../../../entities/software';
import {PageLayout} from '../../../shared/ui';
import {SoftwareEditor} from '../../../widgets/software-editor';

export function SoftwareEditPage() {
    const {id} = useParams();
    const navigate = useNavigate();
    return (
        <PageLayout title="Edit software">
            <SoftwareEditor id={id} onDone={() => {
                void navigate(SOFTWARE_LIST_PATH);
            }}/>
        </PageLayout>
    );
}
