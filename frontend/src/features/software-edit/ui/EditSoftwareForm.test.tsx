import {afterEach, describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {toReleaseDate, toSoftwareId, toSoftwareTitle} from '../../../entities/software';
import {json, noContent, stubFetch} from '../../../shared/lib/test';
import {EditSoftwareForm} from './EditSoftwareForm';

const SOFTWARE = {
    id: toSoftwareId('00000000-0000-0000-0001-000000000001'),
    title: toSoftwareTitle('Elite'),
    description: 'Space trading',
    releaseDate: toReleaseDate('1985-09-20'),
};

describe(EditSoftwareForm, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('saves the edited software', async () => {
        const fetchMock = stubFetch(noContent());
        const onSaved = vi.fn<() => void>();
        render(<EditSoftwareForm software={SOFTWARE} onSaved={onSaved}/>);

        const title = screen.getByRole('textbox', {name: 'Title'});
        await userEvent.clear(title);
        await userEvent.type(title, 'Elite Plus');
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await vi.waitFor(() => {
            expect(onSaved).toHaveBeenCalledExactlyOnceWith();
        });

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/software/${SOFTWARE.id}`);
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({
            method: 'PUT',
            body: '{"title":"Elite Plus","description":"Space trading","releaseDate":"1985-09-20"}',
        });
    });

    it('says so in the form when the software has been deleted meanwhile', async () => {
        stubFetch(json({status: 404, code: 'software.not-found'}, 404));
        const onSaved = vi.fn<() => void>();
        render(<EditSoftwareForm software={SOFTWARE} onSaved={onSaved}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await expect(screen.findByRole('alert')).resolves.toHaveTextContent('This software has been deleted.');
        expect(onSaved).not.toHaveBeenCalled();
    });
});
