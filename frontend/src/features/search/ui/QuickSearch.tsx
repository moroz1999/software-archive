import {useCallback, useEffect, useId, useRef, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {useLocation, useNavigate} from 'react-router';
import {classNames} from '@/shared/lib/class-names';
import {Button} from '@/shared/ui/button';
import {Dialog} from '@/shared/ui/dialog';
import {GameIcon, PersonIcon, SearchIcon} from '@/shared/ui/icon';
import {Inline} from '@/shared/ui/inline';
import {Input} from '@/shared/ui/input';
import {Inset} from '@/shared/ui/inset';
import {LoadingState} from '@/shared/ui/loading-state';
import {Stack} from '@/shared/ui/stack';
import {Text, TextLink} from '@/shared/ui/typography';
import type {FoundKind} from '../model/found';
import {fullSearchHref, keywordOf} from '../model/full-search-filter';
import {QuickSearchProvider} from '../model/QuickSearchProvider';
import {type QuickKind, useQuickSearch} from '../model/quick-search-context';
import styles from './QuickSearch.module.css';

const TITLE_KEYS: Readonly<Record<FoundKind, 'search.authors' | 'search.groups' | 'search.software'>> = {
    author: 'search.authors',
    group: 'search.groups',
    software: 'search.software',
};

const ICONS: Readonly<Record<FoundKind, typeof PersonIcon>> = {author: PersonIcon, group: PersonIcon, software: GameIcon};

/** The full search of the typed text; none while nothing is typed. */
function findAllHref(text: string): string | null {
    const keyword = keywordOf(text);
    return keyword === null ? null : fullSearchHref({keyword, types: [], page: 0});
}

/**
 * The search button of the header and its window with the first matches of every kind. The window starts empty every
 * time it opens, and closes once the visitor goes anywhere.
 */
export function QuickSearch() {
    const {t} = useTranslation();
    const location = useLocation();
    const [openAt, setOpenAt] = useState<string | null>(null);
    if (openAt !== null && openAt !== location.key) {
        setOpenAt(null);
    }
    const close = useCallback(() => {
        setOpenAt(null);
    }, []);

    return (
        <>
            <Button
                color="transparent"
                square
                ariaLabel={t('search.open')}
                onClick={() => {
                    setOpenAt(location.key);
                }}
            >
                <SearchIcon/>
            </Button>
            {openAt !== null && (
                <QuickSearchProvider>
                    <Dialog open title={t('search.title')} onClose={close} focusField dismissible header={<SearchField/>}>
                        <Matches/>
                        <FindAll/>
                    </Dialog>
                </QuickSearchProvider>
            )}
        </>
    );
}

/**
 * The field of the window, a combobox of the matches below it: the arrow keys choose a match round the list, the focus
 * staying in the field; Enter opens the chosen one, or with none chosen, the full search of what is typed.
 */
function SearchField() {
    const {t} = useTranslation();
    const navigate = useNavigate();
    const id = useId();
    const ref = useRef<HTMLDivElement>(null);
    const {text, type, kinds, chosen, move, listId} = useQuickSearch();

    // The input of the design system names itself only; the combobox it is here is told on the element itself.
    useEffect(() => {
        const input = ref.current?.querySelector('input');
        if (input === null || input === undefined) {
            return;
        }
        input.setAttribute('role', 'combobox');
        input.setAttribute('aria-autocomplete', 'list');
        input.setAttribute('aria-controls', listId);
        input.setAttribute('aria-expanded', String(kinds.length > 0));
        if (chosen === null) {
            input.removeAttribute('aria-activedescendant');
        } else {
            input.setAttribute('aria-activedescendant', chosen.id);
        }
    }, [listId, kinds, chosen]);

    useEffect(() => {
        const element = ref.current;
        if (element === null) {
            return undefined;
        }
        const onKeyDown = (event: KeyboardEvent) => {
            if (event.key === 'ArrowDown' || event.key === 'ArrowUp') {
                event.preventDefault();
                move(event.key === 'ArrowDown' ? 1 : -1);
            } else if (event.key === 'Enter') {
                event.preventDefault();
                const href = chosen?.href ?? findAllHref(text);
                if (href !== null) {
                    void navigate(href);
                }
            }
        };
        element.addEventListener('keydown', onKeyDown);
        return () => {
            element.removeEventListener('keydown', onKeyDown);
        };
    }, [move, chosen, text, navigate]);

    return (
        <div ref={ref}>
            <Input id={id} type="search" value={text} onChange={type} placeholder={t('search.placeholder')} ariaLabel={t('search.field')}/>
        </div>
    );
}

function Kind({kind}: {readonly kind: QuickKind}) {
    const {t} = useTranslation();
    const {chosen} = useQuickSearch();
    const captionId = useId();
    const Icon = ICONS[kind.kind];
    return (
        <div role="group" aria-labelledby={captionId}>
            <Inset padding="sm" side="top">
                <Text as="span" variant="caption" tone="muted" className={styles.caption} id={captionId}>{t(TITLE_KEYS[kind.kind], {count: kind.total})}</Text>
            </Inset>
            {kind.matches.map((match) => (
                <div key={match.id} id={match.id} role="option" aria-selected={chosen?.id === match.id} className={classNames(styles.option, chosen?.id === match.id && styles.chosen)}>
                    <Inset padding="sm">
                        <Inline gap="sm" align="center">
                            <Icon className={styles.icon}/>
                            <Text as="span" truncate className={styles.title}><TextLink href={match.href} cover>{match.title}</TextLink></Text>
                        </Inline>
                    </Inset>
                </div>
            ))}
        </div>
    );
}

function Matches() {
    const {t} = useTranslation();
    const {keyword, result, kinds, changing, listId} = useQuickSearch();
    if (keyword === null || result.data === undefined) {
        // Before the first keyword, while its matches load, and when they failed: the error dialog shows why.
        return null;
    }
    if (kinds.length === 0) {
        return <Text tone="muted">{t('empty.nothingFound')}</Text>;
    }
    return (
        <LoadingState loading={changing}>
            <div id={listId} role="listbox" aria-label={t('search.title')}>
                <Stack gap="xs">
                    {kinds.map((kind) => <Kind key={kind.kind} kind={kind}/>)}
                </Stack>
            </div>
        </LoadingState>
    );
}

/** Leads to the full search of what is typed; not offered while nothing is. */
function FindAll() {
    const {t} = useTranslation();
    const {text} = useQuickSearch();
    const href = findAllHref(text);
    return href === null
        ? null
        : <Inline justify="end"><Button href={href} size="sm">{t('search.findAll')}</Button></Inline>;
}
