import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {Inset} from '@/shared/ui/inset';
import {Stack} from '@/shared/ui/stack';
import {Text} from '@/shared/ui/typography';
import type {ToolStep} from '../model/chat-view';
import styles from './AssistantChat.module.css';

/** The tools that have words of their own (`assistant.tools.<name>`); another one is shown by its name. */
const PHRASED = [
    'search', 'find_names', 'search_software', 'list_latest_software', 'list_authors', 'list_groups', 'search_releases',
    'get_software', 'get_release', 'get_author', 'get_group', 'get_label', 'list_categories', 'list_platforms', 'list_hardware',
    'list_countries', 'list_cities', 'find_places', 'fetch_url', 'view_image', 'describe_write_tool', 'list_options', 'load_skill',
];

/** The tools the assistant called in a row, folded into one quiet line that opens the list of them. */
export function ToolSteps({steps}: {readonly steps: readonly ToolStep[]}) {
    const {t} = useTranslation();
    const [open, setOpen] = useState(false);
    const failed = steps.filter((step) => step.failure !== null).length;
    return (
        <Stack gap="xs" align="start">
            <Button color="transparent" size="xs" ariaExpanded={open} onClick={() => {
                setOpen((shown) => !shown);
            }}
            >
                <Text variant="caption" tone="muted" as="span">
                    {t('assistant.steps', {count: steps.length})}
                    {failed > 0 && <>{' · '}<Text variant="caption" tone="danger" as="span">{t('assistant.failedSteps', {count: failed})}</Text></>}
                </Text>
            </Button>
            {open && (
                <Inset padding="sm" side="left">
                    <Stack as="ul" gap="sm" ariaLabel={t('assistant.stepsList')}>
                        {steps.map((step) => (
                            <Stack as="li" key={step.seq} gap="none">
                                <Text variant="bodySm" tone={step.failure === null ? 'muted' : 'danger'} className={styles.message}>
                                    {PHRASED.includes(step.tool) ? t(`assistant.tools.${step.tool}`) : step.tool}
                                    {step.subject !== null && <>{': '}<Text variant="bodySm" tone="primary" as="span">{step.subject}</Text></>}
                                </Text>
                                {step.failure !== null && <Text variant="caption" tone="muted" className={styles.message}>{step.failure}</Text>}
                            </Stack>
                        ))}
                    </Stack>
                </Inset>
            )}
        </Stack>
    );
}
