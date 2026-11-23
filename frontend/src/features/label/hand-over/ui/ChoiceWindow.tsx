import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {Dialog} from '@/shared/ui/dialog';
import {FormField} from '@/shared/ui/form';
import {type EntityAutocompleteOption, MultiEntityAutocomplete} from '@/shared/ui/multi-entity-autocomplete';
import {Stack} from '@/shared/ui/stack';
import {Text} from '@/shared/ui/typography';
import {useHandOverLabel} from '../model/hand-over-context';

export interface ChoiceWindowProps {
    readonly open: boolean;
    readonly onClose: () => void;
    readonly title: string;
    /** What handing the label over does, told before anything is chosen. */
    readonly question: string;
    readonly action: string;
    /** The label of the field choosing whom the label is handed over to. */
    readonly field: string;
    /** Finds whom the label may be handed over to, by name. */
    readonly load: (query: string, signal: AbortSignal) => Promise<readonly EntityAutocompleteOption[]>;
    readonly queryKey: string;
}

/** The window choosing the group, the person or the other label a label is handed over to. */
export function ChoiceWindow({open, onClose, title, question, action, field, load, queryKey}: ChoiceWindowProps) {
    const {t} = useTranslation();
    const {handOver, handingOver, error, reset} = useHandOverLabel();
    // The choice in the open dialog is its own UI state until it is sent.
    const [chosen, setChosen] = useState<EntityAutocompleteOption | null>(null);

    function close() {
        setChosen(null);
        reset();
        onClose();
    }

    return (
        <Dialog
            open={open}
            title={title}
            onClose={handingOver ? noop : close}
            focusField
            actions={(
                <>
                    <Button color="secondary" disabled={handingOver} onClick={close}>{t('actions.cancel')}</Button>
                    <Button
                        disabled={chosen === null}
                        loading={handingOver}
                        onClick={() => {
                            if (chosen !== null) {
                                handOver(chosen.id);
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
                <FormField label={field}>
                    {(control) => (
                        <MultiEntityAutocomplete
                            {...control}
                            value={chosen === null ? [] : [chosen]}
                            onChange={(options) => {
                                setChosen(options.at(-1) ?? null);
                            }}
                            load={load}
                            queryKey={queryKey}
                            disabled={handingOver}
                        />
                    )}
                </FormField>
                {error !== undefined && <Text role="alert" tone="danger">{error}</Text>}
            </Stack>
        </Dialog>
    );
}

function noop() {
    // The dialog stays open while the label is being handed over.
}
