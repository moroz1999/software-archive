import {useTranslation} from 'react-i18next';
import {useSkeleton} from '@/shared/lib/loading';
import {Panel} from '@/shared/ui/panel';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Heading, Text, TextLink} from '@/shared/ui/typography';
import {hardwareEditPath, hardwareGroupEditPath} from '../config/paths';
import type {HardwareGroupItem} from '../model/hardware';
import {useHardwareGroups} from '../model/hardware-context';
import styles from './HardwareGroupsList.module.css';

/**
 * Every group from the HardwareGroupsProvider around in its order, each with its hardware by name; the group and each
 * item lead to their editing.
 */
export function HardwareGroupsList() {
    const query = useHardwareGroups();
    const skeleton = useSkeleton(query);

    if (skeleton || query.isPending) {
        return (
            <div data-testid="hardware-groups-skeleton">
                <Stack gap="sm">
                    {[1, 2, 3, 4, 5, 6].map((row) => <Skeleton key={row} className={styles.row}/>)}
                </Stack>
            </div>
        );
    }
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    return (
        <Stack gap="lg">
            {query.data.map((group) => <Group key={group.code} group={group}/>)}
        </Stack>
    );
}

function Group({group}: {readonly group: HardwareGroupItem}) {
    const {t} = useTranslation();
    return (
        <section aria-label={group.name}>
            <Panel>
                <Stack gap="sm">
                    <Heading level={2} variant="title">
                        <TextLink href={hardwareGroupEditPath(group.code)}>{group.name}</TextLink>
                    </Heading>
                    {group.hardware.length === 0
                        ? <Text tone="muted">{t('hardware.emptyGroup')}</Text>
                        : (
                            <Stack as="ul" gap="xs">
                                {group.hardware.map((item) => (
                                    <li key={item.code}><TextLink href={hardwareEditPath(item.code)}>{item.name}</TextLink></li>
                                ))}
                            </Stack>
                        )}
                </Stack>
            </Panel>
        </section>
    );
}
