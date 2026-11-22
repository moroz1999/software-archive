import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import type {GeographyCity} from '@/entities/locations';
import {Button} from '@/shared/ui/button';
import {ConfirmDialog} from '@/shared/ui/confirm-dialog';
import {useMakeCountry} from '../model/make-country-context';
import {MakeCountryProvider} from '../model/MakeCountryProvider';

/** Makes a city of no known country a country after asking; the city goes with it. */
export function MakeCountryButton({city}: {readonly city: GeographyCity}) {
    return (
        <MakeCountryProvider city={city}>
            <MakeWithConfirmation/>
        </MakeCountryProvider>
    );
}

function MakeWithConfirmation() {
    const {t} = useTranslation();
    const {city, make, making, made, error} = useMakeCountry();
    // Whether the confirmation is open is the dialog's own UI state, not business state.
    const [asking, setAsking] = useState(false);

    return (
        <>
            <Button
                size="sm"
                color="outlined"
                ariaLabel={t('geography.makeCountryOf', {name: city.name})}
                onClick={() => {
                    setAsking(true);
                }}
            >
                {t('geography.makeCountry')}
            </Button>
            <ConfirmDialog
                open={asking && !made}
                title={t('geography.makeCountry')}
                confirmLabel={t('geography.makeCountry')}
                onConfirm={make}
                onCancel={() => {
                    setAsking(false);
                }}
                busy={making}
                error={error}
            >
                {t('geography.makeCountryQuestion', {name: city.name})}
            </ConfirmDialog>
        </>
    );
}
