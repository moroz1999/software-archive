import {useId} from 'react';
import {Heading, ImagePlaceholder, Inset, Panel, Stack, Text, TextLink} from '../../../shared/ui';
import {releaseYear} from '../lib/release-year';
import type {Software} from '../model/software';

export interface SoftwareCardProps {
    readonly software: Software;
    /** Where the title leads. */
    readonly href: string;
}

/** A piece of software as a card: its picture, title and release year. */
export function SoftwareCard({software, href}: SoftwareCardProps) {
    const titleId = useId();
    return (
        <Panel as="article" labelledBy={titleId} radius="xl" padding="none" contentBleed>
            <ImagePlaceholder label="No screenshot yet"/>
            <Inset padding="lg">
                <Stack gap="md">
                    <Heading level={3} id={titleId} truncate><TextLink href={href} tone="inherit">{software.title}</TextLink></Heading>
                    {software.releaseDate !== null && (
                        <Text variant="label" tone="muted">{releaseYear(software.releaseDate)}</Text>
                    )}
                </Stack>
            </Inset>
        </Panel>
    );
}
