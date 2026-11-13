import {useState} from 'react';
import {classNames} from '@/shared/lib/class-names';
import styles from './CardScreenshots.module.css';

export interface CardScreenshotsProps {
    /** The addresses of the pictures, in their order; at least one. */
    readonly urls: readonly string[];
    /** What the pictures show, for assistive technology. */
    readonly alt: string;
}

/**
 * The screenshots of a card in a 4:3 frame: the first is shown, and pointing across the frame goes through the others,
 * each owning an equal strip of its width; leaving the frame shows the first again.
 */
export function CardScreenshots({urls, alt}: CardScreenshotsProps) {
    // The pointer is over a picture of these pictures; other pictures start from the first again.
    const pictures = urls.join('\n');
    const [pointed, setPointed] = useState<{readonly pictures: string; readonly index: number} | null>(null);
    const shown = pointed?.pictures === pictures ? pointed.index : 0;

    return (
        <div className={styles.frame} onPointerLeave={() => {
            setPointed(null);
        }}>
            {urls.map((url, index) => (
                <img
                    key={url}
                    src={url}
                    // Only the shown picture speaks: the others are the same thing a pointer away.
                    alt={index === shown ? alt : ''}
                    loading="lazy"
                    className={classNames(styles.picture, index === shown && styles.shown)}
                />
            ))}
            {urls.length > 1 && (
                <div className={styles.strips}>
                    {urls.map((url, index) => (
                        <div
                            key={url}
                            className={classNames(styles.strip, index === shown && styles.active)}
                            onPointerEnter={() => {
                                setPointed({pictures, index});
                            }}
                        />
                    ))}
                </div>
            )}
        </div>
    );
}
