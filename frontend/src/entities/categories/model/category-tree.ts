import type {CategoryId, CategoryNode} from './category';

/** A category to choose as a parent, at its depth in the tree. */
export interface ParentOption {
    readonly id: CategoryId;
    readonly title: string;
    readonly depth: number;
}

/**
 * Every category in the order of the tree, to choose a parent from. The category being edited and its subcategories are
 * left out: a category never goes under itself or its own subcategory (docs/domain/categories.md).
 */
export function parentOptions(tree: readonly CategoryNode[], except?: CategoryId): readonly ParentOption[] {
    function walk(nodes: readonly CategoryNode[], depth: number): ParentOption[] {
        return nodes
            .filter((node) => node.id !== except)
            .flatMap((node) => [{id: node.id, title: node.title, depth}, ...walk(node.children, depth + 1)]);
    }
    return walk(tree, 0);
}
