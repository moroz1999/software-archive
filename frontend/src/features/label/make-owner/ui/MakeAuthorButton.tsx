import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import type {LabelOwner} from '@/entities/labels';
import {Button} from '@/shared/ui/button';
import {type MadeOwnerLabel, useMakeOwner} from '../model/make-owner-context';
import {MakeOwnerProvider} from '../model/MakeOwnerProvider';
import {MakeOwnerDialog} from './MakeOwnerDialog';

export interface MakeAuthorButtonProps {
    readonly label: MadeOwnerLabel;
    readonly onMade: (owner: LabelOwner) => void;
}

/** Makes a new person known by the label, which becomes their display name, after asking. */
export function MakeAuthorButton({label, onMade}: MakeAuthorButtonProps) {
    return (
        <MakeOwnerProvider label={label} onMade={onMade}>
            <MakeAuthorWithDialog/>
        </MakeOwnerProvider>
    );
}

function MakeAuthorWithDialog() {
    const {t} = useTranslation();
    const {label, makeAuthor, reset} = useMakeOwner();
    const [open, setOpen] = useState(false);

    function close() {
        setOpen(false);
        reset();
    }

    return (
        <>
            <Button color="secondary" onClick={() => {
                setOpen(true);
            }}>
                {t('label.makeAuthor')}
            </Button>
            <MakeOwnerDialog
                open={open}
                title={t('label.makeAuthor')}
                question={t('label.makeAuthorQuestion', {name: label.name})}
                ready
                onMake={makeAuthor}
                onClose={close}
            />
        </>
    );
}
