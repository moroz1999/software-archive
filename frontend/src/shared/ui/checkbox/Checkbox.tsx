import {classNames} from '@/shared/lib/class-names';
import styles from './Checkbox.module.css';

export interface CheckboxProps {
    readonly label: string;
    readonly checked: boolean;
    /** Whether it is checked now, not the event. */
    readonly onChange: (checked: boolean) => void;
    readonly disabled?: boolean;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

/** A yes-or-no choice with its label beside it; the label checks it too. */
export function Checkbox({label, checked, onChange, disabled = false, className}: CheckboxProps) {
    return (
        <label className={classNames(styles.checkbox, disabled && styles.disabled, className)}>
            <input
                type="checkbox"
                className={styles.box}
                checked={checked}
                onChange={(event) => {
                    onChange(event.target.checked);
                }}
                disabled={disabled}
            />
            <span>{label}</span>
        </label>
    );
}
