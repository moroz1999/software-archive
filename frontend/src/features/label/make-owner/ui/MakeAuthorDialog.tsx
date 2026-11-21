import {useTranslation} from 'react-i18next';
import type {LabelOwner} from '@/entities/labels';
import {type MadeOwnerLabel, useMakeOwner} from '../model/make-owner-context';
import {MakeOwnerProvider} from '../model/MakeOwnerProvider';
import {MakeOwnerDialog} from './MakeOwnerDialog';

export interface MakeAuthorDialogProps {
    readonly label: MadeOwnerLabel;
    readonly open: boolean;
    /** Asked to close: by Cancel or Escape. */
    readonly onClose: () => void;
    readonly onMade: (owner: LabelOwner) => void;
}

/** Asks before replacing the label with a new person of the same name. */
export function MakeAuthorDialog({label, open, onClose, onMade}: MakeAuthorDialogProps) {
    return (
        <MakeOwnerProvider label={label} onMade={onMade}>
            <MakeAuthorWindow open={open} onClose={onClose}/>
        </MakeOwnerProvider>
    );
}

function MakeAuthorWindow({open, onClose}: {readonly open: boolean; readonly onClose: () => void}) {
    const {t} = useTranslation();
    const {label, makeAuthor, reset} = useMakeOwner();

    function close() {
        reset();
        onClose();
    }

    return (
        <MakeOwnerDialog
            open={open}
            title={t('label.makeAuthor')}
            question={t('label.makeAuthorQuestion', {name: label.name})}
            ready
            onMake={makeAuthor}
            onClose={close}
        />
    );
}
