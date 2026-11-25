import type {ReactNode} from 'react';
import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {BusinessError} from '@/shared/api';
import {json, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import type {SoftwareDraft} from '../model/software-form';
import {useSoftwareForm} from '../model/software-form-context';
import {SoftwareFormProvider, type SoftwareFormProviderProps} from '../model/SoftwareFormProvider';
import {SoftwareForm} from './SoftwareForm';
import {Button} from '@/shared/ui/button';

type FormProps = Omit<SoftwareFormProviderProps, 'onSubmit' | 'children'> & {readonly actions?: ReactNode; readonly categories?: ReactNode};

const PLATFORMS = [{code: 'agat', name: 'Agat'}, {code: 'bk', name: 'BK'}];
const BK_HARDWARE = [
    {code: 'bk0010', name: 'BK-0010', group: 'computers', groupName: 'Computers'},
    {code: 'bk0011m', name: 'BK-0011M', group: 'computers', groupName: 'Computers'},
    {code: 'ay8910', name: 'AY-3-8910', group: 'sound', groupName: 'Sound'},
    {code: 'gpu', name: 'GPU', group: 'video', groupName: 'Video'},
];

function renderForm({actions, categories, ...props}: FormProps = {}) {
    const onSubmit = vi.fn<(draft: SoftwareDraft) => void>();
    renderWithQueries(
        <SoftwareFormProvider onSubmit={onSubmit} platforms={PLATFORMS} {...props}>
            <SoftwareForm submitLabel="Save" actions={actions} categories={categories}/>
        </SoftwareFormProvider>,
    );
    return {onSubmit};
}

const GAMES = '00000000-0000-0000-0006-000000000001';
const PUBLISHER = '00000000-0000-0000-0003-000000000001';
const DEVELOPER = '00000000-0000-0000-0004-000000000001';

/** Stands in for the field of the categories: their tree is of another slice. */
function GamesOnly() {
    const {values, refusals, changeCategories} = useSoftwareForm();
    return (
        <div>
            <label>
                <input
                    type="checkbox"
                    checked={values.categories.includes(GAMES)}
                    onChange={(event) => {
                        changeCategories(event.target.checked ? [GAMES] : []);
                    }}
                />
                Games
            </label>
            {refusals.categories !== undefined && <span role="alert">{refusals.categories}</span>}
        </div>
    );
}

describe(SoftwareForm, () => {
    beforeEach(() => {
        stubFetchRoutes(new Map([
            ['/api/hardware?platform=bk', json(BK_HARDWARE)],
            ['/api/hardware?platform=agat', json([])],
        ]));
    });

    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('starts with its main section, the required fields starred', () => {
        renderForm();

        const main = screen.getByRole('region', {name: 'Main'});

        expect(within(main).getByRole('textbox', {name: 'Title'})).toBeInTheDocument();
        expect(within(main).getByRole('combobox', {name: 'Platform'})).toBeInTheDocument();
        expect(within(main).getByRole('textbox', {name: 'Description'})).toBeInTheDocument();
        expect(within(main).getByRole('textbox', {name: 'Release date'})).toBeInTheDocument();
        expect(within(main).getAllByText('*')).toHaveLength(2);
    });

    it('puts what the software is and who made it in sections of their own', () => {
        renderForm({categories: <GamesOnly/>});

        expect(within(screen.getByRole('region', {name: 'Classification'})).getByRole('checkbox', {name: 'Games'})).toBeInTheDocument();
        expect(within(screen.getByRole('region', {name: 'Credits'})).getByRole('combobox', {name: 'Person or group'})).toBeInTheDocument();
        expect(screen.queryByRole('region', {name: 'Relations'})).not.toBeInTheDocument();
        expect(screen.queryByLabelText('Publishers')).not.toBeInTheDocument();
        expect(screen.queryByLabelText('Developers')).not.toBeInTheDocument();
    });

    it('ends with its buttons, the submit last', () => {
        renderForm({actions: <Button color="secondary">Cancel</Button>});

        expect(screen.getAllByRole('button').slice(-2).map((button) => button.textContent)).toStrictEqual(['Cancel', 'Save']);
    });

    it('submits the filled form as a draft', async () => {
        const {onSubmit} = renderForm();

        await userEvent.type(screen.getByRole('textbox', {name: 'Title'}), 'Elite');
        await userEvent.type(screen.getByRole('textbox', {name: 'Description'}), 'Space trading');
        await userEvent.type(screen.getByLabelText('Release date'), '20.09.1985');
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Platform'}), 'BK');
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).toHaveBeenCalledExactlyOnceWith({
            title: 'Elite',
            description: 'Space trading',
            releaseDate: '1985-09-20',
            platform: 'bk',
            hardware: [],
            categories: [],
            credits: [],
            preservation: 'unknown',
            instructions: null,
        });
    });

    it('takes the instructions after the description, apart from it', async () => {
        const {onSubmit} = renderForm();
        const main = screen.getByRole('region', {name: 'Main'});
        const instructions = within(main).getByRole('textbox', {name: 'Instructions'});

        expect(within(main).getByRole('textbox', {name: 'Description'}).compareDocumentPosition(instructions) & Node.DOCUMENT_POSITION_FOLLOWING).toBe(Node.DOCUMENT_POSITION_FOLLOWING);

        await userEvent.type(screen.getByRole('textbox', {name: 'Title'}), 'Elite');
        await userEvent.type(screen.getByRole('textbox', {name: 'Instructions'}), 'Press Space to start');
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Platform'}), 'BK');
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).toHaveBeenCalledExactlyOnceWith(expect.objectContaining({description: null, instructions: 'Press Space to start'}));
    });

    it('chooses the preservation status of the software, unknown until it is known', async () => {
        const {onSubmit} = renderForm();
        const status = screen.getByRole('combobox', {name: 'Preservation status'});

        expect(status).toHaveDisplayValue('Unknown');

        await userEvent.type(screen.getByRole('textbox', {name: 'Title'}), 'Elite');
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Platform'}), 'BK');
        await userEvent.selectOptions(status, 'Missing in action');
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).toHaveBeenCalledExactlyOnceWith(expect.objectContaining({preservation: 'missing-in-action'}));
    });

    it('takes a year alone as the release date and tells how to write one', async () => {
        const {onSubmit} = renderForm();

        expect(screen.getByText('A year, such as 1987, or a day, such as 12.05.1987.')).toBeInTheDocument();

        await userEvent.type(screen.getByRole('textbox', {name: 'Title'}), 'Elite');
        await userEvent.type(screen.getByRole('textbox', {name: 'Release date'}), '1985');
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Platform'}), 'BK');
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).toHaveBeenCalledExactlyOnceWith(expect.objectContaining({releaseDate: '1985'}));
    });

    it('credits people, groups and their labels, the developers and publishers among them by their roles', async () => {
        stubFetchRoutes(new Map([
            ['/api/hardware?platform=agat', json([])],
            [
                '/api/entity-lookup?types=author%2Cauthor-label%2Cgroup%2Cgroup-label&query=j.p.',
                json({items: [{id: PUBLISHER, type: 'author-label', title: 'J.P. — Jane Publisher'}]}),
            ],
            [
                '/api/entity-lookup?types=author%2Cauthor-label%2Cgroup%2Cgroup-label&query=nebula',
                json({items: [{id: DEVELOPER, type: 'group', title: 'Nebula Studio'}]}),
            ],
        ]));
        const {onSubmit} = renderForm({
            initial: {title: 'Elite', description: '', releaseDate: '', platform: 'agat', hardware: [], categories: []},
        });
        const credited = screen.getByRole('combobox', {name: 'Person or group'});

        await userEvent.type(credited, 'j.p.');
        await userEvent.click(await screen.findByRole('option', {name: 'J.P. — Jane Publisher'}));
        await userEvent.click(screen.getByRole('combobox', {name: 'Role for J.P. — Jane Publisher'}));
        await userEvent.click(screen.getByRole('option', {name: 'Publisher'}));
        await userEvent.type(credited, 'nebula');
        await userEvent.click(await screen.findByRole('option', {name: 'Nebula Studio'}));
        await userEvent.click(screen.getByRole('combobox', {name: 'Role for Nebula Studio'}));
        await userEvent.click(screen.getByRole('option', {name: 'Developer'}));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).toHaveBeenCalledExactlyOnceWith(expect.objectContaining({
            credits: [
                expect.objectContaining({id: PUBLISHER, type: 'author-label', roles: ['publisher']}),
                expect.objectContaining({id: DEVELOPER, type: 'group', roles: ['developer']}),
            ],
        }));
    });

    it('credits the author of the original, as of a port or a remake', async () => {
        stubFetchRoutes(new Map([
            ['/api/hardware?platform=agat', json([])],
            [
                '/api/entity-lookup?types=author%2Cauthor-label%2Cgroup%2Cgroup-label&query=nebula',
                json({items: [{id: DEVELOPER, type: 'group', title: 'Nebula Studio'}]}),
            ],
        ]));
        const {onSubmit} = renderForm({
            initial: {title: 'Elite', description: '', releaseDate: '', platform: 'agat', hardware: [], categories: []},
        });

        await userEvent.type(screen.getByRole('combobox', {name: 'Person or group'}), 'nebula');
        await userEvent.click(await screen.findByRole('option', {name: 'Nebula Studio'}));
        await userEvent.click(screen.getByRole('combobox', {name: 'Role for Nebula Studio'}));
        await userEvent.click(screen.getByRole('option', {name: 'Original author'}));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).toHaveBeenCalledExactlyOnceWith(expect.objectContaining({
            credits: [expect.objectContaining({id: DEVELOPER, type: 'group', roles: ['original_author']})],
        }));
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

    it('offers the platforms to choose from', () => {
        renderForm();

        expect(within(screen.getByRole('combobox', {name: 'Platform'})).getAllByRole('option').map((option) => option.textContent)).toStrictEqual(['Choose a platform', 'Agat', 'BK']);
    });

    it('does not submit without a platform and says so under the field', async () => {
        const {onSubmit} = renderForm({initial: {title: 'Elite', description: '', releaseDate: '', platform: '', hardware: [], categories: []}});

        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).not.toHaveBeenCalled();
        expect(screen.getByRole('combobox', {name: 'Platform'})).toHaveAccessibleDescription('Choose a platform.');
    });

    it('starts from the given values', () => {
        renderForm({initial: {title: 'Elite', description: '', releaseDate: '20.09.1985', platform: 'bk', hardware: [], categories: []}});

        expect(screen.getByRole('textbox', {name: 'Title'})).toHaveValue('Elite');
        expect(screen.getByLabelText('Release date')).toHaveValue('20.09.1985');
        expect(screen.getByRole('combobox', {name: 'Platform'})).toHaveValue('bk');
    });

    it('does not submit without a title and says so under the field', async () => {
        const {onSubmit} = renderForm();

        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).not.toHaveBeenCalled();
        expect(screen.getByRole('textbox', {name: 'Title'})).toHaveAccessibleDescription('Enter a title.');
    });

    it('does not submit a title longer than 255 characters', async () => {
        const {onSubmit} = renderForm({initial: {title: 'a'.repeat(256), description: '', releaseDate: '', platform: 'bk', hardware: [], categories: []}});

        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).not.toHaveBeenCalled();
        expect(screen.getByRole('textbox', {name: 'Title'})).toHaveAccessibleDescription('Keep the title within 255 characters.');
    });

    it('locks the form while it is being saved', () => {
        renderForm({busy: true});

        expect(screen.getByRole('textbox', {name: 'Title'})).toBeDisabled();
        expect(screen.getByRole('combobox', {name: 'Platform'})).toBeDisabled();
        expect(screen.getByRole('button', {name: 'Save'})).toBeDisabled();
    });

    it('shows a business error of the whole form at its top', () => {
        renderForm({error: new BusinessError(409, 'software.changed', [])});

        expect(screen.getByRole('alert')).toHaveTextContent('This item was changed in the meantime. Reload and try again.');
    });

    it('shows a business error of a field under that field, in the slice\'s words', () => {
        renderForm({
            error: new BusinessError(422, 'software.title-taken', [{field: 'title', code: 'software.title-taken'}]),
            messages: {'software.title-taken': 'This title is already taken.'},
        });

        expect(screen.getByRole('textbox', {name: 'Title'})).toHaveAccessibleDescription('This title is already taken.');
        expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    });

    it('clears a business error of a field once the field is edited', async () => {
        renderForm({error: new BusinessError(422, 'software.title-taken', [{field: 'title', code: 'software.title-taken'}])});

        await userEvent.type(screen.getByRole('textbox', {name: 'Title'}), 'x');

        expect(screen.getByRole('textbox', {name: 'Title'})).not.toHaveAccessibleDescription();
    });

    it('shows a business error of a field the form does not have at its top', () => {
        renderForm({error: new BusinessError(422, 'software.year-locked', [{field: 'year', code: 'software.year-locked'}])});

        expect(screen.getByRole('alert')).toHaveTextContent('The request was refused.');
    });

    it('shows extra actions next to the submit button', () => {
        renderForm({actions: <span>Delete</span>});

        expect(screen.getByText('Delete')).toBeInTheDocument();
    });

    it('offers no hardware until a platform is chosen', () => {
        renderForm();

        expect(screen.queryByRole('group', {name: 'Hardware'})).not.toBeInTheDocument();
    });

    it('offers the hardware of the chosen platform by group and submits what is checked', async () => {
        const {onSubmit} = renderForm({initial: {title: 'Elite', description: '', releaseDate: '', platform: '', hardware: [], categories: []}});

        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Platform'}), 'BK');
        const computers = await screen.findByRole('group', {name: 'Computers'});
        await userEvent.click(within(computers).getByRole('checkbox', {name: 'BK-0011M'}));
        await userEvent.click(screen.getByRole('checkbox', {name: 'AY-3-8910'}));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).toHaveBeenCalledExactlyOnceWith(expect.objectContaining({platform: 'bk', hardware: ['bk0011m', 'ay8910'], categories: []}));
    });

    it('offers the hardware of a group the archive added under the name of the group', async () => {
        renderForm({initial: {title: 'Elite', description: '', releaseDate: '', platform: 'bk', hardware: [], categories: []}});

        const video = await screen.findByRole('group', {name: 'Video'});

        expect(within(video).getByRole('checkbox', {name: 'GPU'})).toBeInTheDocument();
    });

    it('starts from the hardware of the software', async () => {
        renderForm({initial: {title: 'Elite', description: '', releaseDate: '', platform: 'bk', hardware: ['bk0010'], categories: []}});

        await expect(screen.findByRole('checkbox', {name: 'BK-0010'})).resolves.toBeChecked();
        expect(screen.getByRole('button', {name: 'Remove BK-0010'})).toBeInTheDocument();
    });

    it('locks the platform while hardware is checked and says why', async () => {
        renderForm({initial: {title: 'Elite', description: '', releaseDate: '', platform: 'bk', hardware: [], categories: []}});

        await userEvent.click(await screen.findByRole('checkbox', {name: 'BK-0010'}));

        expect(screen.getByRole('combobox', {name: 'Platform'})).toBeDisabled();
        expect(screen.getByRole('combobox', {name: 'Platform'})).toHaveAccessibleDescription('Remove the hardware to change the platform.');

        await userEvent.click(screen.getByRole('button', {name: 'Remove BK-0010'}));

        expect(screen.getByRole('combobox', {name: 'Platform'})).toBeEnabled();
    });

    it('shows a refusal of the hardware under the hardware', async () => {
        renderForm({
            initial: {title: 'Elite', description: '', releaseDate: '', platform: 'bk', hardware: [], categories: []},
            error: new BusinessError(422, 'hardware.unknown', [{field: 'hardware', code: 'hardware.unknown'}]),
            messages: {'hardware.unknown': 'Choose hardware of the platform of the software.'},
        });

        await expect(screen.findByRole('group', {name: 'Hardware'})).resolves.toHaveAccessibleDescription('Choose hardware of the platform of the software.');
    });

    it('locks the hardware while it is being saved', async () => {
        renderForm({initial: {title: 'Elite', description: '', releaseDate: '', platform: 'bk', hardware: [], categories: []}, busy: true});

        await expect(screen.findByRole('checkbox', {name: 'BK-0010'})).resolves.toBeDisabled();
    });

    it('shows the field of the categories under the hardware and submits the categories chosen in it', async () => {
        const {onSubmit} = renderForm({initial: {title: 'Elite', description: '', releaseDate: '', platform: 'agat', hardware: [], categories: []}, categories: <GamesOnly/>});

        await userEvent.click(screen.getByRole('checkbox', {name: 'Games'}));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        expect(onSubmit).toHaveBeenCalledExactlyOnceWith(expect.objectContaining({categories: [GAMES]}));
    });

    it('gives the field of the categories their refusal until they are changed', async () => {
        renderForm({
            initial: {title: 'Elite', description: '', releaseDate: '', platform: 'agat', hardware: [], categories: [GAMES]},
            error: new BusinessError(422, 'category.unknown', [{field: 'categories', code: 'category.unknown'}]),
            messages: {'category.unknown': 'The category is gone.'},
            categories: <GamesOnly/>,
        });

        expect(screen.getByRole('alert')).toHaveTextContent('The category is gone.');

        await userEvent.click(screen.getByRole('checkbox', {name: 'Games'}));

        expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    });
});
