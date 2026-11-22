import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import {fetchLabelGroups, type LabelOwnerOption} from '@/entities/labels';
import {Button} from '@/shared/ui/button';
import {Dialog} from '@/shared/ui/dialog';
import {FormField} from '@/shared/ui/form';
import {MultiEntityAutocomplete} from '@/shared/ui/multi-entity-autocomplete';
import {Stack} from '@/shared/ui/stack';
import {Text} from '@/shared/ui/typography';
import {useGiveLabel} from '../model/give-label-context';

export interface GroupChoiceWindowProps {
    readonly open: boolean;
    readonly onClose: () => void;
    readonly title: string;
    /** What giving the label does, told before the group is chosen. */
    readonly question: string;
    readonly action: string;
}

/** The window choosing the group a label is given to, among groups only. */
export function GroupChoiceWindow({open, onClose, title, question, action}: GroupChoiceWindowProps) {
    const {t} = useTranslation();
    const {give, giving, error, reset} = useGiveLabel();
    // The group chosen in the open dialog is its own UI state until it is sent.
    const [group, setGroup] = useState<LabelOwnerOption | null>(null);

    function close() {
        setGroup(null);
        reset();
        onClose();
    }

    return (
        <Dialog
            open={open}
            title={title}
            onClose={giving ? noop : close}
            focusField
            actions={(
                <>
                    <Button color="secondary" disabled={giving} onClick={close}>{t('actions.cancel')}</Button>
                    <Button
                        disabled={group === null}
                        loading={giving}
                        onClick={() => {
                            if (group !== null) {
                                give(group.id);
                            }
                        }}
                    >
                        {action}
                    </Button>
                </>
            )}
        >
            <Stack gap="md">
                <Text>{question}</Text>
                <FormField label={t('label.toGroupField')}>
                    {(control) => (
                        <MultiEntityAutocomplete
                            {...control}
                            value={group === null ? [] : [group]}
                            onChange={(chosen) => {
                                setGroup(chosen.at(-1) ?? null);
                            }}
                            load={fetchLabelGroups}
                            queryKey="label-group"
                            disabled={giving}
                        />
                    )}
                </FormField>
                {error !== undefined && <Text role="alert" tone="danger">{error}</Text>}
            </Stack>
        </Dialog>
    );
}

function noop() {
    // The dialog stays open while the label is being given.
}
