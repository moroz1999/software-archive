import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import styles from './EditUserFormSkeleton.module.css';

/** Loading placeholder shaped like EditUserForm: the name, the email, the groups and the buttons. */
export function EditUserFormSkeleton() {
    return (
        <Stack gap="lg" className={styles.form}>
            {['name', 'email'].map((field) => (
                <Stack key={field} gap="xs">
                    <Skeleton className={styles.label}/>
                    <Skeleton className={styles.input}/>
                </Stack>
            ))}
            {['admin', 'moderator', 'ai-assistant'].map((group) => <Skeleton key={group} className={styles.label}/>)}
            <Skeleton className={styles.button}/>
        </Stack>
    );
}
