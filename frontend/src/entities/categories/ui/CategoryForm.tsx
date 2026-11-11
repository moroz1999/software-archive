import type {ReactNode, SyntheticEvent} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {Field} from '@/shared/ui/field';
import {Inline} from '@/shared/ui/inline';
import {Input} from '@/shared/ui/input';
import {Select} from '@/shared/ui/select';
import {Stack} from '@/shared/ui/stack';
import {Text} from '@/shared/ui/typography';
import {TITLE_PROBLEM_KEYS} from '../config/messages';
import {useCategoryForm} from '../model/category-form-context';
import {parentOptions} from '../model/category-tree';
import {useCategoryTree} from '../model/category-tree-context';
import styles from './CategoryForm.module.css';

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

    function onSubmit(event: SyntheticEvent) {
        event.preventDefault();
        submit();
    }

    return (
        <form noValidate onSubmit={onSubmit} className={styles.form}>
            <Stack gap="lg">
                {error !== undefined && <Text role="alert" tone="danger">{error}</Text>}
                <Field label={t('fields.title')} error={errors.title === undefined ? refusals.title : t(TITLE_PROBLEM_KEYS[errors.title])}>
                    {(control) => <Input {...control} value={values.title} onChange={change('title')} disabled={busy}/>}
                </Field>
                <Field label={t('category.parent')} error={refusals.parent}>
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
                </Field>
                <Inline gap="sm" justify="end" wrap>
                    {actions}
                    <Button type="submit" loading={busy}>{submitLabel}</Button>
                </Inline>
            </Stack>
        </form>
    );
}
