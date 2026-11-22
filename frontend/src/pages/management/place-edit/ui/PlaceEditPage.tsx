import {useNavigate, useParams} from 'react-router';
import {GEOGRAPHY_PATH, type PlaceKind, requirePlaceId} from '@/entities/locations';
import {ManagementTabs} from '@/features/user/management-navigation';
import {PlaceEditor} from '@/widgets/place-editor';

function PlaceEditPage({kind}: {readonly kind: PlaceKind}) {
    const id = requirePlaceId(kind, useParams()['id']);
    const navigate = useNavigate();
    function backToTree() {
        void navigate(GEOGRAPHY_PATH);
    }
    return <PlaceEditor kind={kind} id={id} onSaved={backToTree} onDeleted={backToTree} navigation={<ManagementTabs/>}/>;
}

export function CountryEditPage() {
    return <PlaceEditPage kind="country"/>;
}

export function CityEditPage() {
    return <PlaceEditPage kind="city"/>;
}
