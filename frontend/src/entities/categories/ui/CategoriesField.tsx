import {useTranslation} from 'react-i18next';
import {useSkeleton} from '@/shared/lib/loading';
import {Skeleton} from '@/shared/ui/skeleton';
import {TreeChecklist, type TreeChecklistItem} from '@/shared/ui/tree-checklist';
import type {CategoryNode} from '../model/category';
import {useCategoryTree} from '../model/category-tree-context';
import styles from './CategoriesField.module.css';

export interface CategoriesFieldProps {
    /** The ids of the chosen categories. */
    readonly value: readonly string[];
    readonly onChange: (value: readonly string[]) => void;
    readonly disabled?: boolean;
    /** Why the choice was refused, shown under it. */
    readonly error?: string | undefined;
}

function toItem(node: CategoryNode): TreeChecklistItem {
    return {id: node.id, name: node.title, children: node.children.map(toItem)};
}

/** Categories chosen from the whole tree, inside a CategoryTreeProvider. */
export function CategoriesField({value, onChange, disabled = false, error}: CategoriesFieldProps) {
    const {t} = useTranslation();
    const query = useCategoryTree();
    const skeleton = useSkeleton(query);

    if (skeleton || query.isPending) {
        return <div data-testid="categories-field-skeleton"><Skeleton className={styles.bone}/></div>;
    }
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    return (
        <TreeChecklist
            label={t('fields.categories')}
            items={query.data.map(toItem)}
            value={value}
            onChange={onChange}
            disabled={disabled}
            error={error}
        />
    );
}
