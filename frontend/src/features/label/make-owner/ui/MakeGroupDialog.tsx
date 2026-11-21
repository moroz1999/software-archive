import {useState} from 'react';
import {useQuery} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {groupTypesQuery} from '@/entities/groups';
import type {LabelOwner} from '@/entities/labels';
import {Checkbox} from '@/shared/ui/checkbox';
import {FormFieldset} from '@/shared/ui/form';
import {Inline} from '@/shared/ui/inline';
import {Skeleton} from '@/shared/ui/skeleton';
import {type MadeOwnerLabel, useMakeOwner} from '../model/make-owner-context';
import {MakeOwnerProvider} from '../model/MakeOwnerProvider';
import {MakeOwnerDialog} from './MakeOwnerDialog';
import styles from './MakeGroupDialog.module.css';

export interface MakeGroupDialogProps {
    readonly label: MadeOwnerLabel;
    readonly open: boolean;
    /** Asked to close: by Cancel or Escape. */
    readonly onClose: () => void;
    readonly onMade: (owner: LabelOwner) => void;
}

/** Asks for the types of a new group under the name of the label and makes it. */
export function MakeGroupDialog({label, open, onClose, onMade}: MakeGroupDialogProps) {
    return (
        <MakeOwnerProvider label={label} onMade={onMade}>
            <MakeGroupWindow open={open} onClose={onClose}/>
        </MakeOwnerProvider>
    );
}

function MakeGroupWindow({open, onClose}: {readonly open: boolean; readonly onClose: () => void}) {
    const {t} = useTranslation();
    const {label, makeGroup, making, reset} = useMakeOwner();
    // What is chosen in the open dialog is its own UI state until it is sent.
    const [types, setTypes] = useState<readonly string[]>([]);

    function close() {
        setTypes([]);
        reset();
        onClose();
    }

    return (
            <MakeOwnerDialog
                open={open}
                title={t('label.makeGroup')}
                question={t('label.makeGroupQuestion', {name: label.name})}
                ready
                onMake={() => {
                    makeGroup(types);
                }}
                onClose={close}
            >
                {open && <TypeChoices types={types} disabled={making} onChange={setTypes}/>}
            </MakeOwnerDialog>
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
        <FormFieldset legend={t('group.types')}>
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
