import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import styles from './SoftwareFormSkeleton.module.css';

/** Loading placeholder shaped like SoftwareForm: three labelled fields and the buttons. */
export function SoftwareFormSkeleton() {
    return (
        <Stack gap="lg">
            <Stack gap="xs">
                <Skeleton className={styles.label}/>
                <Skeleton className={styles.input}/>
            </Stack>
            <Stack gap="xs">
                <Skeleton className={styles.label}/>
                <Skeleton className={styles.textarea}/>
            </Stack>
            <Stack gap="xs">
                <Skeleton className={styles.label}/>
                <Skeleton className={styles.input}/>
            </Stack>
            <Skeleton className={styles.button}/>
        </Stack>
    );
}
