import {type ReactNode, useEffect, useMemo, useState} from 'react';
import {useQuery} from '@tanstack/react-query';
import {usePlatformSelection} from '@/entities/platforms';
import {useLoadingLock} from '@/shared/lib/loading';
import {quickSearchQuery} from '../api/quick-search-queries';
import {QuickSearchContext} from './quick-search-context';

/** How long the keyword waits for the visitor to stop typing before it is looked for. */
const KEYWORD_DELAY_MS = 300;

function keywordOf(text: string): string | null {
    const keyword = text.trim();
    return keyword === '' ? null : keyword;
}

/** Holds the typed keyword and asks for its matches once the visitor stops typing; nothing for a blank one. */
export function QuickSearchProvider({children}: {readonly children: ReactNode}) {
    const [text, setText] = useState('');
    const [keyword, setKeyword] = useState<string | null>(null);
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

    const state = useMemo(() => ({text, type: setText, keyword, result, changing}), [text, keyword, result, changing]);

    return <QuickSearchContext value={state}>{children}</QuickSearchContext>;
}
