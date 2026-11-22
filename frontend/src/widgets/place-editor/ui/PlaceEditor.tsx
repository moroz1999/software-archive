import type {ReactNode} from 'react';
import {useTranslation} from 'react-i18next';
import {
    type CityId,
    CountriesProvider,
    type CountryId,
    GEOGRAPHY_PATH,
    PlaceDetailsProvider,
    PlaceForm,
    PlaceFormSkeleton,
    type PlaceKind,
    usePlaceDetails,
} from '@/entities/locations';
import {DeletePlaceButton} from '@/features/geography/delete';
import {EditPlaceProvider} from '@/features/geography/edit';
import {useSkeleton} from '@/shared/lib/loading';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';

export interface PlaceEditorProps {
    readonly kind: PlaceKind;
    readonly id: CountryId | CityId;
    /** Called once the country or the city is saved. */
    readonly onSaved: () => void;
    /** Called once it is deleted. */
    readonly onDeleted: () => void;
    /** Navigation shared by the management pages. */
    readonly navigation?: ReactNode;
}

/** The page of editing a country or a city: Delete next to the title, the form with Cancel and Save under it. */
export function PlaceEditor(props: PlaceEditorProps) {
    return (
        <CountriesProvider>
            <PlaceDetailsProvider key={props.id} kind={props.kind} id={props.id}>
                <LoadedEditor {...props}/>
            </PlaceDetailsProvider>
        </CountriesProvider>
    );
}

function LoadedEditor({kind, onSaved, onDeleted, navigation}: PlaceEditorProps) {
    const {t} = useTranslation();
    const query = usePlaceDetails();
    const skeleton = useSkeleton(query);
    const title = t(kind === 'country' ? 'actions.editCountry' : 'actions.editCity');

    if (skeleton || query.isPending) {
        return (
            <PageLayout title={title}>
                {navigation}
                <div data-testid="place-editor-skeleton"><PlaceFormSkeleton/></div>
            </PageLayout>
        );
    }
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    return (
        <PageLayout title={title} actions={<DeletePlaceButton place={query.data} onDeleted={onDeleted}/>}>
            {navigation}
            <EditPlaceProvider place={query.data} onSaved={onSaved}>
                <PlaceForm
                    submitLabel={t('actions.save')}
                    actions={<Button href={GEOGRAPHY_PATH} color="secondary">{t('actions.cancel')}</Button>}
                />
            </EditPlaceProvider>
        </PageLayout>
    );
}
