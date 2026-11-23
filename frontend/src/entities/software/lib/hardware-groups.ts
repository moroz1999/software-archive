import type {Hardware} from '../model/software';

/** Hardware of one group, under the group's name. */
export interface HardwareSection {
    readonly group: string;
    readonly label: string;
    readonly items: readonly Hardware[];
}

/** Hardware shown by its group, the groups in the order the archive gives them; a group without items is left out. */
export function groupHardware(hardware: readonly Hardware[]): readonly HardwareSection[] {
    const sections = new Map<string, {group: string; label: string; items: Hardware[]}>();
    for (const item of hardware) {
        const section = sections.get(item.group);
        if (section === undefined) {
            sections.set(item.group, {group: item.group, label: item.groupName, items: [item]});
        } else {
            section.items.push(item);
        }
    }
    return [...sections.values()];
}
