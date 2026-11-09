import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import styles from './GroupView.module.css';

/** Loading placeholder shaped like the group's details: a few labelled sections of short lines. */
export function GroupViewSkeleton() {
    return (
        <Stack gap="lg">
            {[1, 2, 3].map((section) => (
                <Stack key={section} gap="xs">
                    <Skeleton className={styles.labelBone}/>
                    <Skeleton className={styles.lineBone}/>
                    <Skeleton className={styles.lineBone}/>
                </Stack>
            ))}
        </Stack>
    );
}
