import type {ReactNode} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {Form, FormActions, FormField, FormMessage} from '@/shared/ui/form';
import {Input} from '@/shared/ui/input';
import {Select} from '@/shared/ui/select';
import {TITLE_PROBLEM_KEYS} from '../config/messages';
import {useCategoryForm} from '../model/category-form-context';
import {parentOptions} from '../model/category-tree';
import {useCategoryTree} from '../model/category-tree-context';

/** Each level of the tree stands in by this in the list of parents: an option holds text only. */
const INDENT = '    ';

export interface CategoryFormProps {
    readonly submitLabel: string;
    /** More buttons before the submit button, such as Cancel. */
    readonly actions?: ReactNode;
}

/**
 * The fields of a category inside a CategoryFormProvider, which holds their values and checks them; the parents to
 * choose from come from the CategoryTreeProvider around.
 */
export function CategoryForm({submitLabel, actions}: CategoryFormProps) {
    const {t} = useTranslation();
    const {values, errors, refusals, change, editing, submit, busy, error} = useCategoryForm();
    const tree = useCategoryTree();
    const options = parentOptions(tree.data ?? [], editing).map((option) => ({
        value: option.id,
        label: INDENT.repeat(option.depth) + option.title,
    }));

    return (
        <Form onSubmit={submit}>
            {error !== undefined && <FormMessage>{error}</FormMessage>}
            <FormField label={t('fields.title')} required error={errors.title === undefined ? refusals.title : t(TITLE_PROBLEM_KEYS[errors.title])}>
                {(control) => <Input {...control} value={values.title} onChange={change('title')} disabled={busy}/>}
            </FormField>
            <FormField label={t('category.parent')} error={refusals.parent}>
                {(control) => (
                    <Select
                        {...control}
                        value={values.parent}
                        onChange={change('parent')}
                        options={options}
                        placeholder={t('category.top')}
                        disabled={busy || tree.data === undefined}
                    />
                )}
            </FormField>
            <FormActions>
                {actions}
                <Button type="submit" loading={busy}>{submitLabel}</Button>
            </FormActions>
        </Form>
    );
}
