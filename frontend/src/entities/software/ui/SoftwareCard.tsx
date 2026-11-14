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
}

/**
 * A piece of software as a card leading to its page as a whole: its picture, its title, who made it and its release
 * year — on one line when both fit, the year moving under the names when not.
 */
export function SoftwareCard({software, href, children}: SoftwareCardProps) {
    const titleId = useId();
    const {t} = useTranslation();
    const madeBy = software.madeBy ?? [];
    return (
        <Panel as="article" labelledBy={titleId} radius="xl" padding="none" contentBleed href={href}>
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
