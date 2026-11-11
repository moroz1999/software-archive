import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import styles from './CategoryFormSkeleton.module.css';

/** Loading placeholder shaped like CategoryForm: the title, the parent and the buttons. */
export function CategoryFormSkeleton() {
    return (
        <Stack gap="lg" className={styles.form}>
            {['title', 'parent'].map((field) => (
                <Stack key={field} gap="xs">
                    <Skeleton className={styles.label}/>
                    <Skeleton className={styles.input}/>
                </Stack>
            ))}
            <Skeleton className={styles.button}/>
        </Stack>
    );
}
