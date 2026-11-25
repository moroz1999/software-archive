import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {SparklesIcon} from '@/shared/ui/icon';
import {Drawer} from '@/shared/ui/drawer';
import {useAssistantChat} from '../model/assistant-chat-context';
import {AssistantChatProvider} from '../model/AssistantChatProvider';
import {ChatComposer} from './ChatComposer';
import {ConversationList} from './ConversationList';
import {ConversationStream} from './ConversationStream';

/**
 * The button of the AI assistant and its panel beside the page (docs/features/ai-assistant.md#panel). The chat
 * stays while the panel is closed and the editor goes from page to page.
 */
export function AssistantPanel() {
    const {t} = useTranslation();
    const [open, setOpen] = useState(false);
    return (
        <AssistantChatProvider>
            <Button color="transparent" square ariaLabel={t('assistant.open')} ariaHasPopup="dialog" ariaExpanded={open} onClick={() => {
                setOpen((shown) => !shown);
            }}
            >
                <SparklesIcon/>
            </Button>
            <AssistantDrawer open={open} onClose={() => {
                setOpen(false);
            }}
            />
        </AssistantChatProvider>
    );
}

function AssistantDrawer({open, onClose}: {readonly open: boolean; readonly onClose: () => void}) {
    const {t} = useTranslation();
    const {listShown, toggleList, startNew} = useAssistantChat();
    return (
        <Drawer
            open={open}
            title={t('assistant.title')}
            onClose={onClose}
            header={(
                <>
                    <Button color="transparent" size="sm" ariaExpanded={listShown} onClick={toggleList}>{t('assistant.conversations')}</Button>
                    <Button color="transparent" size="sm" onClick={startNew}>{t('assistant.newConversation')}</Button>
                </>
            )}
            {...(listShown ? {} : {footer: <ChatComposer/>})}
        >
            {listShown ? <ConversationList/> : <ConversationStream/>}
        </Drawer>
    );
}
