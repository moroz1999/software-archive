import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import styles from './GroupFormSkeleton.module.css';

/** Loading placeholder shaped like GroupForm: the name, the types, the labels, the locations, the notes and the buttons. */
export function GroupFormSkeleton() {
    return (
        <Stack gap="lg" className={styles.form}>
            {['name', 'types', 'labels', 'locations'].map((field) => (
                <Stack key={field} gap="xs">
                    <Skeleton className={styles.label}/>
                    <Skeleton className={styles.input}/>
                </Stack>
            ))}
            <Stack gap="xs">
                <Skeleton className={styles.label}/>
                <Skeleton className={styles.textarea}/>
            </Stack>
            <Skeleton className={styles.button}/>
        </Stack>
    );
}
