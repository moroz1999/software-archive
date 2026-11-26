import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {ConfirmDialog} from '@/shared/ui/confirm-dialog';
import {CloseIcon} from '@/shared/ui/icon';
import {ListItem} from '@/shared/ui/list-item';
import {Spinner} from '@/shared/ui/spinner';
import {Stack} from '@/shared/ui/stack';
import {Text} from '@/shared/ui/typography';
import {useAssistantChat} from '../model/assistant-chat-context';
import type {ConversationEntry} from '../model/chat';

/** The editor's conversations, the latest first: each opens, or is deleted once the editor confirms. */
export function ConversationList() {
    const {t} = useTranslation();
    const {conversations, openConversation, remove, removing} = useAssistantChat();
    const [deleting, setDeleting] = useState<ConversationEntry | null>(null);
    if (conversations === null) {
        return <Spinner size="sm" label={t('assistant.conversations')}/>;
    }
    const titleOf = (entry: ConversationEntry) => entry.title ?? t('assistant.untitled');
    return (
        <>
            {conversations.length === 0
                ? <Text tone="muted">{t('assistant.noConversations')}</Text>
                : (
                    <Stack as="ul" gap="xs" ariaLabel={t('assistant.conversations')}>
                        {conversations.map((entry) => (
                            <ListItem
                                key={entry.id}
                                onPick={() => {
                                    openConversation(entry.id);
                                }}
                                trailing={(
                                    <Button color="transparent" size="sm" square ariaLabel={t('assistant.deleteConversation', {title: titleOf(entry)})} onClick={() => {
                                        setDeleting(entry);
                                    }}
                                    >
                                        <CloseIcon/>
                                    </Button>
                                )}
                            >
                                {titleOf(entry)}
                            </ListItem>
                        ))}
                    </Stack>
                )}
            <ConfirmDialog
                open={deleting !== null}
                title={t('assistant.deleteTitle')}
                confirmLabel={t('actions.delete')}
                busy={removing}
                onCancel={() => {
                    setDeleting(null);
                }}
                onConfirm={() => {
                    if (deleting !== null) {
                        remove(deleting.id);
                    }
                    setDeleting(null);
                }}
            >
                {deleting === null ? '' : t('assistant.deleteQuestion', {title: titleOf(deleting)})}
            </ConfirmDialog>
        </>
    );
}
