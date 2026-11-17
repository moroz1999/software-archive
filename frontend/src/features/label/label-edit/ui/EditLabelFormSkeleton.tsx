import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import styles from './EditLabelFormSkeleton.module.css';

/** Loading placeholder shaped like EditLabelForm: the name, whose it is, the locations, the sources and the buttons. */
export function EditLabelFormSkeleton() {
    return (
        <Stack gap="lg" className={styles.form}>
            {['name', 'owner', 'locations'].map((field) => (
                <Stack key={field} gap="xs">
                    <Skeleton className={styles.label}/>
                    <Skeleton className={styles.input}/>
                </Stack>
            ))}
            <Skeleton className={styles.label}/>
            <Skeleton className={styles.button}/>
        </Stack>
    );
}
