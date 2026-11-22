import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import styles from './PlaceFormSkeleton.module.css';

/** Loading placeholder shaped like PlaceForm: the names, the coordinates and the buttons. */
export function PlaceFormSkeleton() {
    return (
        <Stack gap="lg" className={styles.form}>
            {['ru', 'en', 'latitude', 'longitude'].map((field) => (
                <Stack key={field} gap="xs">
                    <Skeleton className={styles.label}/>
                    <Skeleton className={styles.input}/>
                </Stack>
            ))}
            <Skeleton className={styles.button}/>
        </Stack>
    );
}
