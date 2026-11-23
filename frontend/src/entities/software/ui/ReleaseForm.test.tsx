import type {ReactNode} from 'react';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {BusinessError} from '@/shared/api';
import {json, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import type {ReleaseDraft} from '../model/release-form';
import {ReleaseFormProvider, type ReleaseFormProviderProps} from '../model/ReleaseFormProvider';
import type {Hardware} from '../model/software';
import {ReleaseForm} from './ReleaseForm';
import {Button} from '@/shared/ui/button';

type FormProps = Omit<ReleaseFormProviderProps, 'onSubmit' | 'children'> & {readonly actions?: ReactNode};

const HARDWARE: readonly Hardware[] = [
    {code: 'vector06c', name: 'Vector-06C', group: 'computers', groupName: 'Computers'},
    {code: 'ay8910', name: 'AY-3-8910', group: 'sound', groupName: 'Sound'},
    {code: 'beeper', name: 'Beeper', group: 'sound', groupName: 'Sound'},
];

function renderForm({actions, ...props}: FormProps = {}) {
    const onSubmit = vi.fn<(draft: ReleaseDraft) => void>();
    renderWithQueries(
        <ReleaseFormProvider onSubmit={onSubmit} hardware={HARDWARE} {...props}>
            <ReleaseForm submitLabel="Save" actions={actions}/>
        </ReleaseFormProvider>,
    );
    return {onSubmit};
}

const JANE = '00000000-0000-0000-0004-000000000001';
const CREW = '00000000-0000-0000-0004-000000000002';

describe(ReleaseForm, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('groups its fields in sections, the required type starred', () => {
        renderForm();

        const main = screen.getByRole('region', {name: 'Main'});

        for (const name of ['Title', 'Version', 'Release date', 'Description']) {
            expect(within(main).getByRole('textbox', {name})).toBeInTheDocument();
        }

        expect(within(main).getByRole('combobox', {name: 'Type'})).toBeInTheDocument();
        expect(within(main).getAllByText('*')).toHaveLength(1);

        const technical = screen.getByRole('region', {name: 'Hardware and archive'});

        expect(within(technical).getByRole('checkbox', {name: 'AY-3-8910'})).toBeInTheDocument();
        expect(within(technical).getByRole('group', {name: 'Archive'})).toBeInTheDocument();
    });

    it('credits people and groups in a section of its own', () => {
        renderForm();

        expect(within(screen.getByRole('region', {name: 'Credits'})).getByRole('combobox', {name: 'Person or group'})).toBeInTheDocument();
    });

    it('ends with its buttons, the submit last', () => {
        renderForm({actions: <Button color="secondary">Cancel</Button>});

        expect(screen.getAllByRole('button').slice(-2).map((button) => button.textContent)).toStrictEqual(['Cancel', 'Save']);
    });

    it('submits the filled form as a draft', async () => {
        const {onSubmit} = renderForm();

        await userEvent.type(screen.getByRole('textbox', {name: 'Title'}), 'Elite AY');
        await userEvent.type(screen.getByRole('textbox', {name: 'Version'}), '1.1');
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Type'}), 'Mod');
        await userEvent.type(screen.getByLabelText('Release date'), '01.03.1990');
        await userEvent.type(screen.getByRole('textbox', {name: 'Description'}), 'With AY music');
        await userEvent.click(screen.getByRole('checkbox', {name: 'AY-3-8910'}));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).toHaveBeenCalledExactlyOnceWith({
            title: 'Elite AY', version: '1.1', type: 'mod', releaseDate: '1990-03-01', description: 'With AY music', hardware: ['ay8910'], credits: [],
        });
    });

    it('credits people, groups and their labels on the release with their roles', async () => {
        stubFetchRoutes(new Map([
            [
                '/api/entity-lookup?types=author%2Cauthor-label%2Cgroup%2Cgroup-label&query=jane',
                json({items: [{id: JANE, type: 'author', title: 'Jane Doe'}]}),
            ],
            [
                '/api/entity-lookup?types=author%2Cauthor-label%2Cgroup%2Cgroup-label&query=crew',
                json({items: [{id: CREW, type: 'group', title: 'Crew'}]}),
            ],
        ]));
        const {onSubmit} = renderForm();
        const credited = screen.getByRole('combobox', {name: 'Person or group'});

        await userEvent.type(credited, 'jane');
        await userEvent.click(await screen.findByRole('option', {name: 'Jane Doe'}));
        await userEvent.click(screen.getByRole('combobox', {name: 'Role for Jane Doe'}));
        await userEvent.click(screen.getByRole('option', {name: 'Intro music'}));
        await userEvent.type(credited, 'crew');
        await userEvent.click(await screen.findByRole('option', {name: 'Crew'}));
        await userEvent.click(screen.getByRole('combobox', {name: 'Role for Crew'}));
        await userEvent.click(screen.getByRole('option', {name: 'Release'}));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).toHaveBeenCalledExactlyOnceWith(expect.objectContaining({
            credits: [
                expect.objectContaining({id: JANE, type: 'author', roles: ['intro_music']}),
                expect.objectContaining({id: CREW, type: 'group', roles: ['release']}),
            ],
        }));
    });

    it('starts from the credits the release has', () => {
        renderForm({
            initial: {
                title: 'Elite AY', version: '', type: 'mod', releaseDate: '', description: '', hardware: [],
                credits: [{id: CREW, type: 'group', title: 'Crew', roles: ['release']}],
            },
        });

        expect(screen.getByRole('combobox', {name: 'Role for Crew'})).toBeInTheDocument();
    });

    it('shows a refusal of the credits under them', () => {
        renderForm({
            error: new BusinessError(422, 'software.credit-unknown', [{field: 'credits', code: 'software.credit-unknown'}]),
            messages: {'software.credit-unknown': 'One of the credited is not in the archive any more.'},
        });

        expect(screen.getByRole('combobox', {name: 'Person or group'})).toHaveAccessibleDescription(
            'One of the credited is not in the archive any more.',
        );
    });

    it('takes a year alone as the release date and tells how to write one', async () => {
        const {onSubmit} = renderForm();

        expect(screen.getByText('A year, such as 1987, or a day, such as 12.05.1987.')).toBeInTheDocument();

        await userEvent.type(screen.getByRole('textbox', {name: 'Release date'}), '2026');
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).toHaveBeenCalledExactlyOnceWith(expect.objectContaining({releaseDate: '2026'}));
    });

    it('says under the release date that it is neither a year nor a day', async () => {
        const {onSubmit} = renderForm();

        await userEvent.type(screen.getByRole('textbox', {name: 'Release date'}), '1987 / 2026');
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).not.toHaveBeenCalled();
        expect(screen.getByText('Enter a year or a full date.')).toBeInTheDocument();
    });

    it('starts a new release of unknown type, offering every type', () => {
        renderForm();

        expect(screen.getByRole('combobox', {name: 'Type'})).toHaveValue('unknown');
        expect(within(screen.getByRole('combobox', {name: 'Type'})).getAllByRole('option').map((option) => option.textContent))
            .toContain('Demo version');
    });

    it('offers the hardware by group and says how a group of the release works', () => {
        renderForm();

        const sound = screen.getByRole('group', {name: 'Sound'});

        expect(within(sound).getAllByRole('checkbox')).toStrictEqual([
            within(sound).getByRole('checkbox', {name: 'AY-3-8910'}),
            within(sound).getByRole('checkbox', {name: 'Beeper'}),
        ]);
        expect(screen.getByRole('group', {name: 'Computers'})).toContainElement(screen.getByRole('checkbox', {name: 'Vector-06C'}));
        expect(screen.getByText('A group checked here replaces that group of the software.')).toBeInTheDocument();
    });

    it('finds hardware by its name', async () => {
        renderForm();

        await userEvent.type(screen.getByRole('searchbox', {name: 'Filter Hardware'}), 'beep');

        expect(screen.getByRole('checkbox', {name: 'Beeper'})).toBeInTheDocument();
        expect(screen.queryByRole('checkbox', {name: 'AY-3-8910'})).not.toBeInTheDocument();
    });

    it('unchecks hardware the release had', async () => {
        const {onSubmit} = renderForm({initial: {title: '', version: '', type: 'mod', releaseDate: '', description: '', hardware: ['ay8910', 'beeper'], credits: []}});

        expect(screen.getByRole('checkbox', {name: 'Beeper'})).toBeChecked();

        await userEvent.click(screen.getByRole('checkbox', {name: 'Beeper'}));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).toHaveBeenCalledWith(expect.objectContaining({hardware: ['ay8910']}));
    });

    it('does not submit a too long version and says so under the field until it is edited', async () => {
        const {onSubmit} = renderForm();

        await userEvent.type(screen.getByRole('textbox', {name: 'Version'}), '1'.repeat(51));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).not.toHaveBeenCalled();
        expect(screen.getByRole('textbox', {name: 'Version'})).toHaveAccessibleDescription('Keep the version within 50 characters.');

        await userEvent.type(screen.getByRole('textbox', {name: 'Version'}), '{backspace}');

        expect(screen.getByRole('textbox', {name: 'Version'})).not.toHaveAccessibleDescription();
    });

    it('shows a refusal of the hardware under the hardware', () => {
        renderForm({
            error: new BusinessError(422, 'hardware.unknown', [{field: 'hardware', code: 'hardware.unknown'}]),
            messages: {'hardware.unknown': 'Choose hardware of the platform of the software.'},
        });

        expect(screen.getByRole('group', {name: 'Hardware'})).toHaveAccessibleDescription('A group checked here replaces that group of the software. Choose hardware of the platform of the software.');
    });

    it('shows a refusal of the whole form at its top', () => {
        renderForm({error: new BusinessError(422, 'release.refused', [])});

        expect(screen.getByRole('alert')).toHaveTextContent('The request was refused.');
    });

    it('locks the fields and the submit button while saving', () => {
        renderForm({busy: true});

        expect(screen.getByRole('textbox', {name: 'Title'})).toBeDisabled();
        expect(screen.getByRole('checkbox', {name: 'Beeper'})).toBeDisabled();
        expect(screen.getByRole('button', {name: 'Save'})).toBeDisabled();
    });
});
