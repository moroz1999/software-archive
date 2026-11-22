import {describe, expect, it} from 'vitest';
import {render, screen} from '@testing-library/react';
import {MemoryRouter} from 'react-router';
import {Heading} from './Heading';
import {Label} from './Label';
import {Text} from './Text';
import {TextLink} from './TextLink';

describe(Text, () => {
    it('is a body paragraph in the inherited tone by default', () => {
        render(<Text>Hello</Text>);

        expect(screen.getByText('Hello').tagName).toBe('P');
        expect(screen.getByText('Hello')).toHaveClass('body', 'toneInherit');
    });

    it('applies variant, tone, truncation and element', () => {
        render(<Text as="span" variant="caption" tone="muted" truncate>Hello</Text>);

        expect(screen.getByText('Hello').tagName).toBe('SPAN');
        expect(screen.getByText('Hello')).toHaveClass('caption', 'toneMuted', 'truncate');
    });
});

describe(Heading, () => {
    it('picks the variant from the level', () => {
        render(<><Heading level={1}>Page</Heading><Heading level={2}>Section</Heading><Heading level={3}>Card</Heading></>);

        expect(screen.getByRole('heading', {level: 1})).toHaveClass('display', 'tonePrimary');
        expect(screen.getByRole('heading', {level: 2})).toHaveClass('headline');
        expect(screen.getByRole('heading', {level: 3})).toHaveClass('title');
    });

    it('applies an explicit variant, tone and truncation', () => {
        render(<Heading level={2} variant="title" tone="muted" truncate>Section</Heading>);

        expect(screen.getByRole('heading', {level: 2})).toHaveClass('title', 'toneMuted', 'truncate');
    });
});

describe(Label, () => {
    it('labels its control', () => {
        render(<><Label htmlFor="title">Title</Label><input id="title"/></>);

        expect(screen.getByLabelText('Title')).toBeInTheDocument();
        expect(screen.getByText('Title')).toHaveClass('label', 'toneInherit');
    });
});

describe(TextLink, () => {
    it('is an in-app link in the link tone by default', () => {
        render(<MemoryRouter><TextLink href="/software/1/edit">Elite</TextLink></MemoryRouter>);

        const link = screen.getByRole('link', {name: 'Elite'});

        expect(link).toHaveAttribute('href', '/software/1/edit');
        expect(link).toHaveClass('toneLink');
    });

    it('is a plain link for an external href, in the requested tone', () => {
        render(<TextLink href="https://example.com" tone="inherit">Site</TextLink>);

        expect(screen.getByRole('link', {name: 'Site'})).toHaveClass('toneInherit');
    });

    it('opens an external href in a new tab that gets no access to this page', () => {
        render(<TextLink href="https://example.com">Site</TextLink>);

        const link = screen.getByRole('link', {name: 'Site'});

        expect(link).toHaveAttribute('target', '_blank');
        expect(link).toHaveAttribute('rel', 'noopener noreferrer');
    });

    it('covers the nearest box around it when asked, staying a link of its own text', () => {
        render(<MemoryRouter><TextLink href="/software/1" cover>Elite</TextLink><TextLink href="/software/2">Exolon</TextLink></MemoryRouter>);

        expect(screen.getByRole('link', {name: 'Elite'})).toHaveClass('cover');
        expect(screen.getByRole('link', {name: 'Exolon'})).not.toHaveClass('cover');
    });

    it('opens an in-app href in the same tab', () => {
        render(<MemoryRouter><TextLink href="/software/1">Elite</TextLink></MemoryRouter>);

        const link = screen.getByRole('link', {name: 'Elite'});

        expect(link).not.toHaveAttribute('target');
        expect(link).not.toHaveAttribute('rel');
    });
});
