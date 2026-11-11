import {classNames} from '@/shared/lib/class-names';
import styles from './Radio.module.css';

export interface RadioProps {
    readonly label: string;
    /** The group it belongs to: one radio of a group is chosen at a time. */
    readonly name: string;
    readonly checked: boolean;
    /** Called when it gets chosen. */
    readonly onChoose: () => void;
    readonly disabled?: boolean;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

/** One choice of a group with its label beside it; the label chooses it too. */
export function Radio({label, name, checked, onChoose, disabled = false, className}: RadioProps) {
    return (
        <label className={classNames(styles.radio, disabled && styles.disabled, className)}>
            <input type="radio" name={name} className={styles.circle} checked={checked} onChange={() => {
                onChoose();
            }} disabled={disabled}/>
            <span>{label}</span>
        </label>
    );
}
