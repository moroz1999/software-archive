import {useEffect, useRef} from 'react';
import {classNames} from '../../lib';
import {trackVisibility} from './visibility';
import styles from './Skeleton.module.css';

export interface SkeletonProps {
    /** Shimmer sweeping over the bone. */
    readonly animated?: boolean;
    /** Sits in a line of text instead of taking its own block. */
    readonly inline?: boolean;
    /** Sizes the bone (width, height, aspect ratio) like the content it stands for; see docs/styles.md. */
    readonly className?: string;
}

/** A bone: the building block of a concrete skeleton shaped like its content. */
export function Skeleton({animated = true, inline = false, className}: SkeletonProps) {
    const ref = useRef<HTMLSpanElement>(null);

    useEffect(() => {
        const element = ref.current;
        return element === null ? undefined : trackVisibility(element);
    }, []);

    return (
        <span
            ref={ref}
            aria-hidden="true"
            className={classNames(styles.bone, animated && styles.animated, inline && styles.inline, className)}
        />
    );
}
