import {describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {ErrorState} from './ErrorState';

describe(ErrorState, () => {
    it('announces the message', () => {
        render(<ErrorState message="Software not found."/>);

        expect(screen.getByRole('alert')).toHaveTextContent('Software not found.');
    });

    it('offers a retry when given one', async () => {
        const onRetry = vi.fn<() => void>();
        render(<ErrorState message="Failed." onRetry={onRetry}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Retry'}));

        expect(onRetry).toHaveBeenCalledExactlyOnceWith();
    });
});
