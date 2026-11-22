import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import {type PlaceDetails, placeName} from '@/entities/locations';
import {parseUiLanguage} from '@/shared/lib/i18n';
import {Button} from '@/shared/ui/button';
import {ConfirmDialog} from '@/shared/ui/confirm-dialog';
import {useDeletePlace} from '../model/delete-place-context';
import {DeletePlaceProvider} from '../model/DeletePlaceProvider';

export interface DeletePlaceButtonProps {
    readonly place: PlaceDetails;
    readonly onDeleted: () => void;
}

/** Deletes a country or a city after asking; the archive refuses one something is in. */
export function DeletePlaceButton({place, onDeleted}: DeletePlaceButtonProps) {
    return (
        <DeletePlaceProvider place={place} onDeleted={onDeleted}>
            <DeleteWithConfirmation/>
        </DeletePlaceProvider>
    );
}

function DeleteWithConfirmation() {
    const {t, i18n} = useTranslation();
    const {place, remove, deleting, deleted, error} = useDeletePlace();
    // Whether the confirmation is open is the dialog's own UI state, not business state.
    const [asking, setAsking] = useState(false);

    return (
        <>
            <Button color="danger" onClick={() => {
                setAsking(true);
            }}>
                {t('actions.delete')}
            </Button>
            <ConfirmDialog
                open={asking && !deleted}
                title={t(place.kind === 'country' ? 'geography.deleteCountry' : 'geography.deleteCity')}
                confirmLabel={t('actions.deleteForGood')}
                onConfirm={remove}
                onCancel={() => {
                    setAsking(false);
                }}
                busy={deleting}
                error={error}
            >
                {t('geography.deleteQuestion', {name: placeName(place.names, parseUiLanguage(i18n.language) ?? 'en')})}
            </ConfirmDialog>
        </>
    );
}
