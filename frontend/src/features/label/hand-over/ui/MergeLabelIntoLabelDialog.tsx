import {useTranslation} from 'react-i18next';
import {fetchLabelsByName, mergeLabelIntoLabel} from '@/entities/labels';
import {mergeIntoLabelMessages} from '../config/messages';
import type {HandedLabel} from '../model/hand-over-context';
import {HandOverLabelProvider} from '../model/HandOverLabelProvider';
import {ChoiceWindow} from './ChoiceWindow';

export interface MergeLabelIntoLabelDialogProps {
    readonly label: HandedLabel;
    readonly open: boolean;
    /** Asked to close: by Cancel or Escape. */
    readonly onClose: () => void;
    readonly onMerged: (label: string) => void;
}

/** Merges a label found out to be a duplicate of another label into it: that label takes what it had, and it goes. */
export function MergeLabelIntoLabelDialog({label, open, onClose, onMerged}: MergeLabelIntoLabelDialogProps) {
    const {t} = useTranslation();
    return (
        <HandOverLabelProvider
            label={label}
            send={mergeLabelIntoLabel}
            messages={mergeIntoLabelMessages(t)}
            onHandedOver={(into) => {
                onMerged(into);
            }}
        >
            <ChoiceWindow
                open={open}
                onClose={onClose}
                title={t('label.mergeIntoLabel')}
                question={t('label.mergeIntoLabelQuestion', {name: label.name})}
                action={t('label.mergeAction')}
                field={t('label.toLabelField')}
                load={async (query, signal) => (await fetchLabelsByName(query, signal)).filter((option) => option.id !== label.id)}
                queryKey={`label-merge-into:${label.id}`}
            />
        </HandOverLabelProvider>
    );
}
