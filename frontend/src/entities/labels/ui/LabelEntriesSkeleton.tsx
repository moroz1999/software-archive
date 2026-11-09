import {Grid} from '@/shared/ui/grid';
import {Skeleton} from '@/shared/ui/skeleton';
import styles from './LabelEntriesSkeleton.module.css';

const BONES = Array.from({length: 24}, (_, index) => index);

/** Loading placeholder shaped like the entries of a catalog of names. */
export function LabelEntriesSkeleton() {
    return (
        <Grid desktopColumns={4} tabletColumns={3} mobileColumns={1} gap="md">
            {BONES.map((bone) => <Skeleton key={bone} className={styles.name}/>)}
        </Grid>
    );
}
