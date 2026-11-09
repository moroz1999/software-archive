import type {ReactNode} from 'react';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {act, renderHook} from '@testing-library/react';
import {toPlatformCode} from './platform';
import {usePlatformSelection} from './platform-selection-context';
import {PlatformSelectionProvider} from './PlatformSelectionProvider';

function renderSelection() {
    return renderHook(() => usePlatformSelection(), {
        wrapper: ({children}: {readonly children: ReactNode}) => <PlatformSelectionProvider>{children}</PlatformSelectionProvider>,
    });
}

describe(PlatformSelectionProvider, () => {
    afterEach(() => {
        localStorage.clear();
        vi.restoreAllMocks();
    });

    it('starts from all platforms when nothing was chosen', () => {
        expect(renderSelection().result.current.platform).toBeNull();
    });

    it('starts from the platform kept in the browser', () => {
        localStorage.setItem('archive.platform', 'vector');

        expect(renderSelection().result.current.platform).toBe('vector');
    });

    it('hands another choice on and keeps it in the browser', () => {
        const {result} = renderSelection();

        act(() => {
            result.current.select(toPlatformCode('bk'));
        });

        expect(result.current.platform).toBe('bk');
        expect(localStorage.getItem('archive.platform')).toBe('bk');
    });

    it('hands a choice on even when the browser refuses its storage', () => {
        vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
            throw new DOMException('denied', 'QuotaExceededError');
        });
        const {result} = renderSelection();

        act(() => {
            result.current.select(toPlatformCode('agat'));
        });

        expect(result.current.platform).toBe('agat');
    });
});
