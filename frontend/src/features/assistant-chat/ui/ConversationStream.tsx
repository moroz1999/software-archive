import {useTranslation} from 'react-i18next';
import {Inline} from '@/shared/ui/inline';
import {Panel} from '@/shared/ui/panel';
import {Spinner} from '@/shared/ui/spinner';
import {Stack} from '@/shared/ui/stack';
import {Text} from '@/shared/ui/typography';
import {useAssistantChat} from '../model/assistant-chat-context';
import type {ChatMessage, Conversation} from '../model/chat';
import {mentionSegments, planOf, streamOf} from '../model/chat-view';
import {PlanCard} from './PlanCard';
import {Segments} from './Segments';
import {ToolSteps} from './ToolSteps';
import styles from './AssistantChat.module.css';

/** The open conversation: its messages, the steps of the assistant, its plans, and whether it works now. */
export function ConversationStream() {
    const {t} = useTranslation();
    const {conversationId, conversation} = useAssistantChat();
    if (conversationId === null) {
        return <Text tone="muted">{t('assistant.empty')}</Text>;
    }
    if (conversation === null) {
        return <Spinner size="sm" label={t('assistant.working')}/>;
    }
    return (
        <Stack gap="md">
            {streamOf(conversation.messages).map((item) => (
                item.kind === 'steps'
                    ? <ToolSteps key={item.seq} steps={item.steps}/>
                    : <Message key={item.message.seq} message={item.message} conversation={conversation}/>
            ))}
            {conversation.activeRun !== null && (
                <Inline gap="sm" align="center">
                    <Spinner size="sm" label={t('assistant.working')}/>
                    <Text variant="bodySm" tone="muted" as="span">{t('assistant.working')}</Text>
                </Inline>
            )}
        </Stack>
    );
}

function Message({message, conversation}: {readonly message: ChatMessage; readonly conversation: Conversation}) {
    const {t} = useTranslation();
    switch (message.kind) {
        case 'plan': {
            const plan = planOf(message, conversation.plans);
            return plan === undefined ? <Text>{message.text}</Text> : <PlanCard plan={plan} refs={conversation.refs}/>;
        }
        case 'error':
            return <Text variant="bodySm" tone="danger">{message.text}</Text>;
        case 'stopped':
            return <Text variant="bodySm" tone="muted">{t('assistant.stopped')}</Text>;
        default:
            return message.role === 'user'
                ? (
                    <Panel variant="flat" surface="primary" radius="lg" padding="sm">
                        <Text className={styles.message}>{message.text}</Text>
                    </Panel>
                )
                : <Text className={styles.message}><Segments segments={mentionSegments(message.text, conversation.refs)}/></Text>;
    }
}
