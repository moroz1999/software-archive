import {useTranslation} from 'react-i18next';
import {fetchLabelGroups, mergeLabelIntoGroup} from '@/entities/labels';
import {giveToGroupMessages} from '../config/messages';
import type {HandedLabel} from '../model/hand-over-context';
import {HandOverLabelProvider} from '../model/HandOverLabelProvider';
import {ChoiceWindow} from './ChoiceWindow';

export interface MergeLabelIntoGroupDialogProps {
    readonly label: HandedLabel;
    readonly open: boolean;
    /** Asked to close: by Cancel or Escape. */
    readonly onClose: () => void;
    readonly onMerged: (group: string) => void;
}

/** Merges a label found out to be a duplicate of a group into it: the group takes what the label had, and the label goes. */
export function MergeLabelIntoGroupDialog({label, open, onClose, onMerged}: MergeLabelIntoGroupDialogProps) {
    const {t} = useTranslation();
    return (
        <HandOverLabelProvider
            label={label}
            send={mergeLabelIntoGroup}
            messages={giveToGroupMessages(t)}
            onHandedOver={(group) => {
                onMerged(group);
            }}
        >
            <ChoiceWindow
                open={open}
                onClose={onClose}
                title={t('label.merge')}
                question={t('label.mergeQuestion', {name: label.name})}
                action={t('label.mergeAction')}
                field={t('label.toGroupField')}
                load={fetchLabelGroups}
                queryKey="label-group"
            />
        </HandOverLabelProvider>
    );
}
