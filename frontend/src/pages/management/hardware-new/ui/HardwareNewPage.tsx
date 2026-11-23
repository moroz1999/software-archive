import {useNavigate} from 'react-router';
import {HARDWARE_PATH, type HardwareKind} from '@/entities/hardware';
import {ManagementTabs} from '@/features/user/management-navigation';
import {HardwareCreator} from '@/widgets/hardware-editor';

function NewPage({kind}: {readonly kind: HardwareKind}) {
    const navigate = useNavigate();
    return (
        <HardwareCreator kind={kind} navigation={<ManagementTabs/>} onCreated={() => {
            void navigate(HARDWARE_PATH);
        }}/>
    );
}

export function HardwareGroupNewPage() {
    return <NewPage kind="group"/>;
}

export function HardwareNewPage() {
    return <NewPage kind="hardware"/>;
}
