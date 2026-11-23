import {useNavigate, useParams} from 'react-router';
import {HARDWARE_PATH, type HardwareKind, requireHardwareCode} from '@/entities/hardware';
import {ManagementTabs} from '@/features/user/management-navigation';
import {HardwareEditor} from '@/widgets/hardware-editor';

function EditPage({kind}: {readonly kind: HardwareKind}) {
    const code = requireHardwareCode(kind, useParams()['code']);
    const navigate = useNavigate();
    function backToList() {
        void navigate(HARDWARE_PATH);
    }
    return <HardwareEditor kind={kind} code={code} onSaved={backToList} onDeleted={backToList} navigation={<ManagementTabs/>}/>;
}

export function HardwareGroupEditPage() {
    return <EditPage kind="group"/>;
}

export function HardwareEditPage() {
    return <EditPage kind="hardware"/>;
}
