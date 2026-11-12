import {type ReactNode, useCallback, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {useLocation} from 'react-router';
import {authorPath} from '@/entities/authors';
import {groupPath} from '@/entities/groups';
import {SOFTWARE_LIST_PATH, softwarePath} from '@/entities/software';
import {Button} from '@/shared/ui/button';
import {Dialog} from '@/shared/ui/dialog';
import {Field} from '@/shared/ui/field';
import {Inline} from '@/shared/ui/inline';
import {Input} from '@/shared/ui/input';
import {LoadingState} from '@/shared/ui/loading-state';
import {Stack} from '@/shared/ui/stack';
import {Heading, Text, TextLink} from '@/shared/ui/typography';
import {QuickSearchProvider} from '../model/QuickSearchProvider';
import {useQuickSearch} from '../model/quick-search-context';

/**
 * The search button of the header and its window with the first matches of every section. The window starts empty
 * every time it opens, and closes once the visitor goes anywhere.
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
            <Button color="transparent" onClick={() => {
                setOpenAt(location.key);
            }}>{t('quickSearch.open')}</Button>
            <Dialog open={openAt !== null} title={t('quickSearch.title')} onClose={close} focusField>
                <QuickSearchProvider>
                    <Stack gap="lg">
                        <KeywordField/>
                        <Matches/>
                    </Stack>
                </QuickSearchProvider>
            </Dialog>
        </>
    );
}

function KeywordField() {
    const {t} = useTranslation();
    const {text, type} = useQuickSearch();
    return (
        <Field label={t('quickSearch.field')}>
            {(control) => <Input id={control.id} type="search" value={text} onChange={type}/>}
        </Field>
    );
}

function Section({title, action, children}: {readonly title: string; readonly action?: ReactNode; readonly children: ReactNode}) {
    return (
        <Stack as="section" gap="xs">
            <Inline gap="md" justify="between" align="center">
                <Heading level={3} variant="title">{title}</Heading>
                {action}
            </Inline>
            <Stack as="ul" gap="xs">{children}</Stack>
        </Stack>
    );
}

function Matches() {
    const {t} = useTranslation();
    const {keyword, result, changing} = useQuickSearch();
    if (keyword === null || result.data === undefined) {
        // Before the first keyword, while its matches load, and when they failed: the error dialog shows why.
        return null;
    }
    const {software, authors, groups} = result.data;
    if (software.total + authors.total + groups.total === 0) {
        return <Text tone="muted">{t('empty.nothingFound')}</Text>;
    }
    const catalog = `${SOFTWARE_LIST_PATH}?${new URLSearchParams({q: keyword}).toString()}`;
    return (
        <LoadingState loading={changing}>
            <Stack gap="lg">
                {software.total > 0 && (
                    <Section
                        title={t('quickSearch.software', {count: software.total})}
                        action={<Text as="span" variant="caption"><TextLink href={catalog}>{t('quickSearch.allSoftware', {count: software.total})}</TextLink></Text>}
                    >
                        {software.items.map((item) => <li key={item.id}><TextLink href={softwarePath(item.id)}>{item.title}</TextLink></li>)}
                    </Section>
                )}
                {authors.total > 0 && (
                    <Section title={t('quickSearch.authors', {count: authors.total})}>
                        {authors.items.map((author) => (
                            <li key={author.id}>
                                <Inline gap="sm" align="center" wrap>
                                    <TextLink href={authorPath(author.id)}>{author.name}</TextLink>
                                    {author.realName !== null && <Text as="span" tone="muted">{author.realName}</Text>}
                                </Inline>
                            </li>
                        ))}
                    </Section>
                )}
                {groups.total > 0 && (
                    <Section title={t('quickSearch.groups', {count: groups.total})}>
                        {groups.items.map((group) => <li key={group.id}><TextLink href={groupPath(group.id)}>{group.name}</TextLink></li>)}
                    </Section>
                )}
            </Stack>
        </LoadingState>
    );
}
