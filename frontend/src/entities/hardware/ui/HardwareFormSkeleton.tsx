import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import styles from './HardwareFormSkeleton.module.css';

/** Loading placeholder shaped like HardwareForm: the code, the names and the buttons. */
export function HardwareFormSkeleton() {
    return (
        <Stack gap="lg" className={styles.form}>
            {['code', 'ru', 'en'].map((field) => (
                <Stack key={field} gap="xs">
                    <Skeleton className={styles.label}/>
                    <Skeleton className={styles.input}/>
                </Stack>
            ))}
            <Skeleton className={styles.button}/>
        </Stack>
    );
}
