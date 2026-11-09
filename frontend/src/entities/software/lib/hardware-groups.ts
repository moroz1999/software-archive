import {type Hardware, HARDWARE_GROUPS, type HardwareGroup} from '../model/software';

/** Hardware of one group, under the group's name. */
export interface HardwareSection {
    readonly group: HardwareGroup;
    readonly label: string;
    readonly items: readonly Hardware[];
}

const GROUP_LABELS: Readonly<Record<HardwareGroup, string>> = {
    computers: 'Computers',
    storage: 'Storage',
    dos: 'DOS',
    sound: 'Sound',
    controls: 'Controls',
    expansion: 'Expansion',
};

/** Hardware shown by its group, the groups in their fixed order; a group without items is left out. */
export function groupHardware(hardware: readonly Hardware[]): readonly HardwareSection[] {
    return HARDWARE_GROUPS
        .map((group) => ({group, label: GROUP_LABELS[group], items: hardware.filter((item) => item.group === group)}))
        .filter((section) => section.items.length > 0);
}
