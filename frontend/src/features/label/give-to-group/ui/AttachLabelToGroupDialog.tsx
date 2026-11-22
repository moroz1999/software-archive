import {useTranslation} from 'react-i18next';
import type {GroupId} from '@/entities/groups';
import {attachLabelToGroup} from '@/entities/labels';
import type {GivenLabel} from '../model/give-label-context';
import {GiveLabelProvider} from '../model/GiveLabelProvider';
import {GroupChoiceWindow} from './GroupChoiceWindow';

export interface AttachLabelToGroupDialogProps {
    readonly label: GivenLabel;
    readonly open: boolean;
    /** Asked to close: by Cancel or Escape. */
    readonly onClose: () => void;
    readonly onAttached: (group: GroupId) => void;
}

/** Makes a label found out to be another name of a group a label of it, keeping its credits and memberships. */
export function AttachLabelToGroupDialog({label, open, onClose, onAttached}: AttachLabelToGroupDialogProps) {
    const {t} = useTranslation();
    return (
        <GiveLabelProvider label={label} send={attachLabelToGroup} onGiven={onAttached}>
            <GroupChoiceWindow
                open={open}
                onClose={onClose}
                title={t('label.giveToGroup')}
                question={t('label.giveQuestion', {name: label.name})}
                action={t('label.giveAction')}
            />
        </GiveLabelProvider>
    );
}
