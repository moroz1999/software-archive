import {useId} from 'react';
import {useTranslation} from 'react-i18next';
import {useLocation} from 'react-router';
import {Button} from '@/shared/ui/button';
import {Inline} from '@/shared/ui/inline';
import {Stack} from '@/shared/ui/stack';
import {Textarea} from '@/shared/ui/textarea';
import {Text} from '@/shared/ui/typography';
import {useDocumentTitle} from '../lib/document-title';
import {useAssistantChat} from '../model/assistant-chat-context';

/**
 * The message being written, sent with the page it is written on: Enter sends it, Shift+Enter starts a new line.
 * While the assistant works, Stop takes the place of Send.
 */
export function ChatComposer() {
    const {t} = useTranslation();
    const id = useId();
    const {pathname} = useLocation();
    const {conversation, draft, setDraft, send, sending, stop, error} = useAssistantChat();
    const title = useDocumentTitle();
    const run = conversation?.activeRun ?? null;
    const submit = () => {
        if (run === null) {
            send(pathname, title);
        }
    };
    return (
        <Stack gap="sm">
            {error !== undefined && <Text variant="bodySm" tone="danger" role="alert">{error}</Text>}
            <Textarea
                id={id}
                ariaLabel={t('assistant.message')}
                rows={3}
                value={draft}
                onChange={setDraft}
                onKeyDown={(event) => {
                    if (event.key === 'Enter' && !event.shiftKey && !event.nativeEvent.isComposing) {
                        event.preventDefault();
                        submit();
                    }
                }}
            />
            <Inline gap="sm" align="center" justify="between">
                <Text variant="caption" tone="muted" truncate as="span">{t('assistant.page', {title})}</Text>
                {run === null
                    ? <Button size="sm" disabled={draft.trim() === ''} loading={sending} onClick={submit}>{t('assistant.send')}</Button>
                    : (
                        <Button size="sm" color="outlined" disabled={run.stopRequested} onClick={stop}>
                            {run.stopRequested ? t('assistant.stopping') : t('assistant.stop')}
                        </Button>
                    )}
            </Inline>
        </Stack>
    );
}
