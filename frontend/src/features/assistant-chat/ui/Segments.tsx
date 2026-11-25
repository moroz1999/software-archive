import {Fragment} from 'react';
import {useTranslation} from 'react-i18next';
import {TextLink} from '@/shared/ui/typography';
import type {Segment} from '../model/chat-view';

/** Text with the entries it mentions as links. */
export function Segments({segments}: {readonly segments: readonly Segment[]}) {
    const {t} = useTranslation();
    return segments.map((segment, index) => (
        // A segment is nothing but its place in the text.
        <Fragment key={index}>
            {segment.kind === 'link' && <TextLink href={segment.path}>{segment.text}</TextLink>}
            {segment.kind === 'madeBy' && t('assistant.plan.madeBy', {n: segment.change})}
            {segment.kind === 'text' && segment.text}
            {segment.kind === 'break' && <br/>}
        </Fragment>
    ));
}
