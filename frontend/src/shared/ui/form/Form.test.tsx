import {describe, expect, it, vi} from 'vitest';
import {fireEvent, render, screen} from '@testing-library/react';
import {Input} from '../input';
import {Form, FormActions, FormField, FormFieldset, FormMessage, FormRow, FormSection} from './index';

const noop = vi.fn<(value: string) => void>();

describe(Form, () => {
    it('is a form that hands its submit over without reloading the page', () => {
        const onSubmit = vi.fn<() => void>();
        render(<Form onSubmit={onSubmit} ariaLabel="Software"><button type="submit">Save</button></Form>);

        const form = screen.getByRole('form', {name: 'Software'});
        const submitted = fireEvent.submit(form);

        expect(onSubmit).toHaveBeenCalledExactlyOnceWith();
        expect(submitted).toBe(false);
        expect(form).toHaveAttribute('novalidate');
    });

    it('lays its sections out in one column by default, in two when asked', () => {
        const {container, rerender} = render(<Form onSubmit={vi.fn<() => void>()}>Body</Form>);

        expect(container.firstElementChild).toHaveClass('form');
        expect(container.firstElementChild).not.toHaveClass('columns2');

        rerender(<Form onSubmit={vi.fn<() => void>()} columns={2}>Body</Form>);

        expect(container.firstElementChild).toHaveClass('form', 'columns2');
    });
});

describe(FormSection, () => {
    it('is a region named by its title, shown as a level 2 heading', () => {
        render(<FormSection title="Classification">Body</FormSection>);

        expect(screen.getByRole('heading', {level: 2, name: 'Classification'})).toBeInTheDocument();
        expect(screen.getByRole('region', {name: 'Classification'})).toHaveTextContent('Body');
    });

    it('keeps the columns of the form unless it sets its own', () => {
        const {container, rerender} = render(<FormSection title="Main">Body</FormSection>);

        expect(container.querySelector('.fields')).not.toHaveClass('fieldsColumns1', 'fieldsColumns2');

        rerender(<FormSection title="Main" columns={1}>Body</FormSection>);

        expect(container.querySelector('.fields')).toHaveClass('fieldsColumns1');
    });
});

describe(FormField, () => {
    it('labels its control', () => {
        render(<FormField label="Title">{(control) => <Input {...control} value="" onChange={noop}/>}</FormField>);

        expect(screen.getByRole('textbox', {name: 'Title'})).toHaveAttribute('aria-invalid', 'false');
    });

    it('marks a required field by a star that is not part of its name', () => {
        render(<FormField label="Title" required>{(control) => <Input {...control} value="" onChange={noop}/>}</FormField>);

        expect(screen.getByRole('textbox', {name: 'Title'})).toBeInTheDocument();
        expect(screen.getByText('*')).toHaveAttribute('aria-hidden', 'true');
    });

    it('shows its error under the control and ties them together', () => {
        render(<FormField label="Title" error="Enter a title.">{(control) => <Input {...control} value="" onChange={noop}/>}</FormField>);

        const input = screen.getByRole('textbox', {name: 'Title'});

        expect(input).toHaveAttribute('aria-invalid', 'true');
        expect(input).toHaveAccessibleDescription('Enter a title.');
    });

    it('shows a hint under the control, before its error', () => {
        render(<FormField label="Platform" hint="Remove the hardware first." error="Choose a platform.">{(control) => <Input {...control} value="" onChange={noop}/>}</FormField>);

        expect(screen.getByRole('textbox', {name: 'Platform'})).toHaveAccessibleDescription('Remove the hardware first. Choose a platform.');
    });

    it('spans every column of its section when asked', () => {
        const {container} = render(<FormField label="Title" fullWidth>{(control) => <Input {...control} value="" onChange={noop}/>}</FormField>);

        expect(container.firstElementChild).toHaveClass('field', 'fullWidth');
    });
});

describe(FormFieldset, () => {
    it('is a group named by its legend, its error tied to it', () => {
        render(<FormFieldset legend="Labels" error="Add a label.">Body</FormFieldset>);

        expect(screen.getByRole('group', {name: 'Labels'})).toHaveAccessibleDescription('Add a label.');
    });

    it('marks a required group by a star that is not part of its name', () => {
        render(<FormFieldset legend="Types" required>Body</FormFieldset>);

        expect(screen.getByRole('group', {name: 'Types'})).toBeInTheDocument();
        expect(screen.getByText('*')).toHaveAttribute('aria-hidden', 'true');
    });
});

describe(FormRow, () => {
    it('puts its fields side by side in the columns asked', () => {
        const {container} = render(<FormRow columns={3}>Body</FormRow>);

        expect(container.firstElementChild).toHaveClass('row', 'rowColumns3');
    });
});

describe(FormMessage, () => {
    it('tells an error of the whole form as an alert', () => {
        render(<FormMessage>Saving failed.</FormMessage>);

        expect(screen.getByRole('alert')).toHaveTextContent('Saving failed.');
    });

    it('tells a success as a status', () => {
        render(<FormMessage variant="success">Saved.</FormMessage>);

        expect(screen.getByRole('status')).toHaveTextContent('Saved.');
    });
});

describe(FormActions, () => {
    it('puts its buttons at the end by default', () => {
        const {container, rerender} = render(<FormActions>Buttons</FormActions>);

        expect(container.firstElementChild).toHaveClass('actions', 'actionsEnd');

        rerender(<FormActions align="between">Buttons</FormActions>);

        expect(container.firstElementChild).toHaveClass('actions', 'actionsBetween');
    });
});
