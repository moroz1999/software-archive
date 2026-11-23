import {afterEach, describe, expect, it, vi} from 'vitest';
import {render, screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createQueryClient} from '@/shared/api';
import {json, noContent} from '@/shared/lib/test';
import {App} from './App';
import {type AppRouter, createAppRouter} from './router';

const MEMBER = {id: '00000000-0000-0000-0009-000000000001', name: 'Zed', email: 'zed@example.com', groups: ['member'], abilities: ['add-entries']};
const ADMIN = {...MEMBER, groups: ['admin', 'member'], abilities: ['add-entries', 'keep-categories']};

const GROUPS = [
    {code: 'computers', name: 'Computers', hardware: [{code: 'vector06c', name: 'Vector-06C'}]},
    {code: 'storage', name: 'Storage', hardware: [{code: 'floppy', name: 'Floppy disk'}, {code: 'tape', name: 'Tape'}]},
    {code: 'dos', name: 'DOS', hardware: []},
];
const PLATFORMS = [{code: 'bk', name: 'BK'}, {code: 'vector', name: 'Vector'}];
const STORAGE_DETAILS = {code: 'storage', names: {ru: 'Носители', en: 'Storage'}};
const FLOPPY_DETAILS = {code: 'floppy', names: {ru: 'Дискета', en: 'Floppy disk'}, group: 'storage', platforms: ['vector']};

type Answer = () => Response;
type Routes = Readonly<Record<string, Answer>>;

function urlOf(input: Parameters<typeof fetch>[0]): string {
    return typeof input === 'string' ? input : input instanceof URL ? input.href : input.url;
}

/** Answers every fetch by its method and URL; anything else fails like a network failure, showing up as a system error. */
function stubApi(user: typeof MEMBER, routes: Routes = {}) {
    const answers = new Map<string, Answer>([
        ['GET /api/auth/me', json(user)],
        ['GET /api/platforms', json(PLATFORMS)],
        ['GET /api/hardware-groups', json(GROUPS)],
        ...Object.entries(routes),
    ]);
    const fetchMock = vi.fn<typeof fetch>((input, init) => {
        const url = urlOf(input);
        const answer = answers.get(`${init?.method ?? 'GET'} ${url}`);
        return answer === undefined ? Promise.reject(new TypeError(`Unexpected fetch: ${url}`)) : Promise.resolve(answer());
    });
    vi.stubGlobal('fetch', fetchMock);
    return fetchMock;
}

function bodyOf(fetchMock: ReturnType<typeof stubApi>, method: string, url: string): unknown {
    const call = fetchMock.mock.calls.find(([input, init]) => urlOf(input) === url && init?.method === method);
    const body = call?.[1]?.body;
    return typeof body === 'string' ? JSON.parse(body) : undefined;
}

function sent(fetchMock: ReturnType<typeof stubApi>, method: string): boolean {
    return fetchMock.mock.calls.some(([, init]) => init?.method === method);
}

function refusal(status: number, code: string, field?: string): Answer {
    return json({status, code, detail: code, ...(field === undefined ? {} : {errors: [{field, code}]})}, status);
}

describe('hardware routes', () => {
    let router: AppRouter | undefined;

    afterEach(() => {
        router?.dispose();
        router = undefined;
        window.history.replaceState(null, '', '/');
        vi.unstubAllGlobals();
    });

    function openAt(path: string) {
        window.history.replaceState(null, '', path);
        const queryClient = createQueryClient();
        router = createAppRouter(queryClient);
        render(<App router={router} queryClient={queryClient}/>);
    }

    async function backAtTheList() {
        await expect(screen.findByRole('heading', {level: 1, name: 'Hardware'})).resolves.toBeInTheDocument();
        expect(window.location.pathname).toBe('/management/hardware');
    }

    describe('the list', () => {
        it('shows every group in its order with its hardware, each leading to its editing', async () => {
            stubApi(ADMIN);
            openAt('/management/hardware');

            await expect(screen.findByRole('link', {name: 'Computers'})).resolves.toHaveAttribute('href', '/management/hardware/groups/computers/edit');

            expect(screen.getAllByRole('region').map((region) => region.getAttribute('aria-label'))).toStrictEqual(['Computers', 'Storage', 'DOS']);
            expect(within(screen.getByRole('region', {name: 'Storage'})).getAllByRole('link').map((link) => link.textContent))
                .toStrictEqual(['Storage', 'Floppy disk', 'Tape']);
            expect(screen.getByRole('link', {name: 'Vector-06C'})).toHaveAttribute('href', '/management/hardware/vector06c/edit');
            expect(within(screen.getByRole('region', {name: 'DOS'})).getByText('No hardware in the group yet.')).toBeInTheDocument();
        });

        it('is a section of the management', async () => {
            stubApi(ADMIN);
            openAt('/management/hardware');

            await expect(screen.findByRole('heading', {level: 1, name: 'Hardware'})).resolves.toBeInTheDocument();
            expect(within(screen.getByRole('navigation', {name: 'Management'})).getByRole('link', {name: 'Hardware'})).toHaveAttribute('aria-current', 'page');
        });

        it('leads to adding a group and hardware', async () => {
            stubApi(ADMIN);
            openAt('/management/hardware');

            await expect(screen.findByRole('link', {name: 'Add group'})).resolves.toHaveAttribute('href', '/management/hardware/groups/new');
            expect(screen.getByRole('link', {name: 'Add hardware'})).toHaveAttribute('href', '/management/hardware/new');
        });

        it('hides the hardware from a member', async () => {
            stubApi(MEMBER);
            openAt('/management/hardware');

            await expect(screen.findByRole('heading', {level: 1, name: 'Not found'})).resolves.toBeInTheDocument();
        });

        it('hides adding and editing from a member', async () => {
            stubApi(MEMBER);
            openAt('/management/hardware/new');

            await expect(screen.findByRole('heading', {level: 1, name: 'Not found'})).resolves.toBeInTheDocument();
        });
    });

    describe('groups', () => {
        it('adds a group with its code and names and returns to the list', async () => {
            const fetchMock = stubApi(ADMIN, {'POST /api/hardware-groups': json({code: 'video'}, 201)});
            openAt('/management/hardware/groups/new');

            await expect(screen.findByRole('heading', {level: 1, name: 'Add group'})).resolves.toBeInTheDocument();

            await userEvent.type(screen.getByRole('textbox', {name: 'Code'}), 'video');
            await userEvent.type(screen.getByRole('textbox', {name: 'Name in Russian'}), 'Видео');
            await userEvent.type(screen.getByRole('textbox', {name: 'Name in English'}), 'Video');
            await userEvent.click(screen.getByRole('button', {name: 'Add group'}));

            await backAtTheList();

            expect(bodyOf(fetchMock, 'POST', '/api/hardware-groups')).toStrictEqual({code: 'video', names: {ru: 'Видео', en: 'Video'}});
        });

        it('asks for a code of lower-case letters and digits and for the Russian name before sending', async () => {
            const fetchMock = stubApi(ADMIN);
            openAt('/management/hardware/groups/new');

            await userEvent.type(await screen.findByRole('textbox', {name: 'Code'}), 'Video Cards');
            await userEvent.click(screen.getByRole('button', {name: 'Add group'}));

            expect(screen.getByRole('textbox', {name: 'Code'})).toHaveAccessibleDescription('The code is lower-case Latin letters and digits, words joined by a hyphen.');
            expect(screen.getByRole('textbox', {name: 'Name in Russian'})).toHaveAccessibleDescription('Enter the name in Russian.');
            expect(sent(fetchMock, 'POST')).toBe(false);

            await userEvent.clear(screen.getByRole('textbox', {name: 'Code'}));
            await userEvent.click(screen.getByRole('button', {name: 'Add group'}));

            expect(screen.getByRole('textbox', {name: 'Code'})).toHaveAccessibleDescription('Enter the code.');
        });

        it('says under the code when another group has it', async () => {
            stubApi(ADMIN, {'POST /api/hardware-groups': refusal(409, 'hardware.group-code-taken', 'code')});
            openAt('/management/hardware/groups/new');

            await userEvent.type(await screen.findByRole('textbox', {name: 'Code'}), 'sound');
            await userEvent.type(screen.getByRole('textbox', {name: 'Name in Russian'}), 'Звук');
            await userEvent.click(screen.getByRole('button', {name: 'Add group'}));

            await expect(screen.findByText('The code is taken already.')).resolves.toBeInTheDocument();
            expect(screen.getByRole('textbox', {name: 'Code'})).toHaveAccessibleDescription('The code is taken already.');
        });

        it('renames a group, its code staying as it is', async () => {
            const fetchMock = stubApi(ADMIN, {
                'GET /api/hardware-groups/storage': json(STORAGE_DETAILS),
                'PUT /api/hardware-groups/storage': noContent(),
            });
            openAt('/management/hardware/groups/storage/edit');

            const russian = await screen.findByRole('textbox', {name: 'Name in Russian'});

            expect(screen.getByRole('heading', {level: 1, name: 'Edit group'})).toBeInTheDocument();
            expect(screen.getByRole('textbox', {name: 'Code'})).toHaveValue('storage');
            expect(screen.getByRole('textbox', {name: 'Code'})).toBeDisabled();
            expect(russian).toHaveValue('Носители');

            await userEvent.clear(russian);
            await userEvent.type(russian, 'Накопители');
            await userEvent.click(screen.getByRole('button', {name: 'Save'}));

            await backAtTheList();

            expect(bodyOf(fetchMock, 'PUT', '/api/hardware-groups/storage')).toStrictEqual({names: {ru: 'Накопители', en: 'Storage'}});
        });

        it('hands a failure of saving to the error dialog, keeping what was typed', async () => {
            stubApi(ADMIN, {
                'GET /api/hardware-groups/storage': json(STORAGE_DETAILS),
                'PUT /api/hardware-groups/storage': json({status: 500}, 500),
            });
            openAt('/management/hardware/groups/storage/edit');

            await userEvent.type(await screen.findByRole('textbox', {name: 'Name in English'}), '!');
            await userEvent.click(screen.getByRole('button', {name: 'Save'}));

            await expect(screen.findByRole('alertdialog', {name: 'Something went wrong'})).resolves.toBeInTheDocument();
            expect(screen.getByRole('textbox', {name: 'Name in English'})).toHaveValue('Storage!');
        });

        it('deletes a group once confirmed and returns to the list', async () => {
            const fetchMock = stubApi(ADMIN, {
                'GET /api/hardware-groups/storage': json(STORAGE_DETAILS),
                'DELETE /api/hardware-groups/storage': noContent(),
            });
            openAt('/management/hardware/groups/storage/edit');

            await userEvent.click(await screen.findByRole('button', {name: 'Delete'}));

            expect(screen.getByRole('alertdialog')).toHaveTextContent('Delete “Storage” from the archive?');

            await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

            await backAtTheList();

            expect(sent(fetchMock, 'DELETE')).toBe(true);
        });

        it('says inside the confirmation why a group with hardware stays', async () => {
            stubApi(ADMIN, {
                'GET /api/hardware-groups/storage': json(STORAGE_DETAILS),
                'DELETE /api/hardware-groups/storage': refusal(422, 'hardware.group-in-use'),
            });
            openAt('/management/hardware/groups/storage/edit');

            await userEvent.click(await screen.findByRole('button', {name: 'Delete'}));
            await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

            await expect(within(screen.getByRole('alertdialog')).findByText('The group has hardware in it: it cannot be deleted.')).resolves.toBeInTheDocument();
        });

        it('shows the not found page for a group not in the archive', async () => {
            stubApi(ADMIN, {'GET /api/hardware-groups/video': json({status: 404, code: 'hardware.group-not-found'}, 404)});
            openAt('/management/hardware/groups/video/edit');

            await expect(screen.findByRole('heading', {level: 1, name: 'Not found'})).resolves.toBeInTheDocument();
        });
    });

    describe('hardware', () => {
        it('adds hardware with its code, names, group and platforms and returns to the list', async () => {
            const fetchMock = stubApi(ADMIN, {'POST /api/hardware': json({code: 'gpu'}, 201)});
            openAt('/management/hardware/new');

            await expect(screen.findByRole('heading', {level: 1, name: 'Add hardware'})).resolves.toBeInTheDocument();

            await userEvent.type(screen.getByRole('textbox', {name: 'Code'}), 'gpu');
            await userEvent.type(screen.getByRole('textbox', {name: 'Name in Russian'}), 'Видеоускоритель');
            await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Group'}), await screen.findByRole('option', {name: 'Storage'}));
            const platforms = within(screen.getByRole('group', {name: 'Platforms'}));
            await userEvent.click(await platforms.findByRole('checkbox', {name: 'Vector'}));
            await userEvent.click(platforms.getByRole('checkbox', {name: 'BK'}));
            await userEvent.click(screen.getByRole('button', {name: 'Add hardware'}));

            await backAtTheList();

            expect(bodyOf(fetchMock, 'POST', '/api/hardware')).toStrictEqual({code: 'gpu', names: {ru: 'Видеоускоритель'}, group: 'storage', platforms: ['vector', 'bk']});
        });

        it('offers the groups in their order and the platforms by name', async () => {
            stubApi(ADMIN);
            openAt('/management/hardware/new');

            const group = await screen.findByRole('combobox', {name: 'Group'});

            await expect(within(group).findByRole('option', {name: 'Computers'})).resolves.toBeInTheDocument();
            expect(within(group).getAllByRole('option').map((option) => option.textContent)).toStrictEqual(['Choose a group', 'Computers', 'Storage', 'DOS']);
            expect(within(screen.getByRole('group', {name: 'Platforms'})).getAllByRole<HTMLInputElement>('checkbox').map((box) => box.labels?.[0]?.textContent))
                .toStrictEqual(['BK', 'Vector']);
        });

        it('asks for the code, the Russian name, the group and a platform before sending', async () => {
            const fetchMock = stubApi(ADMIN);
            openAt('/management/hardware/new');

            await screen.findByRole('checkbox', {name: 'Vector'});
            await userEvent.type(screen.getByRole('textbox', {name: 'Name in English'}), 'GPU');
            await userEvent.click(screen.getByRole('button', {name: 'Add hardware'}));

            expect(screen.getByRole('textbox', {name: 'Code'})).toHaveAccessibleDescription('Enter the code.');
            expect(screen.getByRole('textbox', {name: 'Name in Russian'})).toHaveAccessibleDescription('Enter the name in Russian.');
            expect(screen.getByRole('combobox', {name: 'Group'})).toHaveAccessibleDescription('Choose a group.');
            expect(screen.getByRole('group', {name: 'Platforms'})).toHaveAccessibleDescription('Choose at least one platform.');
            expect(sent(fetchMock, 'POST')).toBe(false);
        });

        it('asks for a name of at most 255 characters before sending', async () => {
            const fetchMock = stubApi(ADMIN);
            openAt('/management/hardware/new');

            const russian = await screen.findByRole('textbox', {name: 'Name in Russian'});
            await userEvent.click(russian);
            await userEvent.paste('Я'.repeat(256));
            await userEvent.click(screen.getByRole('button', {name: 'Add hardware'}));

            expect(russian).toHaveAccessibleDescription('The name is at most 255 characters long.');
            expect(sent(fetchMock, 'POST')).toBe(false);
        });

        it('says under the code when other hardware has it', async () => {
            stubApi(ADMIN, {'POST /api/hardware': refusal(409, 'hardware.code-taken', 'code')});
            openAt('/management/hardware/new');

            await fillNewHardware();
            await userEvent.click(screen.getByRole('button', {name: 'Add hardware'}));

            await expect(screen.findByText('The code is taken already.')).resolves.toBeInTheDocument();
            expect(screen.getByRole('textbox', {name: 'Code'})).toHaveAccessibleDescription('The code is taken already.');
        });

        it('says under the group when it is gone', async () => {
            stubApi(ADMIN, {'POST /api/hardware': refusal(422, 'hardware.group-unknown', 'group')});
            openAt('/management/hardware/new');

            await fillNewHardware();
            await userEvent.click(screen.getByRole('button', {name: 'Add hardware'}));

            await expect(screen.findByText('The group is not in the archive any more. Reload and try again.')).resolves.toBeInTheDocument();
            expect(screen.getByRole('combobox', {name: 'Group'})).toHaveAccessibleDescription('The group is not in the archive any more. Reload and try again.');
        });

        it('says under the platforms when one of them is gone', async () => {
            stubApi(ADMIN, {'POST /api/hardware': refusal(422, 'hardware.platform-unknown', 'platforms')});
            openAt('/management/hardware/new');

            await fillNewHardware();
            await userEvent.click(screen.getByRole('button', {name: 'Add hardware'}));

            await expect(screen.findByText('A platform is not in the archive any more. Reload and try again.')).resolves.toBeInTheDocument();
            expect(screen.getByRole('group', {name: 'Platforms'})).toHaveAccessibleDescription('A platform is not in the archive any more. Reload and try again.');
        });

        it('opens the form of hardware with its values, its code kept as it is', async () => {
            stubApi(ADMIN, {'GET /api/hardware/floppy': json(FLOPPY_DETAILS)});
            openAt('/management/hardware/floppy/edit');

            const group = await screen.findByRole('combobox', {name: 'Group'});

            expect(screen.getByRole('heading', {level: 1, name: 'Edit hardware'})).toBeInTheDocument();
            expect(screen.getByRole('textbox', {name: 'Code'})).toHaveValue('floppy');
            expect(screen.getByRole('textbox', {name: 'Code'})).toBeDisabled();
            expect(screen.getByRole('textbox', {name: 'Name in Russian'})).toHaveValue('Дискета');
            await expect(within(group).findByRole('option', {name: 'Storage'})).resolves.toBeInTheDocument();
        });

        it('edits the names, the group and the platforms of hardware', async () => {
            const fetchMock = stubApi(ADMIN, {
                'GET /api/hardware/floppy': json(FLOPPY_DETAILS),
                'PUT /api/hardware/floppy': noContent(),
            });
            openAt('/management/hardware/floppy/edit');

            const group = await screen.findByRole('combobox', {name: 'Group'});
            await within(group).findByRole('option', {name: 'Storage'});

            expect(group).toHaveValue('storage');
            await expect(screen.findByRole('checkbox', {name: 'Vector'})).resolves.toBeChecked();
            expect(screen.getByRole('checkbox', {name: 'BK'})).not.toBeChecked();

            await userEvent.selectOptions(group, screen.getByRole('option', {name: 'DOS'}));
            await userEvent.click(screen.getByRole('checkbox', {name: 'BK'}));
            await userEvent.click(screen.getByRole('button', {name: 'Save'}));

            await backAtTheList();

            expect(bodyOf(fetchMock, 'PUT', '/api/hardware/floppy')).toStrictEqual({names: {ru: 'Дискета', en: 'Floppy disk'}, group: 'dos', platforms: ['vector', 'bk']});
        });

        it('says under the platforms when software of a platform taken away has the hardware', async () => {
            stubApi(ADMIN, {
                'GET /api/hardware/floppy': json(FLOPPY_DETAILS),
                'PUT /api/hardware/floppy': refusal(422, 'hardware.platform-in-use', 'platforms'),
            });
            openAt('/management/hardware/floppy/edit');

            await userEvent.click(await screen.findByRole('checkbox', {name: 'BK'}));
            await userEvent.click(screen.getByRole('checkbox', {name: 'Vector'}));
            await userEvent.click(screen.getByRole('button', {name: 'Save'}));

            await expect(screen.findByText('Software of a platform taken away has this hardware: keep that platform.')).resolves.toBeInTheDocument();
        });

        it('deletes hardware once confirmed and returns to the list', async () => {
            const fetchMock = stubApi(ADMIN, {
                'GET /api/hardware/floppy': json(FLOPPY_DETAILS),
                'DELETE /api/hardware/floppy': noContent(),
            });
            openAt('/management/hardware/floppy/edit');

            await userEvent.click(await screen.findByRole('button', {name: 'Delete'}));

            expect(screen.getByRole('alertdialog')).toHaveTextContent('Delete “Floppy disk” from the archive?');

            await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

            await backAtTheList();

            expect(sent(fetchMock, 'DELETE')).toBe(true);
        });

        it('says inside the confirmation why hardware of software or of a release stays', async () => {
            stubApi(ADMIN, {
                'GET /api/hardware/floppy': json(FLOPPY_DETAILS),
                'DELETE /api/hardware/floppy': refusal(422, 'hardware.in-use'),
            });
            openAt('/management/hardware/floppy/edit');

            await userEvent.click(await screen.findByRole('button', {name: 'Delete'}));
            await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

            await expect(within(screen.getByRole('alertdialog')).findByText('Software or releases have this hardware: it cannot be deleted.')).resolves.toBeInTheDocument();
        });

        it('shows the not found page for hardware not in the archive', async () => {
            stubApi(ADMIN, {'GET /api/hardware/gpu': json({status: 404, code: 'hardware.not-found'}, 404)});
            openAt('/management/hardware/gpu/edit');

            await expect(screen.findByRole('heading', {level: 1, name: 'Not found'})).resolves.toBeInTheDocument();
        });

        it('shows the not found page for a code in the address that cannot be one, without asking', async () => {
            const fetchMock = stubApi(ADMIN);
            openAt('/management/hardware/GPU!/edit');

            await expect(screen.findByRole('heading', {level: 1, name: 'Not found'})).resolves.toBeInTheDocument();
            expect(fetchMock.mock.calls.some(([input]) => urlOf(input).startsWith('/api/hardware/'))).toBe(false);
        });
    });

    async function fillNewHardware() {
        await userEvent.type(await screen.findByRole('textbox', {name: 'Code'}), 'floppy');
        await userEvent.type(screen.getByRole('textbox', {name: 'Name in Russian'}), 'Дискета');
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Group'}), await screen.findByRole('option', {name: 'Storage'}));
        await userEvent.click(await screen.findByRole('checkbox', {name: 'Vector'}));
    }
});
