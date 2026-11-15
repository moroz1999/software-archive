import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import {fetchLabelOwners, type LabelOwner, type LabelOwnerOption} from '@/entities/labels';
import {Button} from '@/shared/ui/button';
import {Checkbox} from '@/shared/ui/checkbox';
import {Dialog} from '@/shared/ui/dialog';
import {FormField} from '@/shared/ui/form';
import {MultiEntityAutocomplete} from '@/shared/ui/multi-entity-autocomplete';
import {Stack} from '@/shared/ui/stack';
import {Text} from '@/shared/ui/typography';
import {type AttachedLabel, useAttachLabel} from '../model/attach-label-context';
import {AttachLabelProvider} from '../model/AttachLabelProvider';

export type {LabelOwner} from '@/entities/labels';

export interface AttachLabelButtonProps {
    readonly label: AttachedLabel;
    readonly onAttached: (owner: LabelOwner) => void;
}

/**
 * Attaches a label of nobody to the person or group found out to be its owner; whoever attaches it says whether it is
 * the owner's display name.
 */
export function AttachLabelButton({label, onAttached}: AttachLabelButtonProps) {
    return (
        <AttachLabelProvider label={label} onAttached={onAttached}>
            <AttachWithDialog/>
        </AttachLabelProvider>
    );
}

function AttachWithDialog() {
    const {t} = useTranslation();
    const {label, attach, attaching, error, reset} = useAttachLabel();
    // What is chosen in the open dialog is its own UI state until it is sent.
    const [open, setOpen] = useState(false);
    const [owner, setOwner] = useState<LabelOwnerOption | null>(null);
    const [display, setDisplay] = useState(false);

    function close() {
        setOpen(false);
        setOwner(null);
        setDisplay(false);
        reset();
    }

    return (
        <>
            <Button color="secondary" onClick={() => {
                setOpen(true);
            }}>
                {t('label.attach')}
            </Button>
            <Dialog
                open={open}
                title={t('label.attach')}
                onClose={attaching ? noop : close}
                focusField
                actions={(
                    <>
                        <Button color="secondary" disabled={attaching} onClick={close}>{t('actions.cancel')}</Button>
                        <Button
                            disabled={owner === null}
                            loading={attaching}
                            onClick={() => {
                                if (owner !== null) {
                                    attach(owner, display);
                                }
                            }}
                        >
                            {t('label.attachAction')}
                        </Button>
                    </>
                )}
            >
                <Stack gap="md">
                    <Text>{t('label.whose', {name: label.name})}</Text>
                    <FormField label={t('fields.participant')}>
                        {(control) => (
                            <MultiEntityAutocomplete
                                {...control}
                                value={owner === null ? [] : [owner]}
                                onChange={(chosen) => {
                                    setOwner(chosen.at(-1) ?? null);
                                }}
                                load={fetchLabelOwners}
                                queryKey="label-owner"
                                disabled={attaching}
                            />
                        )}
                    </FormField>
                    <Checkbox label={t('label.display')} checked={display} onChange={setDisplay} disabled={attaching}/>
                    {error !== undefined && <Text role="alert" tone="danger">{error}</Text>}
                </Stack>
            </Dialog>
        </>
    );
}

function noop() {
    // The dialog stays open while the label is being attached.
}
