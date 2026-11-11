import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import styles from './AuthorFormSkeleton.module.css';

/** Loading placeholder shaped like AuthorForm: the real name, the labels, the locations, the notes and the buttons. */
export function AuthorFormSkeleton() {
    return (
        <Stack gap="lg" className={styles.form}>
            {['realName', 'labels', 'locations'].map((field) => (
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
