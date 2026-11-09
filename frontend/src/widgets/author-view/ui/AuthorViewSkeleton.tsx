import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import styles from './AuthorView.module.css';

/** Loading placeholder shaped like the author's details: a few labelled sections of short lines. */
export function AuthorViewSkeleton() {
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
