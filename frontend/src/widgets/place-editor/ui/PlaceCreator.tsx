import type {ReactNode} from 'react';
import {useTranslation} from 'react-i18next';
import {CountriesProvider, GEOGRAPHY_PATH, PlaceForm, type PlaceKind} from '@/entities/locations';
import {CreatePlaceProvider} from '@/features/geography/create';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';

export interface PlaceCreatorProps {
    readonly kind: PlaceKind;
    /** Called once the country or the city is added. */
    readonly onCreated: () => void;
    /** Navigation shared by the management pages. */
    readonly navigation?: ReactNode;
}

/** The page of adding a country or a city: its names, a city's country and the coordinates; Cancel returns to the tree. */
export function PlaceCreator({kind, onCreated, navigation}: PlaceCreatorProps) {
    const {t} = useTranslation();
    const title = t(kind === 'country' ? 'actions.addCountry' : 'actions.addCity');
    return (
        <PageLayout title={title}>
            {navigation}
            <CountriesProvider>
                <CreatePlaceProvider kind={kind} onCreated={onCreated}>
                    <PlaceForm
                        submitLabel={title}
                        actions={<Button href={GEOGRAPHY_PATH} color="secondary">{t('actions.cancel')}</Button>}
                    />
                </CreatePlaceProvider>
            </CountriesProvider>
        </PageLayout>
    );
}
