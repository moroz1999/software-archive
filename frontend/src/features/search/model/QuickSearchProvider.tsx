import {type ReactNode, useCallback, useEffect, useId, useMemo, useState} from 'react';
import {useQuery} from '@tanstack/react-query';
import {authorPath} from '@/entities/authors';
import {groupPath} from '@/entities/groups';
import {usePlatformSelection} from '@/entities/platforms';
import {softwarePath} from '@/entities/software';
import {useLoadingLock} from '@/shared/lib/loading';
import {quickSearchQuery} from '../api/quick-search-queries';
import type {QuickSearchResult} from './quick-search';
import {type QuickKind, QuickSearchContext} from './quick-search-context';

/** How long the keyword waits for the visitor to stop typing before it is looked for. */
const KEYWORD_DELAY_MS = 400;

/** The shortest keyword looked for. */
const KEYWORD_MIN_LENGTH = 2;

function keywordOf(text: string): string | null {
    const keyword = text.trim();
    return keyword.length < KEYWORD_MIN_LENGTH ? null : keyword;
}

/** The kinds with matches, in their order: people, groups, software. */
function kindsOf(result: QuickSearchResult, optionId: (index: number) => string): readonly QuickKind[] {
    let index = 0;
    const next = () => optionId(index++);
    const kinds: QuickKind[] = [
        {kind: 'author', total: result.authors.total, matches: result.authors.items.map((author) => ({id: next(), kind: 'author', title: author.name, href: authorPath(author.id)}))},
        {kind: 'group', total: result.groups.total, matches: result.groups.items.map((group) => ({id: next(), kind: 'group', title: group.name, href: groupPath(group.id)}))},
        {kind: 'software', total: result.software.total, matches: result.software.items.map((software) => ({id: next(), kind: 'software', title: software.title, href: softwarePath(software.id)}))},
    ];
    return kinds.filter((kind) => kind.matches.length > 0);
}

/**
 * Holds the typed keyword, asks for its matches once the visitor stops typing, from two letters on, and the match the
 * arrow keys chose; typing forgets it.
 */
export function QuickSearchProvider({children}: {readonly children: ReactNode}) {
    const id = useId();
    const [text, setText] = useState('');
    const [keyword, setKeyword] = useState<string | null>(null);
    const [chosenIndex, setChosenIndex] = useState<number | null>(null);
    const {platform} = usePlatformSelection();
    const result = useQuery({...quickSearchQuery(keyword ?? '', platform), enabled: keyword !== null});
    const changing = useLoadingLock(result.isPlaceholderData);

    useEffect(() => {
        const timer = setTimeout(() => {
            setKeyword(keywordOf(text));
        }, KEYWORD_DELAY_MS);
        return () => {
            clearTimeout(timer);
        };
    }, [text]);

    const kinds = useMemo(
        () => (keyword === null || result.data === undefined ? [] : kindsOf(result.data, (index) => `${id}-${String(index)}`)),
        [keyword, result.data, id],
    );
    const matches = useMemo(() => kinds.flatMap((kind) => kind.matches), [kinds]);
    const type = useCallback((next: string) => {
        setText(next);
        setChosenIndex(null);
    }, []);
    const move = useCallback((step: 1 | -1) => {
        if (matches.length === 0) {
            return;
        }
        setChosenIndex((index) => {
            if (index === null) {
                return step === 1 ? 0 : matches.length - 1;
            }
            return (index + step + matches.length) % matches.length;
        });
    }, [matches]);

    const chosen = chosenIndex === null ? null : matches[chosenIndex] ?? null;
    const state = useMemo(
        () => ({text, type, keyword, result, kinds, chosen, move, listId: `${id}-matches`, changing}),
        [text, type, keyword, result, kinds, chosen, move, id, changing],
    );

    return <QuickSearchContext value={state}>{children}</QuickSearchContext>;
}
