import {useTranslation} from 'react-i18next';
import type {GroupId} from '@/entities/groups';
import {mergeLabelIntoGroup} from '@/entities/labels';
import type {GivenLabel} from '../model/give-label-context';
import {GiveLabelProvider} from '../model/GiveLabelProvider';
import {GroupChoiceWindow} from './GroupChoiceWindow';

export interface MergeLabelIntoGroupDialogProps {
    readonly label: GivenLabel;
    readonly open: boolean;
    /** Asked to close: by Cancel or Escape. */
    readonly onClose: () => void;
    readonly onMerged: (group: GroupId) => void;
}

/** Merges a label found out to be a duplicate of a group into it: the group takes what the label had, and the label goes. */
export function MergeLabelIntoGroupDialog({label, open, onClose, onMerged}: MergeLabelIntoGroupDialogProps) {
    const {t} = useTranslation();
    return (
        <GiveLabelProvider label={label} send={mergeLabelIntoGroup} onGiven={onMerged}>
            <GroupChoiceWindow
                open={open}
                onClose={onClose}
                title={t('label.merge')}
                question={t('label.mergeQuestion', {name: label.name})}
                action={t('label.mergeAction')}
            />
        </GiveLabelProvider>
    );
}
