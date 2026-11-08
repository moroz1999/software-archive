import {Inset, Panel, Skeleton, Stack} from '../../../shared/ui';
import styles from './SoftwareCardSkeleton.module.css';

/** Loading placeholder shaped like SoftwareCard. */
export function SoftwareCardSkeleton() {
    return (
        <Panel radius="xl" padding="none" contentBleed>
            <Skeleton className={styles.image}/>
            <Inset padding="lg">
                <Stack gap="md">
                    <Skeleton className={styles.title}/>
                    <Skeleton className={styles.year}/>
                </Stack>
            </Inset>
        </Panel>
    );
}
