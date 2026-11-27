import type {JsonObject} from './chat';
import type {ChatMessage, ChatPlan, EntityRef, PlanAction} from './chat';

/** A piece of shown text: plain, a link to an entry, the id an earlier change of the plan makes, or a new line. */
export type Segment =
    | {readonly kind: 'text'; readonly text: string}
    | {readonly kind: 'link'; readonly text: string; readonly path: string}
    | {readonly kind: 'madeBy'; readonly change: number}
    | {readonly kind: 'break'};

/**
 * The word for a value of a reference book a field holds (a release type, a role), or for a flag set on an item
 * (`display`); null when there is none and the value is shown as it is.
 */
export type ValueWords = (field: string, value: string) => string | null;

/** The flags an item of a change may have set, named by the field `flag`. */
const FLAG = 'flag';

type Refs = Readonly<Record<string, EntityRef>>;

const MENTION = /\[\[(\w+):([^\]\s]+)\]\]/gu;
const EMPTY = '—';

/** `[[type:id]]` in a text as links to the entries the conversation knows; an entry it does not know stays its id. */
export function mentionSegments(text: string, refs: Refs): readonly Segment[] {
    const segments: Segment[] = [];
    let last = 0;
    for (const match of text.matchAll(MENTION)) {
        if (match.index > last) {
            segments.push({kind: 'text', text: text.slice(last, match.index)});
        }
        const id = match[2] ?? '';
        const ref = refs[id];
        segments.push(ref === undefined ? {kind: 'text', text: id} : {kind: 'link', text: ref.title, path: ref.path});
        last = match.index + match[0].length;
    }
    if (last < text.length) {
        segments.push({kind: 'text', text: text.slice(last)});
    }
    return segments;
}

/** A call of a tool, as the editor sees it among the steps. */
export interface ToolStep {
    readonly seq: number;
    readonly tool: string;
    /** What the call was about: the words searched, the address read, the name looked up; null when nothing. */
    readonly subject: string | null;
    /** What the tool answered when it failed; null when it did not. */
    readonly failure: string | null;
}

/** What the conversation shows: its messages, the calls of tools in a row folded into steps, plan progress left to the plans. */
export type StreamItem =
    | {readonly kind: 'message'; readonly message: ChatMessage}
    | {readonly kind: 'steps'; readonly seq: number; readonly steps: readonly ToolStep[]};

const FAILED = 'Error:';

/** The arguments that tell what a call was about, in the order they are looked for. */
const SUBJECTS = ['phrase', 'keyword', 'word', 'name', 'title', 'url'];

function subjectOf(json: unknown): string | null {
    if (typeof json !== 'string') {
        return null;
    }
    let parsed: unknown;
    try {
        parsed = JSON.parse(json);
    } catch {
        return null;
    }
    if (typeof parsed !== 'object' || parsed === null) {
        return null;
    }
    const fields: JsonObject = Object.fromEntries(Object.entries(parsed));
    const values: unknown[] = SUBJECTS.map((key) => fields[key]);
    const found = values.find((value): value is string => typeof value === 'string' && value.trim() !== '');
    return found ?? null;
}

function toolStep(message: ChatMessage): ToolStep {
    const tool = message.payload?.['tool'];
    const result = message.payload?.['result'];
    return {
        seq: message.seq,
        tool: typeof tool === 'string' ? tool : '',
        subject: subjectOf(message.payload?.['arguments']),
        failure: typeof result === 'string' && result.startsWith(FAILED) ? result.slice(FAILED.length).trim() : null,
    };
}

export function streamOf(messages: readonly ChatMessage[]): readonly StreamItem[] {
    const items: StreamItem[] = [];
    for (const message of messages) {
        if (message.kind === 'planProgress') {
            continue;
        }
        const last = items.at(-1);
        if (message.kind !== 'toolCall') {
            items.push({kind: 'message', message});
        } else if (last?.kind === 'steps') {
            items[items.length - 1] = {...last, steps: [...last.steps, toolStep(message)]};
        } else {
            items.push({kind: 'steps', seq: message.seq, steps: [toolStep(message)]});
        }
    }
    return items;
}

/** The plan a message of kind plan proposed. */
export function planOf(message: ChatMessage, plans: readonly ChatPlan[]): ChatPlan | undefined {
    const id = message.payload?.['planId'];
    return plans.find((plan) => plan.id === id);
}

/** The change of the plan that failed; null when none did. */
export function failedAt(plan: ChatPlan): number | null {
    return plan.actions.find((action) => action.status === 'failed')?.position ?? null;
}

/** The kinds of change that take data away: a plan holding one asks to be made with care. */
const DESTRUCTIVE = ['delete', 'merge', 'convert', 'delete-file', 'unlink', 'detach'];

/** How many changes of the plan take data away. */
export function destructiveCount(plan: ChatPlan): number {
    return plan.actions.filter((action) => DESTRUCTIVE.includes(action.kind)).length;
}

function hasStateFields(state: PlanAction['state']): state is JsonObject {
    return state !== null && !Array.isArray(state);
}

/** The entry a change changes: a link once it is known, else the title the change gives it. */
export function actionTarget(action: PlanAction, refs: Refs): readonly Segment[] {
    const stateId = hasStateFields(action.state) ? action.state['id'] : undefined;
    for (const id of [action.arguments['id'], stateId, action.result]) {
        const ref = typeof id === 'string' ? refs[id] : undefined;
        if (ref !== undefined) {
            return [{kind: 'link', text: ref.title, path: ref.path}];
        }
    }
    return [];
}

/** A field of a change: what the entry holds now, when it differs, beside what it becomes. */
export interface ActionRow {
    readonly field: string;
    readonly before: readonly Segment[] | null;
    readonly after: readonly Segment[];
}

/** The fields a change sets, each beside what the entry holds; the id of the entry is its target, not a field. */
export function actionRows(action: PlanAction, refs: Refs, words: ValueWords): readonly ActionRow[] {
    const state = hasStateFields(action.state) ? action.state : null;
    const shown = (field: string, value: unknown) => valueSegments(field, value, refs, words);
    return Object.entries(action.arguments)
        .filter(([field, value]) => field !== 'id' && value !== null && value !== undefined)
        .map(([field, value]) => ({field, before: before(state, field, value, shown), after: shown(field, value)}));
}

function before(state: JsonObject | null, field: string, value: unknown, shown: (field: string, value: unknown) => readonly Segment[]): readonly Segment[] | null {
    if (state === null || !(field in state) || JSON.stringify(state[field]) === JSON.stringify(value)) {
        return null;
    }
    return shown(field, state[field]);
}

function isItem(value: unknown): value is JsonObject {
    return typeof value === 'object' && value !== null && !Array.isArray(value) && !('$ref' in value && typeof value.$ref === 'number');
}

function valueSegments(field: string, value: unknown, refs: Refs, words: ValueWords): readonly Segment[] {
    if (value === null || value === undefined || value === '' || (Array.isArray(value) && value.length === 0)) {
        return [{kind: 'text', text: EMPTY}];
    }
    if (Array.isArray(value)) {
        // Items of a list, such as labels or credits, each on a line of its own; plain values in a row.
        const between: Segment = value.some(isItem) ? {kind: 'break'} : {kind: 'text', text: ', '};
        return value.flatMap((item: unknown, index) => [...(index === 0 ? [] : [between]), ...valueSegments(field, item, refs, words)]);
    }
    if (typeof value === 'string') {
        const ref = refs[value];
        return [ref === undefined ? {kind: 'text', text: words(field, value) ?? value} : {kind: 'link', text: ref.title, path: ref.path}];
    }
    if (isItem(value)) {
        return itemSegments(value, refs, words);
    }
    if (typeof value === 'object') {
        return [{kind: 'madeBy', change: '$ref' in value ? Number(value.$ref) : Number.NaN}];
    }
    return [{kind: 'text', text: typeof value === 'number' || typeof value === 'boolean' ? String(value) : JSON.stringify(value)}];
}

/**
 * An item of a list a change sets — a label, a place, a credit — as its values in a row: a flag set by its word, an
 * unset one and an empty value left out, and the kind of a credited entry left to its link.
 */
function itemSegments(item: JsonObject, refs: Refs, words: ValueWords): readonly Segment[] {
    const parts = Object.entries(item)
        .filter(([key, value]) => key !== 'type' && value !== null && value !== undefined && value !== '' && value !== false
            && !(Array.isArray(value) && value.length === 0))
        .map(([key, value]): readonly Segment[] => value === true
            ? [{kind: 'text', text: words(FLAG, key) ?? key}]
            : valueSegments(key, value, refs, words));
    return parts.flatMap((part, index) => [...(index === 0 ? [] : [{kind: 'text', text: ' · '} as const]), ...part]);
}
