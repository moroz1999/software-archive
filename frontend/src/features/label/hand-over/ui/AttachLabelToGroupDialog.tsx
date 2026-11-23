import {useTranslation} from 'react-i18next';
import {type GroupId, toGroupId} from '@/entities/groups';
import {attachLabelToGroup, fetchLabelGroups} from '@/entities/labels';
import {giveToGroupMessages} from '../config/messages';
import type {HandedLabel} from '../model/hand-over-context';
import {HandOverLabelProvider} from '../model/HandOverLabelProvider';
import {ChoiceWindow} from './ChoiceWindow';

export interface AttachLabelToGroupDialogProps {
    readonly label: HandedLabel;
    readonly open: boolean;
    /** Asked to close: by Cancel or Escape. */
    readonly onClose: () => void;
    readonly onAttached: (group: GroupId) => void;
}

/** Makes a label found out to be another name of a group a label of it, keeping its credits and memberships. */
export function AttachLabelToGroupDialog({label, open, onClose, onAttached}: AttachLabelToGroupDialogProps) {
    const {t} = useTranslation();
    return (
        <HandOverLabelProvider
            label={label}
            send={attachLabelToGroup}
            messages={giveToGroupMessages(t)}
            onHandedOver={(group) => {
                onAttached(toGroupId(group));
            }}
        >
            <ChoiceWindow
                open={open}
                onClose={onClose}
                title={t('label.giveToGroup')}
                question={t('label.giveQuestion', {name: label.name})}
                action={t('label.giveAction')}
                field={t('label.toGroupField')}
                load={fetchLabelGroups}
                queryKey="label-group"
            />
        </HandOverLabelProvider>
    );
}
