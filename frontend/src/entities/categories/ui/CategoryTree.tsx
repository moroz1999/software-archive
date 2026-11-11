import {useTranslation} from 'react-i18next';
import {useSkeleton} from '@/shared/lib/loading';
import {Tree, type TreeItem} from '@/shared/ui/tree';
import {Text, TextLink} from '@/shared/ui/typography';
import {categoryEditPath} from '../config/paths';
import type {CategoryNode} from '../model/category';
import {useCategoryTree} from '../model/category-tree-context';
import {CategoryTreeSkeleton} from './CategoryTreeSkeleton';

function toItem(node: CategoryNode): TreeItem {
    return {
        id: node.id,
        name: node.title,
        content: <TextLink href={categoryEditPath(node.id)}>{node.title}</TextLink>,
        children: node.children.map(toItem),
    };
}

/** The tree of categories from the CategoryTreeProvider around, folded to its top; each category leads to its editing. */
export function CategoryTree() {
    const {t} = useTranslation();
    const query = useCategoryTree();
    const skeleton = useSkeleton(query);

    if (skeleton || query.isPending) {
        return <div data-testid="category-tree-skeleton"><CategoryTreeSkeleton/></div>;
    }
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    if (query.data.length === 0) {
        return <Text tone="muted">{t('empty.categories')}</Text>;
    }
    return (
        <Tree
            items={query.data.map(toItem)}
            ariaLabel={t('pages.categories')}
            expandLabel={(name) => t('category.expand', {name})}
            collapseLabel={(name) => t('category.collapse', {name})}
        />
    );
}
