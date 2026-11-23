import type {ReactNode} from 'react';
import {useTranslation} from 'react-i18next';
import {HARDWARE_PATH, HardwareForm, type HardwareKind} from '@/entities/hardware';
import {CreateHardwareProvider} from '@/features/hardware/create';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';
import {useHardwareChoices} from './hardware-choices';

export interface HardwareCreatorProps {
    readonly kind: HardwareKind;
    /** Called once the group or the item is added. */
    readonly onCreated: () => void;
    /** Navigation shared by the management pages. */
    readonly navigation?: ReactNode;
}

/** The page of adding a group or an item of hardware: its code, names, and an item's group and platforms; Cancel returns to the list. */
export function HardwareCreator({kind, onCreated, navigation}: HardwareCreatorProps) {
    const {t} = useTranslation();
    const {groups, platforms} = useHardwareChoices(kind);
    const title = t(kind === 'group' ? 'actions.addGroup' : 'actions.addHardware');
    return (
        <PageLayout title={title}>
            {navigation}
            <CreateHardwareProvider kind={kind} groups={groups} platforms={platforms} onCreated={onCreated}>
                <HardwareForm
                    submitLabel={title}
                    actions={<Button href={HARDWARE_PATH} color="secondary">{t('actions.cancel')}</Button>}
                />
            </CreateHardwareProvider>
        </PageLayout>
    );
}
