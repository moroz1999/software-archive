import {useState} from 'react';
import {describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {renderWithQueries} from '@/shared/lib/test';
import {MemberRoleEditor, type MemberRoleValue} from './MemberRoleEditor';

const ALIAS = '00000000-0000-0000-00bb-000000000002';

function Editor() {
    const [value, setValue] = useState<MemberRoleValue[]>([]);
    return (
        <>
            <MemberRoleEditor
                label="Members"
                value={value}
                roles={[{code: 'coder', title: 'Coder'}, {code: 'organizer', title: 'Organizer'}]}
                search={vi.fn(() => Promise.resolve([{id: ALIAS, type: 'author-label', title: 'J.D. — Jane Doe'}]))}
                onChange={(next) => { setValue([...next]); }}
                showDates
            />
            <output>{JSON.stringify(value)}</output>
        </>
    );
}

describe(MemberRoleEditor, () => {
    it('adds an alias with several roles and membership dates', async () => {
        renderWithQueries(<Editor/>);

        await userEvent.type(screen.getByRole('combobox', {name: 'Members'}), 'j.d.');
        await userEvent.click(await screen.findByRole('option', {name: 'J.D. — Jane Doe'}));
        await userEvent.click(screen.getByRole('button', {name: 'Add role to J.D. — Jane Doe'}));
        await userEvent.click(screen.getByRole('option', {name: 'Coder'}));
        await userEvent.click(screen.getByRole('button', {name: 'Add role to J.D. — Jane Doe'}));
        await userEvent.click(screen.getByRole('option', {name: 'Organizer'}));
        await userEvent.type(screen.getByRole('textbox', {name: 'Start date for J.D. — Jane Doe'}), '1990-01-01');
        await userEvent.type(screen.getByRole('textbox', {name: 'End date for J.D. — Jane Doe'}), '1995-12-31');

        expect(screen.getByRole('status')).toHaveTextContent(JSON.stringify([{
            id: ALIAS,
            type: 'author-label',
            title: 'J.D. — Jane Doe',
            roles: ['coder', 'organizer'],
            startDate: '1990-01-01',
            endDate: '1995-12-31',
        }]));
    });
});
