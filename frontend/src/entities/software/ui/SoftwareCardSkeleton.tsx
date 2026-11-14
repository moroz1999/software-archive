import {Inline} from '@/shared/ui/inline';
import {Inset} from '@/shared/ui/inset';
import {Panel} from '@/shared/ui/panel';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import styles from './SoftwareCardSkeleton.module.css';

/** Loading placeholder shaped like SoftwareCard. */
export function SoftwareCardSkeleton() {
    return (
        <Panel radius="xl" padding="none" contentBleed>
            <Skeleton className={styles.image}/>
            <Inset padding="lg">
                <Stack gap="md">
                    <Skeleton className={styles.title}/>
                    <Inline gap="sm" justify="between" align="center">
                        <Skeleton className={styles.madeBy}/>
                        <Skeleton className={styles.year}/>
                    </Inline>
                </Stack>
            </Inset>
        </Panel>
    );
}
