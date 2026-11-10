import type {ReactNode, SyntheticEvent} from 'react';
import {Button} from '@/shared/ui/button';
import {Field} from '@/shared/ui/field';
import {Inline} from '@/shared/ui/inline';
import {Input} from '@/shared/ui/input';
import {Select} from '@/shared/ui/select';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Textarea} from '@/shared/ui/textarea';
import {Text} from '@/shared/ui/typography';
import {useSkeleton} from '@/shared/lib/skeleton';
import {PLATFORM_REQUIRED_MESSAGE, RELEASE_DATE_INVALID_MESSAGE, TITLE_PROBLEM_MESSAGES} from '../config/messages';
import {usePlatformHardware} from '../model/platform-hardware-context';
import {PlatformHardwareProvider} from '../model/PlatformHardwareProvider';
import {toSoftwarePlatformCode} from '../model/software';
import {useSoftwareForm} from '../model/software-form-context';
import {HardwarePicker} from './HardwarePicker';
import styles from './SoftwareForm.module.css';

export interface SoftwareFormProps {
    readonly submitLabel: string;
    /** More buttons next to the submit button. */
    readonly actions?: ReactNode;
}

/** The fields of software inside a SoftwareFormProvider, which holds their values and checks them. */
export function SoftwareForm({submitLabel, actions}: SoftwareFormProps) {
    const {values, errors, refusals, change, submit, platforms, busy, error} = useSoftwareForm();
    // Hardware belongs to its platform: the platform stays while hardware is chosen (docs/domain/hardware.md).
    const platformLocked = values.hardware.length > 0;

    function onSubmit(event: SyntheticEvent) {
        event.preventDefault();
        submit();
    }

    return (
        <form noValidate onSubmit={onSubmit}>
            <Stack gap="lg">
                {error !== undefined && <Text role="alert" tone="danger">{error}</Text>}
                <Field label="Title" error={errors.title === undefined ? refusals.title : TITLE_PROBLEM_MESSAGES[errors.title]}>
                    {(control) => <Input {...control} value={values.title} onChange={change('title')} disabled={busy}/>}
                </Field>
                <Field
                    label="Platform"
                    error={errors.platform === undefined ? refusals.platform : PLATFORM_REQUIRED_MESSAGE}
                    hint={platformLocked ? 'Remove the hardware to change the platform.' : undefined}
                >
                    {(control) => (
                        <Select
                            {...control}
                            value={values.platform}
                            onChange={change('platform')}
                            options={platforms.map((platform) => ({value: platform.code, label: platform.name}))}
                            placeholder="Choose a platform"
                            disabled={busy || platformLocked}
                        />
                    )}
                </Field>
                <Field label="Description" error={refusals.description}>
                    {(control) => <Textarea {...control} value={values.description} onChange={change('description')} disabled={busy}/>}
                </Field>
                <Field label="Release date" error={errors.releaseDate === undefined ? refusals.releaseDate : RELEASE_DATE_INVALID_MESSAGE}>
                    {(control) => <Input {...control} type="date" value={values.releaseDate} onChange={change('releaseDate')} disabled={busy}/>}
                </Field>
                {values.platform !== '' && (
                    <PlatformHardwareProvider platform={toSoftwarePlatformCode(values.platform)}>
                        <SoftwareHardware/>
                    </PlatformHardwareProvider>
                )}
                <Inline gap="sm" justify="between" wrap>
                    <Button type="submit" loading={busy}>{submitLabel}</Button>
                    {actions}
                </Inline>
            </Stack>
        </form>
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
