import {Button} from '../button';
import {Inline} from '../inline';
import {Text} from '../typography';

export interface ErrorStateProps {
    readonly message: string;
    readonly onRetry?: () => void;
}

/** A business error of a component's own action, shown inside that component. */
export function ErrorState({message, onRetry}: ErrorStateProps) {
    return (
        <div role="alert">
            <Inline gap="md" align="center" wrap>
                <Text tone="danger">{message}</Text>
                {onRetry !== undefined && <Button size="sm" color="secondary" onClick={onRetry}>Retry</Button>}
            </Inline>
        </div>
    );
}
