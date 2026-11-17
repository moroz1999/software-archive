import {useState} from 'react';
import {useQuery} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {groupTypesQuery} from '@/entities/groups';
import type {LabelOwner} from '@/entities/labels';
import {Button} from '@/shared/ui/button';
import {Checkbox} from '@/shared/ui/checkbox';
import {FormFieldset} from '@/shared/ui/form';
import {Inline} from '@/shared/ui/inline';
import {Skeleton} from '@/shared/ui/skeleton';
import {type MadeOwnerLabel, useMakeOwner} from '../model/make-owner-context';
import {MakeOwnerProvider} from '../model/MakeOwnerProvider';
import {MakeOwnerDialog} from './MakeOwnerDialog';
import styles from './MakeGroupButton.module.css';

export interface MakeGroupButtonProps {
    readonly label: MadeOwnerLabel;
    readonly onMade: (owner: LabelOwner) => void;
}

/** Makes a new group under the name of the label, of the types chosen in its dialog. */
export function MakeGroupButton({label, onMade}: MakeGroupButtonProps) {
    return (
        <MakeOwnerProvider label={label} onMade={onMade}>
            <MakeGroupWithDialog/>
        </MakeOwnerProvider>
    );
}

function MakeGroupWithDialog() {
    const {t} = useTranslation();
    const {label, makeGroup, making, reset} = useMakeOwner();
    // What is chosen in the open dialog is its own UI state until it is sent.
    const [open, setOpen] = useState(false);
    const [types, setTypes] = useState<readonly string[]>([]);

    function close() {
        setOpen(false);
        setTypes([]);
        reset();
    }

    return (
        <>
            <Button color="secondary" onClick={() => {
                setOpen(true);
            }}>
                {t('label.makeGroup')}
            </Button>
            <MakeOwnerDialog
                open={open}
                title={t('label.makeGroup')}
                question={t('label.makeGroupQuestion', {name: label.name})}
                ready={types.length > 0}
                onMake={() => {
                    makeGroup(types);
                }}
                onClose={close}
            >
                {open && <TypeChoices types={types} disabled={making} onChange={setTypes}/>}
            </MakeOwnerDialog>
        </>
    );
}

interface TypeChoicesProps {
    readonly types: readonly string[];
    readonly disabled: boolean;
    readonly onChange: (types: readonly string[]) => void;
}

function TypeChoices({types, disabled, onChange}: TypeChoicesProps) {
    const {t} = useTranslation();
    const codes = useQuery(groupTypesQuery());
    return (
        <FormFieldset legend={t('group.types')} required>
            {codes.data === undefined
                ? <Skeleton className={styles.loading}/>
                : (
                    <Inline gap="md" wrap>
                        {codes.data.map((code) => (
                            <Checkbox
                                key={code}
                                label={t(`group.typeNames.${code}`, {defaultValue: code})}
                                checked={types.includes(code)}
                                onChange={(checked) => {
                                    onChange(checked ? [...types, code] : types.filter((type) => type !== code));
                                }}
                                disabled={disabled}
                            />
                        ))}
                    </Inline>
                )}
        </FormFieldset>
    );
}
