import {useNavigate} from 'react-router';
import {GEOGRAPHY_PATH, type PlaceKind} from '@/entities/locations';
import {ManagementTabs} from '@/features/user/management-navigation';
import {PlaceCreator} from '@/widgets/place-editor';

function PlaceNewPage({kind}: {readonly kind: PlaceKind}) {
    const navigate = useNavigate();
    return (
        <PlaceCreator kind={kind} navigation={<ManagementTabs/>} onCreated={() => {
            void navigate(GEOGRAPHY_PATH);
        }}/>
    );
}

export function CountryNewPage() {
    return <PlaceNewPage kind="country"/>;
}

export function CityNewPage() {
    return <PlaceNewPage kind="city"/>;
}
