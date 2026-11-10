import {classNames} from '@/shared/lib/class-names';
import styles from './Toggle.module.css';

export interface ToggleOption<T extends string> {
    readonly value: T;
    readonly label: string;
}

export interface ToggleProps<T extends string> {
    /** The name of the choice for assistive technology. */
    readonly label: string;
    readonly options: readonly ToggleOption<T>[];
    readonly value: T;
    readonly onChange: (value: T) => void;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

/** One of a few options side by side, the chosen one pressed. */
export function Toggle<T extends string>({label, options, value, onChange, className}: ToggleProps<T>) {
    return (
        <div role="group" aria-label={label} className={classNames(styles.toggle, className)}>
            {options.map((option) => (
                <button
                    key={option.value}
                    type="button"
                    aria-pressed={option.value === value}
                    className={classNames(styles.option, option.value === value && styles.pressed)}
                    onClick={() => {
                        if (option.value !== value) {
                            onChange(option.value);
                        }
                    }}
                >
                    {option.label}
                </button>
            ))}
        </div>
    );
}
