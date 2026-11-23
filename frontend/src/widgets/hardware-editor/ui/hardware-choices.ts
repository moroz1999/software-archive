import {useQuery} from '@tanstack/react-query';
import {type HardwareGroupItem, hardwareGroupsQuery, type HardwareKind, type HardwarePlatformChoice} from '@/entities/hardware';
import {platformsQuery} from '@/entities/platforms';

export interface HardwareChoices {
    readonly groups: readonly HardwareGroupItem[] | undefined;
    readonly platforms: readonly HardwarePlatformChoice[] | undefined;
}

/** The groups and the platforms an item of hardware is put in; a group needs neither. */
export function useHardwareChoices(kind: HardwareKind): HardwareChoices {
    const enabled = kind === 'hardware';
    const groups = useQuery({...hardwareGroupsQuery(), enabled});
    const platforms = useQuery({...platformsQuery(), enabled});
    return {groups: groups.data, platforms: platforms.data};
}
