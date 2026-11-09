import {useId} from 'react';
import {useQuery} from '@tanstack/react-query';
import {Select} from '@/shared/ui/select';
import {platformsQuery} from '../api/platform-queries';
import {parsePlatformCode} from '../model/platform';
import {usePlatformSelection} from '../model/platform-selection-context';

/**
 * The switch of the platform for the whole archive, at the top of every page (docs/domain/hardware.md): its choices
 * are the platforms of the archive.
 */
export function PlatformSwitch() {
    const id = useId();
    const {platform, select} = usePlatformSelection();
    const platforms = useQuery(platformsQuery());

    return (
        <Select
            id={id}
            ariaLabel="Platform"
            value={platform ?? ''}
            onChange={(value) => {
                select(parsePlatformCode(value === '' ? null : value));
            }}
            options={(platforms.data ?? []).map((item) => ({value: item.code, label: item.name}))}
            placeholder="All platforms"
        />
    );
}
