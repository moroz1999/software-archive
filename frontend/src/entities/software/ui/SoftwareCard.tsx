import {type ReactNode, useId} from 'react';
import {useTranslation} from 'react-i18next';
import {CardScreenshots} from '@/shared/ui/card-screenshots';
import {ImagePlaceholder} from '@/shared/ui/image-placeholder';
import {Inline} from '@/shared/ui/inline';
import {Inset} from '@/shared/ui/inset';
import {Panel} from '@/shared/ui/panel';
import {Stack} from '@/shared/ui/stack';
import {Heading, Text} from '@/shared/ui/typography';
import {releaseYear} from '../lib/release-year';
import type {Software} from '../model/software';

export interface SoftwareCardProps {
    readonly software: Software;
    /** Where the card leads. */
    readonly href: string;
    /** More under who made it and the year, such as the roles of the person whose page lists the card. */
    readonly children?: ReactNode;
    /** Under the card, outside its link: what is done on the card itself, such as voting. */
    readonly footer?: ReactNode;
}

/**
 * A piece of software as a card leading to its page as a whole: its picture, its title, who made it and its release
 * year — on one line when both fit, the year moving under the names when not; under it, outside the link, what is done on the
 * card, such as voting.
 */
export function SoftwareCard({software, href, children, footer}: SoftwareCardProps) {
    const titleId = useId();
    const {t} = useTranslation();
    const madeBy = software.madeBy ?? [];
    return (
        <Panel
            as="article"
            labelledBy={titleId}
            radius="xl"
            padding="none"
            contentBleed
            href={href}
            footer={footer === undefined ? undefined : (
                <Inset padding="lg" side="inline"><Inset padding="lg" side="bottom">{footer}</Inset></Inset>
            )}
        >
            {software.screenshots !== undefined && software.screenshots.length > 0
                ? <CardScreenshots urls={software.screenshots.map((screenshot) => screenshot.url)} alt={software.title}/>
                : <ImagePlaceholder label={t('empty.noScreenshot')}/>}
            <Inset padding="lg">
                <Stack gap="md">
                    <Heading level={3} id={titleId} truncate>{software.title}</Heading>
                    {(madeBy.length > 0 || software.releaseDate !== null) && (
                        <Inline gap="sm" justify={madeBy.length > 0 ? 'between' : 'end'} align="baseline" wrap>
                            {madeBy.length > 0 && <Text variant="label" tone="muted">{madeBy.join(', ')}</Text>}
                            {software.releaseDate !== null && (
                                <Text variant="label" tone="muted">{releaseYear(software.releaseDate)}</Text>
                            )}
                        </Inline>
                    )}
                    {children}
                </Stack>
            </Inset>
        </Panel>
    );
}
