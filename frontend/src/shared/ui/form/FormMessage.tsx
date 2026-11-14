import type {ReactNode} from 'react';
import {Text} from '../typography';

export interface FormMessageProps {
    readonly children: ReactNode;
    /** An error of the whole form, told at once, or a success. */
    readonly variant?: 'error' | 'success';
}

/** What the form as a whole has to tell, not one of its fields: why a submit was refused, or that it went through. */
export function FormMessage({children, variant = 'error'}: FormMessageProps) {
    return variant === 'error'
        ? <Text role="alert" tone="danger">{children}</Text>
        : <Text role="status">{children}</Text>;
}
