import type {ReactNode} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {Checkbox} from '@/shared/ui/checkbox';
import {Form, FormActions, FormField, FormFieldset, FormMessage} from '@/shared/ui/form';
import {Inline} from '@/shared/ui/inline';
import {Input} from '@/shared/ui/input';
import {Select} from '@/shared/ui/select';
import {Skeleton} from '@/shared/ui/skeleton';
import {HARDWARE_NAME_LANGUAGES} from '../model/hardware-form';
import {useHardwareForm} from '../model/hardware-form-context';
import styles from './HardwareForm.module.css';

export interface HardwareFormProps {
    readonly submitLabel: string;
    /** More buttons before the submit button, such as Cancel. */
    readonly actions?: ReactNode;
}

/**
 * The fields of a group or of an item of hardware inside a HardwareFormProvider: the code, kept once given; a name per
 * language with the Russian one required; an item's group and platforms.
 */
export function HardwareForm({submitLabel, actions}: HardwareFormProps) {
    const {t} = useTranslation();
    const {kind, codeFixed, values, errors, refusals, changeCode, changeName, submit, busy, error} = useHardwareForm();
    const codeError = errors.code === 'required'
        ? t('hardware.codeRequired')
        : errors.code === 'pattern' ? t('hardware.codePattern') : refusals.code;
    const nameError = errors.name === 'required'
        ? t('geography.nameRequired')
        : errors.name === 'too-long' ? t('hardware.nameTooLong') : undefined;

    return (
        <Form onSubmit={submit}>
            {error !== undefined && <FormMessage>{error}</FormMessage>}
            <FormField label={t('hardware.code')} required={!codeFixed} error={codeError}>
                {(control) => <Input {...control} value={values.code} onChange={changeCode} disabled={busy || codeFixed}/>}
            </FormField>
            {HARDWARE_NAME_LANGUAGES.map((language) => (
                <FormField
                    key={language}
                    label={t(`geography.nameIn.${language}`)}
                    required={language === 'ru'}
                    error={language === 'ru' ? nameError : undefined}
                >
                    {(control) => <Input {...control} value={values.names[language]} onChange={changeName(language)} disabled={busy}/>}
                </FormField>
            ))}
            {kind === 'hardware' && (
                <>
                    <GroupField/>
                    <PlatformsField/>
                </>
            )}
            <FormActions>
                {actions}
                <Button type="submit" loading={busy}>{submitLabel}</Button>
            </FormActions>
        </Form>
    );
}

function GroupField() {
    const {t} = useTranslation();
    const {values, errors, refusals, groups, changeGroup, busy} = useHardwareForm();
    return (
        <FormField label={t('hardware.group')} required error={errors.group === true ? t('hardware.groupRequired') : refusals.group}>
            {(control) => (
                <Select
                    {...control}
                    value={values.group}
                    onChange={changeGroup}
                    options={(groups ?? []).map((group) => ({value: group.code, label: group.name}))}
                    placeholder={t('hardware.chooseGroup')}
                    disabled={busy || groups === undefined}
                />
            )}
        </FormField>
    );
}

function PlatformsField() {
    const {t} = useTranslation();
    const {values, errors, refusals, platforms, changePlatform, busy} = useHardwareForm();
    return (
        <FormFieldset legend={t('hardware.platforms')} required error={errors.platforms === true ? t('hardware.platformRequired') : refusals.platforms}>
            {platforms === undefined
                ? <Skeleton className={styles.loading}/>
                : (
                    <Inline gap="md" wrap>
                        {platforms.map((platform) => (
                            <Checkbox
                                key={platform.code}
                                label={platform.name}
                                checked={values.platforms.includes(platform.code)}
                                onChange={(checked) => {
                                    changePlatform(platform.code, checked);
                                }}
                                disabled={busy}
                            />
                        ))}
                    </Inline>
                )}
        </FormFieldset>
    );
}
