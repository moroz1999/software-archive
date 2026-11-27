import {useTranslation} from 'react-i18next';
import {fetchLabelAuthors, mergeLabelIntoAuthor} from '@/entities/labels';
import {mergeIntoAuthorMessages} from '../config/messages';
import type {HandedLabel} from '../model/hand-over-context';
import {HandOverLabelProvider} from '../model/HandOverLabelProvider';
import {ChoiceWindow} from './ChoiceWindow';

export interface MergeLabelIntoAuthorDialogProps {
    readonly label: HandedLabel;
    readonly open: boolean;
    /** Asked to close: by Cancel or Escape. */
    readonly onClose: () => void;
    readonly onMerged: (author: string) => void;
}

/** Merges a label found out to be a duplicate of a person into them: the person takes what the label had, and the label goes. */
export function MergeLabelIntoAuthorDialog({label, open, onClose, onMerged}: MergeLabelIntoAuthorDialogProps) {
    const {t} = useTranslation();
    return (
        <HandOverLabelProvider
            label={label}
            send={mergeLabelIntoAuthor}
            messages={mergeIntoAuthorMessages(t)}
            onHandedOver={(author) => {
                onMerged(author);
            }}
        >
            <ChoiceWindow
                open={open}
                onClose={onClose}
                title={t('label.mergeIntoAuthor')}
                question={t('label.mergeIntoAuthorQuestion', {name: label.name})}
                action={t('label.mergeAction')}
                field={t('label.toAuthorField')}
                load={fetchLabelAuthors}
                queryKey="label-author"
            />
        </HandOverLabelProvider>
    );
}
