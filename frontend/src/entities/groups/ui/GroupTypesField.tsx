import {useTranslation} from 'react-i18next';
import {Checkbox} from '@/shared/ui/checkbox';
import {FieldGroup} from '@/shared/ui/field-group';
import {Inline} from '@/shared/ui/inline';
import {Skeleton} from '@/shared/ui/skeleton';
import {TYPES_REQUIRED_KEY} from '../config/messages';
import {useGroupForm} from '../model/group-form-context';
import {useGroupTypes} from '../model/group-types-context';
import {GroupTypesProvider} from '../model/GroupTypesProvider';
import styles from './GroupTypesField.module.css';

/** The types of the group in the form around, chosen from the reference book. */
export function GroupTypesField() {
    return (
        <GroupTypesProvider>
            <TypeChoices/>
        </GroupTypesProvider>
    );
}

function TypeChoices() {
    const {t} = useTranslation();
    const {values, errors, refusals, changeTypes, busy} = useGroupForm();
    const types = useGroupTypes();

    function toggle(code: string, checked: boolean) {
        changeTypes(checked ? [...values.types, code] : values.types.filter((type) => type !== code));
    }

    return (
        <FieldGroup legend={t('group.types')} error={errors.types === undefined ? refusals.types : t(TYPES_REQUIRED_KEY)}>
            {types.data === undefined
                ? <Skeleton className={styles.loading}/>
                : (
                    <Inline gap="md" wrap>
                        {types.data.map((code) => (
                            <Checkbox
                                key={code}
                                label={code}
                                checked={values.types.includes(code)}
                                onChange={(checked) => {
                                    toggle(code, checked);
                                }}
                                disabled={busy}
                            />
                        ))}
                    </Inline>
                )}
        </FieldGroup>
    );
}
