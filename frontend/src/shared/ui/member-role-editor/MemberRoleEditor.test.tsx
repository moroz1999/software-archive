import {useState} from 'react';
import {describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {renderWithQueries} from '@/shared/lib/test';
import {MemberRoleEditor, type MemberRoleValue} from './MemberRoleEditor';

const ALIAS = '00000000-0000-0000-00bb-000000000002';
const JANE: MemberRoleValue = {id: ALIAS, type: 'author-label', title: 'J.D. — Jane Doe', roles: [], startDate: '', endDate: ''};

function Editor({initial = []}: {readonly initial?: readonly MemberRoleValue[]}) {
    const [value, setValue] = useState<readonly MemberRoleValue[]>(initial);
    return (
        <>
            <MemberRoleEditor
                label="Members"
                value={value}
                roles={[{code: 'coder', title: 'Coder'}, {code: 'organizer', title: 'Organizer'}, {code: 'musician', title: 'Musician'}]}
                search={vi.fn(() => Promise.resolve([{id: ALIAS, type: 'author-label', title: 'J.D. — Jane Doe'}]))}
                onChange={setValue}
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
        await userEvent.click(screen.getByRole('combobox', {name: 'Role for J.D. — Jane Doe'}));
        await userEvent.click(screen.getByRole('option', {name: 'Coder'}));
        await userEvent.click(screen.getByRole('option', {name: 'Organizer'}));
        await userEvent.type(screen.getByRole('textbox', {name: 'Start date for J.D. — Jane Doe'}), '1990-01-01');
        await userEvent.type(screen.getByRole('textbox', {name: 'End date for J.D. — Jane Doe'}), '1995-12-31');

        expect(screen.getByRole('status')).toHaveTextContent(JSON.stringify([{
            ...JANE,
            roles: ['coder', 'organizer'],
            startDate: '1990-01-01',
            endDate: '1995-12-31',
        }]));
    });

    it('picks one role after another from the keyboard, keeping the role field open', async () => {
        renderWithQueries(<Editor initial={[JANE]}/>);
        const role = screen.getByRole('combobox', {name: 'Role for J.D. — Jane Doe'});

        await userEvent.type(role, 'org{ArrowDown}{Enter}');
        await userEvent.type(role, 'mus{ArrowDown}{Enter}');

        expect(screen.getByRole('status')).toHaveTextContent(JSON.stringify([{...JANE, roles: ['organizer', 'musician']}]));
        expect(role).toHaveFocus();
        expect(role).toHaveValue('');
        expect(screen.getByRole('option', {name: 'Coder'})).toBeVisible();
    });

    it('offers only the roles not chosen yet and says when none matches', async () => {
        renderWithQueries(<Editor initial={[{...JANE, roles: ['coder']}]}/>);
        const role = screen.getByRole('combobox', {name: 'Role for J.D. — Jane Doe'});

        await userEvent.click(role);

        expect(screen.getAllByRole('option').map((option) => option.textContent)).toStrictEqual(['Organizer', 'Musician']);

        await userEvent.type(role, 'xyz');

        expect(screen.queryByRole('option')).not.toBeInTheDocument();
        expect(screen.getByText('Nothing found.')).toBeVisible();
    });

    it('closes the roles on Escape', async () => {
        renderWithQueries(<Editor initial={[JANE]}/>);

        await userEvent.click(screen.getByRole('combobox', {name: 'Role for J.D. — Jane Doe'}));
        await userEvent.keyboard('{Escape}');

        expect(screen.queryByRole('listbox')).not.toBeInTheDocument();
    });

    it('takes a role away by its chip and a member away by the remove button of the card', async () => {
        renderWithQueries(<Editor initial={[{...JANE, roles: ['coder', 'organizer']}]}/>);

        await userEvent.click(screen.getByRole('button', {name: 'Remove Coder'}));

        expect(screen.getByRole('status')).toHaveTextContent(JSON.stringify([{...JANE, roles: ['organizer']}]));

        await userEvent.click(screen.getByRole('button', {name: 'Remove J.D. — Jane Doe'}));

        expect(screen.getByRole('status')).toHaveTextContent('[]');
    });
});
