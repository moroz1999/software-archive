import {useCallback, useEffect, useRef, useState} from 'react';
import {classNames} from '@/shared/lib/class-names';
import {Button} from '../button';
import styles from './FileUpload.module.css';

export interface FileUploadProps {
    /** The text of the button that opens the choice of files. */
    readonly label: string;
    /** The kinds of files offered for choice, as the `accept` of a file input. */
    readonly accept?: string;
    readonly multiple?: boolean;
    readonly disabled?: boolean;
    /** Called with the files chosen or dropped, never with none. */
    readonly onFiles: (files: readonly File[]) => void;
}

/** Choosing files by a button, or dropping them on it. */
export function FileUpload({label, accept, multiple = false, disabled = false, onFiles}: FileUploadProps) {
    const input = useRef<HTMLInputElement>(null);
    const zone = useRef<HTMLDivElement>(null);
    const [over, setOver] = useState(false);

    const take = useCallback((files: FileList | readonly File[] | null) => {
        const taken = files === null ? [] : Array.from(files);
        if (!disabled && taken.length > 0) {
            onFiles(multiple ? taken : taken.slice(0, 1));
        }
    }, [disabled, multiple, onFiles]);

    // Dropping is a pointer's shortcut to the button, which stays the way in for everyone: the zone listens itself
    // instead of becoming a control of its own.
    useEffect(() => {
        const element = zone.current;
        if (element === null) {
            return undefined;
        }
        const onDragOver = (event: DragEvent) => {
            if (!disabled && event.dataTransfer?.types.includes('Files') === true) {
                event.preventDefault();
                setOver(true);
            }
        };
        const onDragLeave = () => {
            setOver(false);
        };
        const onDrop = (event: DragEvent) => {
            event.preventDefault();
            setOver(false);
            take(event.dataTransfer?.files ?? null);
        };
        element.addEventListener('dragover', onDragOver);
        element.addEventListener('dragleave', onDragLeave);
        element.addEventListener('drop', onDrop);
        return () => {
            element.removeEventListener('dragover', onDragOver);
            element.removeEventListener('dragleave', onDragLeave);
            element.removeEventListener('drop', onDrop);
        };
    }, [disabled, take]);

    return (
        <div ref={zone} className={classNames(styles.zone, over && styles.over)}>
            <input
                ref={input}
                type="file"
                aria-label={label}
                accept={accept}
                multiple={multiple}
                disabled={disabled}
                className={styles.input}
                onChange={(event) => {
                    take(event.currentTarget.files);
                    // Cleared, so choosing the same file again is a change too.
                    event.currentTarget.value = '';
                }}
            />
            <Button color="outlined" disabled={disabled} onClick={() => {
                input.current?.click();
            }}>{label}</Button>
        </div>
    );
}
