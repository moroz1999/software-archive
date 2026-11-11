import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import styles from './CategoryTreeSkeleton.module.css';

/** Loading placeholder shaped like the folded tree: a few rows of the top. */
export function CategoryTreeSkeleton() {
    return (
        <Stack gap="sm">
            {[1, 2, 3, 4, 5, 6].map((row) => <Skeleton key={row} className={styles.row}/>)}
        </Stack>
    );
}
