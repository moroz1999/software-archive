import type {ReactNode} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {Form, FormActions, FormField, FormMessage, FormSection} from '@/shared/ui/form';
import {Input} from '@/shared/ui/input';
import {MultiEntityAutocomplete} from '@/shared/ui/multi-entity-autocomplete';
import {MemberRoleEditor} from '@/shared/ui/member-role-editor';
import {Select} from '@/shared/ui/select';
import {Skeleton} from '@/shared/ui/skeleton';
import {Textarea} from '@/shared/ui/textarea';
import {useSkeleton} from '@/shared/lib/loading';
import {PLATFORM_REQUIRED_KEY, RELEASE_DATE_HINT_KEY, RELEASE_DATE_INVALID_KEY, TITLE_PROBLEM_KEYS} from '../config/messages';
import {fetchSoftwarePeople, fetchSoftwareReferences} from '../api/software-api';
import {usePlatformHardware} from '../model/platform-hardware-context';
import {PlatformHardwareProvider} from '../model/PlatformHardwareProvider';
import {type SoftwareReferenceType, toSoftwarePlatformCode} from '../model/software';
import {useSoftwareForm} from '../model/software-form-context';
import {HardwarePicker} from './HardwarePicker';
import styles from './SoftwareForm.module.css';

const PUBLISHER_TYPES = ['author', 'author-label', 'group', 'group-label'] as const satisfies readonly SoftwareReferenceType[];
const DEVELOPER_TYPES = ['group', 'group-label'] as const satisfies readonly SoftwareReferenceType[];

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
    const {values, errors, refusals, change, changePublishers, changeDevelopers, changeCredits, submit, platforms, busy, error} = useSoftwareForm();
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
            <FormSection title={t('formSections.relations')}>
                <FormField label={t('fields.publishers')} error={refusals.publishers}>
                    {(control) => (
                        <MultiEntityAutocomplete
                            {...control}
                            value={values.publishers ?? []}
                            onChange={changePublishers}
                            load={(query, signal) => fetchSoftwareReferences(PUBLISHER_TYPES, query, signal)}
                            queryKey="software-publishers"
                            disabled={busy}
                        />
                    )}
                </FormField>
                <FormField label={t('fields.developers')} error={refusals.developers}>
                    {(control) => (
                        <MultiEntityAutocomplete
                            {...control}
                            value={values.developers ?? []}
                            onChange={changeDevelopers}
                            load={(query, signal) => fetchSoftwareReferences(DEVELOPER_TYPES, query, signal)}
                            queryKey="software-developers"
                            disabled={busy}
                        />
                    )}
                </FormField>
            </FormSection>
            <FormSection title={t('formSections.authors')} columns={1}>
                <MemberRoleEditor
                    label={t('fields.credits')}
                    value={values.credits ?? []}
                    onChange={changeCredits}
                    search={fetchSoftwarePeople}
                    roles={SOFTWARE_ROLES.map((code) => ({code, title: t(`roles.software.${code}`)}))}
                    disabled={busy}
                    error={refusals.credits}
                />
            </FormSection>
            <FormActions>
                {actions}
                <Button type="submit" loading={busy}>{submitLabel}</Button>
            </FormActions>
        </Form>
    );
}

const SOFTWARE_ROLES = [
    'unknown', 'code', 'release', 'adaptation', 'restoring', 'music', 'sfx', 'support', 'testing', 'graphics',
    'loading_screen', 'intro_code', 'intro_graphics', 'intro_music', 'organizing', 'direction', '3dmodels', 'design',
    'logo', 'font', 'ascii', 'illustrating', 'tools', 'localization', 'concept', 'gamedesign', 'leveldesign', 'story',
    'text', 'editing', 'translation', 'video', 'guest',
] as const;

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
