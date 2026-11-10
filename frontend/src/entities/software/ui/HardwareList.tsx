import {Stack} from '@/shared/ui/stack';
import {Text} from '@/shared/ui/typography';
import {groupHardware} from '../lib/hardware-groups';
import type {Hardware} from '../model/software';

export interface HardwareListProps {
    readonly hardware: readonly Hardware[];
}

/** Hardware by its group: the name of every group over the names of its items. */
export function HardwareList({hardware}: HardwareListProps) {
    const {t} = useTranslation();
    const sections = groupHardware(hardware);
    if (sections.length === 0) {
        return null;
    }
    return (
        <Stack gap="md">
            {sections.map((section) => (
                <div key={section.group} role="group" aria-label={t(`hardwareGroups.${section.group}`)}>
                    <Stack gap="xs">
                        <Text variant="label" tone="muted">{t(`hardwareGroups.${section.group}`)}</Text>
                        <Stack as="ul" gap="xs">
                            {section.items.map((item) => <li key={item.code}><Text>{item.name}</Text></li>)}
                        </Stack>
                    </Stack>
                </div>
            ))}
        </Stack>
    );
}
import {useTranslation} from 'react-i18next';
