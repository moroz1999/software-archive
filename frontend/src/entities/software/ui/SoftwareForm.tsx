import type {ReactNode} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {Form, FormActions, FormField, FormMessage, FormSection} from '@/shared/ui/form';
import {Input} from '@/shared/ui/input';
import {Select} from '@/shared/ui/select';
import {Skeleton} from '@/shared/ui/skeleton';
import {Textarea} from '@/shared/ui/textarea';
import {useSkeleton} from '@/shared/lib/loading';
import {PLATFORM_REQUIRED_KEY, RELEASE_DATE_HINT_KEY, RELEASE_DATE_INVALID_KEY, TITLE_PROBLEM_KEYS} from '../config/messages';
import {usePlatformHardware} from '../model/platform-hardware-context';
import {PlatformHardwareProvider} from '../model/PlatformHardwareProvider';
import {PRESERVATION_STATUSES, toSoftwarePlatformCode} from '../model/software';
import {useSoftwareForm} from '../model/software-form-context';
import {CreditsEditor} from './CreditsEditor';
import {HardwarePicker} from './HardwarePicker';
import styles from './SoftwareForm.module.css';

export interface SoftwareFormProps {
    readonly submitLabel: string;
    /** More buttons before the submit button, such as Cancel. */
    readonly actions?: ReactNode;
    /** The field of the categories of the software, under its hardware: the tree of categories is not of this slice. */
    readonly categories?: ReactNode;
}

/** The fields of software inside a SoftwareFormProvider, which holds their values and checks them. */
export function SoftwareForm({submitLabel, actions, categories}: SoftwareFormProps) {
    const {t} = useTranslation();
    const {values, errors, refusals, change, changeCredits, submit, platforms, busy, error} = useSoftwareForm();
    // Hardware belongs to its platform: the platform stays while hardware is chosen (docs/domain/hardware.md).
    const platformLocked = values.hardware.length > 0;

    return (
        <Form onSubmit={submit} columns={2}>
            {error !== undefined && <FormMessage>{error}</FormMessage>}
            <FormSection title={t('formSections.main')}>
                <FormField label={t('fields.title')} required fullWidth error={errors.title === undefined ? refusals.title : t(TITLE_PROBLEM_KEYS[errors.title])}>
                    {(control) => <Input {...control} value={values.title} onChange={change('title')} disabled={busy}/>}
                </FormField>
                <FormField
                    label={t('fields.platform')}
                    required
                    error={errors.platform === undefined ? refusals.platform : t(PLATFORM_REQUIRED_KEY)}
                    hint={platformLocked ? t('fields.platformLocked') : undefined}
                >
                    {(control) => (
                        <Select
                            {...control}
                            value={values.platform}
                            onChange={change('platform')}
                            options={platforms.map((platform) => ({value: platform.code, label: platform.name}))}
                            placeholder={t('fields.choosePlatform')}
                            disabled={busy || platformLocked}
                        />
                    )}
                </FormField>
                <FormField
                    label={t('fields.releaseDate')}
                    hint={t(RELEASE_DATE_HINT_KEY)}
                    error={errors.releaseDate === undefined ? refusals.releaseDate : t(RELEASE_DATE_INVALID_KEY)}
                >
                    {(control) => <Input {...control} value={values.releaseDate} onChange={change('releaseDate')} disabled={busy}/>}
                </FormField>
                <FormField label={t('fields.preservation')} error={refusals.preservation}>
                    {(control) => (
                        <Select
                            {...control}
                            value={values.preservation ?? 'unknown'}
                            onChange={change('preservation')}
                            options={PRESERVATION_STATUSES.map((status) => ({value: status, label: t(`software.preservation.${status}`)}))}
                            disabled={busy}
                        />
                    )}
                </FormField>
                <FormField label={t('fields.description')} fullWidth error={refusals.description}>
                    {(control) => <Textarea {...control} value={values.description} onChange={change('description')} disabled={busy}/>}
                </FormField>
            </FormSection>
            <FormSection title={t('formSections.classification')} columns={1}>
                {values.platform !== '' && (
                    <PlatformHardwareProvider platform={toSoftwarePlatformCode(values.platform)}>
                        <SoftwareHardware/>
                    </PlatformHardwareProvider>
                )}
                {categories}
            </FormSection>
            <FormSection title={t('formSections.credits')} columns={1}>
                <CreditsEditor value={values.credits ?? []} onChange={changeCredits} disabled={busy} error={refusals.credits}/>
            </FormSection>
            <FormActions>
                {actions}
                <Button type="submit" loading={busy}>{submitLabel}</Button>
            </FormActions>
        </Form>
    );
}

/** The hardware of the chosen platform to choose the software's own from. */
function SoftwareHardware() {
    const {values, refusals, changeHardware, busy} = useSoftwareForm();
    const query = usePlatformHardware();
    const skeleton = useSkeleton(query);

    if (skeleton || query.isPending) {
        return <Skeleton className={styles.hardwareBone}/>;
    }
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    return <HardwarePicker hardware={query.data} value={values.hardware} onChange={changeHardware} disabled={busy} error={refusals.hardware}/>;
}
