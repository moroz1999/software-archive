import {afterEach, beforeEach, describe, expect, it, vi} from 'vitest';
import {screen, waitFor, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter} from 'react-router';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {AssistantPanel} from './AssistantPanel';

const CONVERSATION = '00000000-0000-0000-00a1-000000000001';
const OTHER = '00000000-0000-0000-00a1-000000000002';
const PLAN = '00000000-0000-0000-00a2-000000000001';
const RUN = '00000000-0000-0000-00a3-000000000001';
const ELITE = '00000000-0000-0000-0001-000000000001';
const ELITE_PLUS = '00000000-0000-0000-0001-000000000002';
const RELEASE = '00000000-0000-0000-0002-000000000001';
const AT = '2026-11-25T10:00:00Z';

const CONVERSATIONS = '/api/assistant/conversations';
const MESSAGES = `${CONVERSATIONS}/${CONVERSATION}/messages`;
const STOP = `${CONVERSATIONS}/${CONVERSATION}/stop`;
const APPROVE = `/api/assistant/plans/${PLAN}/approve`;
const CANCEL = `/api/assistant/plans/${PLAN}/cancel`;

function stateUrl(conversation: string, afterSeq: number): string {
    return `${CONVERSATIONS}/${conversation}?afterSeq=${String(afterSeq)}`;
}

const ELITE_REF = {type: 'software', id: ELITE, title: 'Elite', path: `/software/${ELITE}`};

interface Message {
    readonly seq: number;
    readonly role: 'user' | 'assistant' | 'system';
    readonly kind: 'text' | 'toolCall' | 'plan' | 'planProgress' | 'error' | 'stopped';
    readonly text: string;
    readonly payload: unknown;
}

function message(seq: number, role: Message['role'], kind: Message['kind'], text: string, payload: unknown = null) {
    return {seq, role, kind, text, payload, createdAt: AT};
}

const QUESTION = message(1, 'user', 'text', 'Что известно про Elite?', {page: {path: `/software/${ELITE}`}});
const ANSWER = message(2, 'assistant', 'text', 'Это [[software:' + ELITE + ']], игра 1984 года.');

function state(overrides: Readonly<Record<string, unknown>> = {}) {
    return {id: CONVERSATION, title: 'Что известно про Elite?', messages: [], plans: [], activeRun: null, refs: {[ELITE]: ELITE_REF}, ...overrides};
}

function run(type: 'chat' | 'execute' = 'chat', stopRequested = false) {
    return {id: RUN, type, planId: type === 'execute' ? PLAN : null, status: 'running', stopRequested, queuedAt: AT, steps: 1};
}

function action(position: number, overrides: Readonly<Record<string, unknown>>) {
    return {position, dependsOn: [], state: null, status: 'pending', result: null, error: null, ...overrides};
}

const PLAN_ACTIONS = [
    action(1, {tool: 'update_software', arguments: {id: ELITE, year: 1985, title: 'Elite'}, kind: 'update', entityType: 'software', state: {title: 'Elite', year: 1984}}),
    action(2, {tool: 'create_software', arguments: {title: 'Elite Plus'}, kind: 'create', entityType: 'software'}),
    action(3, {tool: 'create_release', arguments: {softwareId: {$ref: 2}, version: '1.0'}, kind: 'create', entityType: 'release', dependsOn: [2]}),
];

const RUSSIA = '00000000-0000-0000-00c1-000000000001';
const LENINGRAD = '00000000-0000-0000-00c2-000000000001';
const AUTHOR_REFS = {
    [ELITE]: ELITE_REF,
    [RUSSIA]: {type: 'country', id: RUSSIA, title: 'Russia', path: '/management/geography'},
    [LENINGRAD]: {type: 'city', id: LENINGRAD, title: 'Leningrad', path: '/management/geography'},
};

/** An author, software and its release as an import adds them. */
const AUTHOR_PLAN = [
    action(1, {
        tool: 'create_author', kind: 'create', entityType: 'author',
        arguments: {
            realName: 'Лёвин Михаил Викторович',
            labels: [{id: null, name: 'Лёвин Михаил Викторович', display: true}],
            locations: [{countryId: RUSSIA, cityId: LENINGRAD, primary: true}],
        },
    }),
    action(2, {tool: 'create_software', kind: 'create', entityType: 'software', arguments: {title: 'Диверсант', preservation: 'unknown', colour: 'red'}}),
    action(3, {
        tool: 'create_release', kind: 'create', entityType: 'release', dependsOn: [1, 2],
        arguments: {softwareId: {$ref: 2}, type: 'adaptation', credits: [{id: {$ref: 1}, type: 'author', roles: ['adaptation', 'original_author']}]},
    }),
];

function plan(status: string, actions: readonly unknown[] = PLAN_ACTIONS) {
    return {id: PLAN, title: 'Исправить Elite и добавить Elite Plus', status, proposedAt: AT, actions};
}

const PLAN_MESSAGE = message(2, 'assistant', 'plan', 'Исправить Elite и добавить Elite Plus', {planId: PLAN, callId: 'call-1', arguments: '{}'});

function show(routes: Map<string, () => Response>, path = `/software/${ELITE}`) {
    const fetchMock = stubFetchRoutes(routes);
    renderWithQueries(<MemoryRouter initialEntries={[path]}><AssistantPanel/></MemoryRouter>);
    return fetchMock;
}

function calls(fetchMock: ReturnType<typeof stubFetchRoutes>, url: string, method: string): unknown[] {
    return fetchMock.mock.calls
        .filter(([input, init]) => input === url && (init?.method ?? 'GET') === method)
        .map(([, init]) => {
            const body: unknown = typeof init?.body === 'string' ? JSON.parse(init.body) : init?.body;
            return body;
        });
}

function assistant(): HTMLElement {
    return screen.getByRole('dialog', {name: 'AI assistant'});
}

async function open() {
    await userEvent.click(screen.getByRole('button', {name: 'AI assistant'}));
}

/** A change of the plan shown, by its number. */
function change(number: number): HTMLElement {
    return within(assistant()).getByRole('listitem', {name: `Change ${String(number)}`});
}

/** Opens the panel on the conversation, as picked from the list. */
async function openConversation(conversationState: unknown, routes: [string, () => Response][] = []) {
    const fetchMock = show(new Map([
        [CONVERSATIONS, json([{id: CONVERSATION, title: 'Что известно про Elite?', lastMessageAt: AT, running: false}])],
        [stateUrl(CONVERSATION, 0), json(conversationState)],
        ...routes,
    ]));
    await open();
    await userEvent.click(within(assistant()).getByRole('button', {name: 'Conversations'}));
    await userEvent.click(await within(assistant()).findByRole('button', {name: 'Что известно про Elite?'}));
    return fetchMock;
}

async function send(text: string) {
    await userEvent.type(within(assistant()).getByRole('textbox', {name: 'Message'}), text);
    await userEvent.click(within(assistant()).getByRole('button', {name: 'Send'}));
}

describe(AssistantPanel, () => {
    const unsubscribers: (() => void)[] = [];

    beforeEach(() => {
        document.title = 'Elite — Archive';
    });

    afterEach(() => {
        vi.useRealTimers();
        vi.unstubAllGlobals();
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
    });

    it('opens beside the page on a new conversation, says which page goes along, and closes', async () => {
        const fetchMock = show(new Map());

        await open();
        const panel = within(assistant());

        expect(assistant()).not.toHaveAttribute('aria-modal', 'true');
        expect(panel.getByText('Ask about the archive, or tell what to add or fix.')).toBeInTheDocument();
        expect(panel.getByText('Sent with the page: Elite — Archive')).toBeInTheDocument();

        await userEvent.click(panel.getByRole('button', {name: 'Close'}));

        expect(screen.queryByRole('dialog', {name: 'AI assistant'})).not.toBeInTheDocument();
        expect(fetchMock).not.toHaveBeenCalled();
    });

    it('starts a conversation with the first message, sends the page it is written on and shows the answer', async () => {
        const fetchMock = show(new Map([
            [CONVERSATIONS, json({id: CONVERSATION}, 201)],
            [MESSAGES, noContent()],
            [stateUrl(CONVERSATION, 0), json(state({messages: [QUESTION, ANSWER]}))],
        ]));
        await open();
        const panel = within(assistant());

        await send('Что известно про Elite?');

        await expect(panel.findByText(/игра 1984 года/u)).resolves.toBeInTheDocument();

        expect(panel.getByText('Что известно про Elite?')).toBeInTheDocument();
        expect(panel.getByRole('textbox', {name: 'Message'})).toHaveValue('');
        expect(calls(fetchMock, CONVERSATIONS, 'POST')).toHaveLength(1);
        expect(calls(fetchMock, MESSAGES, 'POST')).toStrictEqual([{
            text: 'Что известно про Elite?',
            page: {path: `/software/${ELITE}`, title: 'Elite — Archive', entityType: 'software', entityId: ELITE},
        }]);
    });

    it('follows the title the page gives its tab once it is shown, and sends that title', async () => {
        const fetchMock = show(new Map([
            [CONVERSATIONS, json({id: CONVERSATION}, 201)],
            [MESSAGES, noContent()],
            [stateUrl(CONVERSATION, 0), json(state())],
        ]));
        await open();

        document.title = 'Elite Plus — Archive';

        await expect(within(assistant()).findByText('Sent with the page: Elite Plus — Archive')).resolves.toBeInTheDocument();

        await send('Что это?');

        await waitFor(() => {
            expect(calls(fetchMock, MESSAGES, 'POST')).toStrictEqual([{
                text: 'Что это?',
                page: {path: `/software/${ELITE}`, title: 'Elite Plus — Archive', entityType: 'software', entityId: ELITE},
            }]);
        });
    });

    it('sends the list a page shows with its filters and page, so the assistant can read what the editor sees', async () => {
        const fetchMock = show(new Map([
            [CONVERSATIONS, json({id: CONVERSATION}, 201)],
            [MESSAGES, noContent()],
            [stateUrl(CONVERSATION, 0), json(state())],
        ]), '/software?category=games&page=2');
        await open();

        await send('Найди тут дубли');

        await waitFor(() => {
            expect(calls(fetchMock, MESSAGES, 'POST')).toStrictEqual([{
                text: 'Найди тут дубли',
                page: {path: '/software?category=games&page=2', title: 'Elite — Archive', entityType: null, entityId: null},
            }]);
        });
    });

    it('sends by Enter and keeps Shift+Enter for a new line', async () => {
        const fetchMock = show(new Map([
            [CONVERSATIONS, json({id: CONVERSATION}, 201)],
            [MESSAGES, noContent()],
            [stateUrl(CONVERSATION, 0), json(state())],
        ]));
        await open();

        await userEvent.type(within(assistant()).getByRole('textbox', {name: 'Message'}), 'Первая{Shift>}{Enter}{/Shift}вторая{Enter}');

        await waitFor(() => {
            expect(calls(fetchMock, MESSAGES, 'POST')).toStrictEqual([expect.objectContaining({text: 'Первая\nвторая'})]);
        });
    });

    it('sends no empty message', async () => {
        show(new Map());
        await open();
        const panel = within(assistant());

        await userEvent.type(panel.getByRole('textbox', {name: 'Message'}), '   ');

        expect(panel.getByRole('button', {name: 'Send'})).toBeDisabled();
    });

    it('shows the assistant at work and offers Stop instead of Send', async () => {
        await openConversation(state({messages: [QUESTION], activeRun: run()}), [
            [stateUrl(CONVERSATION, 1), json(state({messages: [], activeRun: run()}))],
        ]);
        const panel = within(assistant());

        await expect(panel.findByText('Working…')).resolves.toBeInTheDocument();

        expect(panel.getByRole('button', {name: 'Stop'})).toBeInTheDocument();
        expect(panel.queryByRole('button', {name: 'Send'})).not.toBeInTheDocument();
    });

    it('reads what is new every 2 s while the assistant works, and stops reading once it is done', async () => {
        vi.useFakeTimers({shouldAdvanceTime: true});
        const fetchMock = await openConversation(state({messages: [QUESTION], activeRun: run()}), [
            [stateUrl(CONVERSATION, 1), json(state({messages: [ANSWER], activeRun: null}))],
        ]);
        const panel = within(assistant());
        await panel.findByText('Working…');

        await vi.advanceTimersByTimeAsync(2000);

        await expect(panel.findByText(/игра 1984 года/u)).resolves.toBeInTheDocument();

        expect(panel.getByRole('button', {name: 'Send'})).toBeInTheDocument();

        await vi.advanceTimersByTimeAsync(6000);

        expect(calls(fetchMock, stateUrl(CONVERSATION, 1), 'GET')).toHaveLength(1);
        expect(calls(fetchMock, stateUrl(CONVERSATION, 2), 'GET')).toHaveLength(0);
    });

    it('asks the assistant to stop, and shows that it stops', async () => {
        const fetchMock = await openConversation(state({messages: [QUESTION], activeRun: run()}), [
            [STOP, noContent()],
            [stateUrl(CONVERSATION, 1), json(state({messages: [], activeRun: run('chat', true)}))],
        ]);
        const panel = within(assistant());

        await userEvent.click(await panel.findByRole('button', {name: 'Stop'}));

        await expect(panel.findByRole('button', {name: 'Stopping…'})).resolves.toBeDisabled();

        expect(calls(fetchMock, STOP, 'POST')).toHaveLength(1);
    });

    it('links the entries the assistant mentions, and shows an entry it cannot find as it was written', async () => {
        const unknown = '00000000-0000-0000-0003-000000000009';
        await openConversation(state({
            messages: [QUESTION, message(2, 'assistant', 'text', `Нашёл [[software:${ELITE}]] и [[author:${unknown}]].`)],
        }));
        const panel = within(assistant());

        await expect(panel.findByRole('link', {name: 'Elite'})).resolves.toHaveAttribute('href', `/software/${ELITE}`);

        expect(panel.getByText(/Нашёл/u)).toHaveTextContent(`Нашёл Elite и ${unknown}.`);
    });

    it('folds the calls of tools into steps, marking those that failed', async () => {
        await openConversation(state({
            messages: [
                QUESTION,
                message(2, 'assistant', 'toolCall', '', {callId: 'c1', tool: 'search_software', arguments: '{"keyword":"Elite"}', result: '{"total":1}'}),
                message(3, 'assistant', 'toolCall', '', {callId: 'c2', tool: 'get_software', arguments: '{"id":"x"}', result: 'Error: No such software.'}),
                ANSWER,
            ],
        }));
        const panel = within(assistant());
        const folded = await panel.findByRole('button', {name: '2 steps · 1 failed'});

        expect(folded).toHaveAttribute('aria-expanded', 'false');

        await userEvent.click(folded);
        const steps = within(panel.getByRole('list', {name: 'Steps'}));

        expect(steps.getAllByRole('listitem').map((step) => step.textContent)).toStrictEqual([
            'Searched software: Elite',
            'Read softwareNo such software.',
        ]);
    });

    it('names a tool it has no words for by its name', async () => {
        await openConversation(state({
            messages: [QUESTION, message(2, 'assistant', 'toolCall', '', {callId: 'c1', tool: 'count_stars', arguments: '{}', result: '7'})],
        }));
        const panel = within(assistant());

        await userEvent.click(await panel.findByRole('button', {name: '1 step'}));

        expect(within(panel.getByRole('list', {name: 'Steps'})).getByRole('listitem')).toHaveTextContent(/^count_stars$/u);
    });

    it('shows the errors and the stops of the assistant in the conversation', async () => {
        await openConversation(state({
            messages: [
                QUESTION,
                message(2, 'system', 'error', 'The model failed: Overloaded'),
                message(3, 'user', 'text', 'Ещё раз'),
                message(4, 'system', 'stopped', ''),
            ],
        }));
        const panel = within(assistant());

        await expect(panel.findByText('The model failed: Overloaded')).resolves.toBeInTheDocument();

        expect(panel.getByText('Stopped.')).toBeInTheDocument();
    });

    it('shows a proposed plan waiting for approval, with each change by the kind of its entry and what it does', async () => {
        await openConversation(state({messages: [QUESTION, PLAN_MESSAGE], plans: [plan('proposed')]}));
        const shown = within(await within(assistant()).findByRole('region', {name: 'Исправить Elite и добавить Elite Plus'}));

        expect(shown.getByText('Waits for your approval')).toBeInTheDocument();
        expect(shown.getAllByRole('listitem')).toHaveLength(3);
        expect(change(1)).toHaveTextContent(/Software.*Edit/u);
        expect(change(2)).toHaveTextContent(/Software.*Add/u);
        expect(change(3)).toHaveTextContent(/Release.*Add/u);
    });

    it('puts the details of each proposed operation in its own named section', async () => {
        await openConversation(state({messages: [QUESTION, PLAN_MESSAGE], plans: [plan('proposed')]}));
        const shown = within(await within(assistant()).findByRole('list', {name: 'Changes'}));

        const edit = within(shown.getByRole('region', {name: /^1\. Software · Edit Elite$/u}));
        const addition = within(shown.getByRole('region', {name: /^2\. Software · Add/u}));
        const release = within(shown.getByRole('region', {name: /^3\. Release · Add/u}));

        expect(edit.getByRole('definition', {name: 'year'})).toHaveTextContent('1984 → 1985');
        expect(addition.getByRole('definition', {name: 'Title'})).toHaveTextContent('Elite Plus');
        expect(release.getByRole('definition', {name: 'Software'})).toHaveTextContent('made by change 2');
    });

    it('shows the entry a change edits, and what it holds beside what it becomes', async () => {
        await openConversation(state({messages: [QUESTION, PLAN_MESSAGE], plans: [plan('proposed')]}));
        await within(assistant()).findByRole('region', {name: 'Исправить Elite и добавить Elite Plus'});
        const edit = within(change(1));

        expect(edit.getByRole('link', {name: 'Elite'})).toHaveAttribute('href', `/software/${ELITE}`);
        expect(edit.getByRole('definition', {name: 'year'})).toHaveTextContent('1984 → 1985');
        expect(edit.getByRole('definition', {name: 'Title'})).toHaveTextContent(/^Elite$/u);
    });

    it('shows which program the assistant will add to the compilation as a link', async () => {
        const compilation = {type: 'software', id: ELITE_PLUS, title: 'Arcade Collection', path: `/software/${ELITE_PLUS}`};
        await openConversation(state({
            messages: [QUESTION, PLAN_MESSAGE],
            plans: [plan('proposed', [
                action(1, {
                    tool: 'add_compilation_items',
                    kind: 'link',
                    entityType: 'software',
                    arguments: {id: compilation.id, compilationItemIds: [ELITE]},
                }),
            ])],
            refs: {[compilation.id]: compilation, [ELITE]: ELITE_REF},
        }));

        const step = within(await within(assistant()).findByRole('listitem', {name: 'Change 1'}));
        const contents = within(step.getByRole('definition', {name: 'Compilation programs'}));

        expect(step.getByRole('link', {name: compilation.title})).toHaveAttribute('href', compilation.path);
        expect(contents.getByRole('link', {name: 'Elite'})).toHaveAttribute('href', `/software/${ELITE}`);
    });

    it('shows an id an earlier change makes by the number of that change', async () => {
        await openConversation(state({messages: [QUESTION, PLAN_MESSAGE], plans: [plan('proposed')]}));
        await within(assistant()).findByRole('region', {name: 'Исправить Elite и добавить Elite Plus'});
        const release = within(change(3));

        expect(within(change(2)).getByRole('definition', {name: 'Title'})).toHaveTextContent('Elite Plus');
        expect(release.getByRole('definition', {name: 'Software'})).toHaveTextContent('made by change 2');
        expect(release.getByRole('definition', {name: 'Version'})).toHaveTextContent('1.0');
    });

    it('names the fields of a change in words, and a field it has no words for by its name', async () => {
        await openConversation(state({messages: [QUESTION, PLAN_MESSAGE], plans: [plan('proposed', AUTHOR_PLAN)], refs: AUTHOR_REFS}));
        await within(assistant()).findByRole('region', {name: 'Исправить Elite и добавить Elite Plus'});
        const author = within(change(1));

        expect(author.getByRole('definition', {name: 'Real name'})).toHaveTextContent('Лёвин Михаил Викторович');
        expect(author.getByRole('definition', {name: 'Labels'})).toBeInTheDocument();
        expect(author.getByRole('definition', {name: 'Locations'})).toBeInTheDocument();
        expect(within(change(2)).getByRole('definition', {name: 'colour'})).toHaveTextContent('red');
    });

    it('shows what a change holds inside as words and links, not as data', async () => {
        await openConversation(state({messages: [QUESTION, PLAN_MESSAGE], plans: [plan('proposed', AUTHOR_PLAN)], refs: AUTHOR_REFS}));
        await within(assistant()).findByRole('region', {name: 'Исправить Elite и добавить Elite Plus'});
        const author = within(change(1));

        expect(author.getByRole('definition', {name: 'Labels'})).toHaveTextContent(/^Лёвин Михаил Викторович · display name$/u);
        expect(author.getByRole('definition', {name: 'Locations'})).toHaveTextContent(/^Russia · Leningrad · primary$/u);
        expect(author.getByRole('link', {name: 'Leningrad'})).toHaveAttribute('href', '/management/geography');
        expect(within(change(3)).getByRole('definition', {name: 'Credits'})).toHaveTextContent(/^made by change 1 · Adaptation, Original author$/u);
    });

    it('names the values of the reference books in words', async () => {
        await openConversation(state({messages: [QUESTION, PLAN_MESSAGE], plans: [plan('proposed', AUTHOR_PLAN)], refs: AUTHOR_REFS}));
        await within(assistant()).findByRole('region', {name: 'Исправить Elite и добавить Elite Plus'});
        const release = within(change(3));

        expect(release.getByRole('definition', {name: 'Type'})).toHaveTextContent(/^Adaptation$/u);
        expect(within(change(2)).getByRole('definition', {name: 'Preservation status'})).toHaveTextContent(/^Unknown$/u);
    });

    it('asks before making the plan, and says how it will be made', async () => {
        const fetchMock = await openConversation(state({messages: [QUESTION, PLAN_MESSAGE], plans: [plan('proposed')]}), [
            [APPROVE, noContent()],
        ]);

        await userEvent.click(await within(assistant()).findByRole('button', {name: 'Make the plan'}));
        const confirm = within(screen.getByRole('alertdialog', {name: 'Make the plan?'}));

        expect(confirm.getByText(/3 changes are made as you, in order/u)).toBeInTheDocument();

        await userEvent.click(confirm.getByRole('button', {name: 'Cancel'}));

        expect(calls(fetchMock, APPROVE, 'POST')).toHaveLength(0);
    });

    it('says before making a plan how many of its changes take data away', async () => {
        const merge = action(1, {tool: 'merge_authors', arguments: {id: ELITE, intoId: ELITE_PLUS}, kind: 'merge', entityType: 'author', state: {name: 'Bell'}});
        await openConversation(state({messages: [QUESTION, PLAN_MESSAGE], plans: [plan('proposed', [merge, PLAN_ACTIONS[1]])]}));

        await userEvent.click(await within(assistant()).findByRole('button', {name: 'Make the plan'}));

        expect(within(screen.getByRole('alertdialog', {name: 'Make the plan?'})).getByText(/One of them deletes or merges data away\./u)).toBeInTheDocument();
    });

    it('makes the plan once the editor confirms, then shows what each change made', async () => {
        const made = [
            {...PLAN_ACTIONS[0], status: 'done', result: ELITE},
            {...PLAN_ACTIONS[1], status: 'done', result: ELITE_PLUS},
            {...PLAN_ACTIONS[2], status: 'done', result: RELEASE},
        ];
        const fetchMock = await openConversation(state({messages: [QUESTION, PLAN_MESSAGE], plans: [plan('proposed')]}), [
            [APPROVE, noContent()],
            [stateUrl(CONVERSATION, 2), json(state({
                plans: [plan('done', made)],
                refs: {[ELITE]: ELITE_REF, [ELITE_PLUS]: {type: 'software', id: ELITE_PLUS, title: 'Elite Plus', path: `/software/${ELITE_PLUS}`}},
            }))],
        ]);

        await userEvent.click(await within(assistant()).findByRole('button', {name: 'Make the plan'}));
        await userEvent.click(within(screen.getByRole('alertdialog', {name: 'Make the plan?'})).getByRole('button', {name: 'Make it'}));
        const shown = within(within(assistant()).getByRole('region', {name: 'Исправить Elite и добавить Elite Plus'}));

        await expect(shown.findByText('Done')).resolves.toBeInTheDocument();

        expect(calls(fetchMock, APPROVE, 'POST')).toHaveLength(1);
        expect(within(change(2)).getByRole('link', {name: 'Elite Plus'})).toHaveAttribute('href', `/software/${ELITE_PLUS}`);
        expect(shown.queryByRole('button', {name: 'Make the plan'})).not.toBeInTheDocument();
    });

    it('cancels the plan without making it', async () => {
        const fetchMock = await openConversation(state({messages: [QUESTION, PLAN_MESSAGE], plans: [plan('proposed')]}), [
            [CANCEL, noContent()],
            [stateUrl(CONVERSATION, 2), json(state({plans: [plan('cancelled')]}))],
        ]);
        const panel = within(assistant());

        await userEvent.click(await panel.findByRole('button', {name: 'Cancel the plan'}));

        await expect(panel.findByText('Cancelled')).resolves.toBeInTheDocument();

        expect(calls(fetchMock, CANCEL, 'POST')).toHaveLength(1);
        expect(calls(fetchMock, APPROVE, 'POST')).toHaveLength(0);
        expect(panel.queryByRole('button', {name: 'Make the plan'})).not.toBeInTheDocument();
    });

    it('offers nothing to do with a plan replaced by a later message', async () => {
        await openConversation(state({messages: [QUESTION, PLAN_MESSAGE], plans: [plan('superseded')]}));
        const panel = within(assistant());

        await expect(panel.findByText('Replaced by your next message')).resolves.toBeInTheDocument();

        expect(panel.queryByRole('button', {name: 'Make the plan'})).not.toBeInTheDocument();
        expect(panel.queryByRole('button', {name: 'Cancel the plan'})).not.toBeInTheDocument();
    });

    it('offers nothing to do with a proposed plan while the assistant works', async () => {
        await openConversation(state({messages: [QUESTION, PLAN_MESSAGE], plans: [plan('proposed')], activeRun: run()}), [
            [stateUrl(CONVERSATION, 2), json(state({plans: [plan('proposed')], activeRun: run()}))],
        ]);
        const panel = within(assistant());

        await expect(panel.findByText('Waits for your approval')).resolves.toBeInTheDocument();

        expect(panel.queryByRole('button', {name: 'Make the plan'})).not.toBeInTheDocument();
    });

    it('shows why a change failed and that those after it were not made', async () => {
        const failed = [
            {...PLAN_ACTIONS[0], status: 'done', result: ELITE},
            {...PLAN_ACTIONS[1], status: 'failed', error: 'This title is taken.'},
            {...PLAN_ACTIONS[2], status: 'skipped'},
        ];
        await openConversation(state({messages: [QUESTION, PLAN_MESSAGE], plans: [plan('failed', failed)]}));
        const shown = within(await within(assistant()).findByRole('region', {name: 'Исправить Elite и добавить Elite Plus'}));

        expect(shown.getByText('Failed at change 2')).toBeInTheDocument();
        expect(change(1)).toHaveTextContent('Made');
        expect(change(2)).toHaveTextContent('This title is taken.');
        expect(change(3)).toHaveTextContent('Not made: change 2 failed.');
    });

    it('opens a conversation with a failed download and a retry plan for empty and populated galleries', async () => {
        const error = 'The download failed: timeout';
        const failed = plan('failed', [action(1, {
            tool: 'set_release_archive', kind: 'files', entityType: 'release',
            arguments: {softwareId: ELITE, releaseId: RELEASE, url: 'https://example.com/elite.zip'},
            status: 'failed', error,
        })]);
        const firstImage = 'https://example.com/elite.png';
        const secondImage = 'https://example.com/elite-plus.png';
        const retry = {
            ...plan('proposed', [
                action(1, {
                    tool: 'add_screenshots', kind: 'files', entityType: 'software',
                    arguments: {softwareId: ELITE, urls: [firstImage]}, state: [],
                }),
                action(2, {
                    tool: 'add_screenshots', kind: 'files', entityType: 'software',
                    arguments: {softwareId: ELITE_PLUS, urls: [secondImage]}, state: [{id: '00000000-0000-0000-00a4-000000000001'}],
                }),
            ]),
            id: '00000000-0000-0000-00a2-000000000002',
            title: 'Добавить скриншоты',
        };
        await openConversation(state({
            messages: [QUESTION, PLAN_MESSAGE, message(3, 'assistant', 'plan', retry.title, {planId: retry.id})],
            plans: [failed, retry],
        }));
        const panel = within(assistant());
        const shown = within(await panel.findByRole('region', {name: retry.title}));

        expect(panel.getByText(error)).toBeInTheDocument();
        expect(shown.getByText(firstImage)).toBeInTheDocument();
        expect(shown.getByText(secondImage)).toBeInTheDocument();
        expect(shown.getByRole('button', {name: 'Make the plan'})).toBeEnabled();
    });

    it('opens a conversation from the list, and starts a new one', async () => {
        await openConversation(state({messages: [QUESTION, ANSWER]}));
        const panel = within(assistant());

        await expect(panel.findByText(/игра 1984 года/u)).resolves.toBeInTheDocument();

        await userEvent.click(panel.getByRole('button', {name: 'New conversation'}));

        expect(panel.getByText('Ask about the archive, or tell what to add or fix.')).toBeInTheDocument();
        expect(panel.queryByText(/игра 1984 года/u)).not.toBeInTheDocument();
    });

    it('names a conversation without a title in the list', async () => {
        show(new Map([[CONVERSATIONS, json([{id: OTHER, title: null, lastMessageAt: AT, running: false}])]]));
        await open();

        await userEvent.click(within(assistant()).getByRole('button', {name: 'Conversations'}));
        const list = within(await within(assistant()).findByRole('list', {name: 'Conversations'}));

        expect(list.getByRole('button', {name: 'Untitled conversation'})).toBeInTheDocument();
    });

    it('deletes a conversation once the editor confirms', async () => {
        const routes = new Map<string, () => Response>([
            [CONVERSATIONS, json([{id: CONVERSATION, title: 'Что известно про Elite?', lastMessageAt: AT, running: false}])],
            [`${CONVERSATIONS}/${CONVERSATION}`, noContent()],
        ]);
        const fetchMock = show(routes);
        await open();
        const panel = within(assistant());
        await userEvent.click(panel.getByRole('button', {name: 'Conversations'}));

        await userEvent.click(await panel.findByRole('button', {name: 'Delete “Что известно про Elite?”'}));
        routes.set(CONVERSATIONS, json([]));
        await userEvent.click(within(screen.getByRole('alertdialog', {name: 'Delete the conversation?'})).getByRole('button', {name: 'Delete'}));

        await expect(panel.findByText('No conversations yet.')).resolves.toBeInTheDocument();

        expect(calls(fetchMock, `${CONVERSATIONS}/${CONVERSATION}`, 'DELETE')).toHaveLength(1);
    });

    it('says that the assistant is still on a task, and keeps the message', async () => {
        await openConversation(state({messages: [QUESTION, ANSWER]}), [
            [MESSAGES, json({status: 409, code: 'assistant.busy'}, 409)],
        ]);
        const panel = within(assistant());
        await panel.findByText(/игра 1984 года/u);

        await send('И ещё');

        await expect(panel.findByRole('alert')).resolves.toHaveTextContent('The assistant is still on the last task: wait for it or stop it.');

        expect(panel.getByRole('textbox', {name: 'Message'})).toHaveValue('И ещё');
    });

    it('says who may use the assistant when the archive refuses', async () => {
        show(new Map([[CONVERSATIONS, json({status: 403, code: 'access.denied'}, 403)]]));
        await open();

        await send('Привет');

        await expect(within(assistant()).findByRole('alert')).resolves.toHaveTextContent('Only the members of the AI assistant group use the assistant.');
    });

    it('hands a system error to the error dialog and keeps the message', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        show(new Map([[CONVERSATIONS, json({status: 500}, 500)]]));
        await open();

        await send('Привет');

        await waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(within(assistant()).getByRole('textbox', {name: 'Message'})).toHaveValue('Привет');
    });

    it('starts a new conversation when the open one is gone', async () => {
        await openConversation(state(), [
            [stateUrl(CONVERSATION, 0), json({status: 404, code: 'assistant.conversation-not-found'}, 404)],
        ]);

        await expect(within(assistant()).findByText('Ask about the archive, or tell what to add or fix.')).resolves.toBeInTheDocument();
    });
});
