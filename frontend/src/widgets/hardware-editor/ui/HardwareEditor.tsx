import type {ReactNode} from 'react';
import {useTranslation} from 'react-i18next';
import {
    type HardwareCode,
    HardwareDetailsProvider,
    HardwareForm,
    HardwareFormSkeleton,
    type HardwareGroupCode,
    type HardwareKind,
    HARDWARE_PATH,
    useHardwareDetails,
} from '@/entities/hardware';
import {DeleteHardwareButton} from '@/features/hardware/delete';
import {EditHardwareProvider} from '@/features/hardware/edit';
import {useSkeleton} from '@/shared/lib/loading';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';
import {useHardwareChoices} from './hardware-choices';

export interface HardwareEditorProps {
    readonly kind: HardwareKind;
    readonly code: HardwareCode | HardwareGroupCode;
    /** Called once the group or the item is saved. */
    readonly onSaved: () => void;
    /** Called once it is deleted. */
    readonly onDeleted: () => void;
    /** Navigation shared by the management pages. */
    readonly navigation?: ReactNode;
}

/** The page of editing a group or an item of hardware: Delete next to the title, the form with Cancel and Save under it. */
export function HardwareEditor(props: HardwareEditorProps) {
    return (
        <HardwareDetailsProvider key={`${props.kind}:${props.code}`} kind={props.kind} code={props.code}>
            <LoadedEditor {...props}/>
        </HardwareDetailsProvider>
    );
}

function LoadedEditor({kind, onSaved, onDeleted, navigation}: HardwareEditorProps) {
    const {t} = useTranslation();
    const query = useHardwareDetails();
    const skeleton = useSkeleton(query);
    const {groups, platforms} = useHardwareChoices(kind);
    const title = t(kind === 'group' ? 'actions.editGroup' : 'actions.editHardware');

    if (skeleton || query.isPending) {
        return (
            <PageLayout title={title}>
                {navigation}
                <div data-testid="hardware-editor-skeleton"><HardwareFormSkeleton/></div>
            </PageLayout>
        );
    }
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    return (
        <PageLayout title={title} actions={<DeleteHardwareButton hardware={query.data} onDeleted={onDeleted}/>}>
            {navigation}
            <EditHardwareProvider hardware={query.data} groups={groups} platforms={platforms} onSaved={onSaved}>
                <HardwareForm
                    submitLabel={t('actions.save')}
                    actions={<Button href={HARDWARE_PATH} color="secondary">{t('actions.cancel')}</Button>}
                />
            </EditHardwareProvider>
        </PageLayout>
    );
}
