import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import styles from './SoftwareView.module.css';

/** Loading placeholder shaped like the software's details: the release day and a few lines of description. */
export function SoftwareViewSkeleton() {
    return (
        <Stack gap="lg">
            <Stack gap="xs">
                <Skeleton className={styles.labelBone}/>
                <Skeleton className={styles.lineBone}/>
            </Stack>
            <Stack gap="sm">
                <Skeleton className={styles.textBone}/>
                <Skeleton className={styles.textBone}/>
                <Skeleton className={styles.textBone}/>
            </Stack>
        </Stack>
    );
}
