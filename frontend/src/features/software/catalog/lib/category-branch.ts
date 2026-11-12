import type {CategoryId, CategoryNode} from '@/entities/categories';

/** The categories from the top of the tree down to the one of the id, itself included; empty when it is not in the tree. */
export function branchTo(tree: readonly CategoryNode[], id: CategoryId): readonly CategoryNode[] {
    for (const node of tree) {
        if (node.id === id) {
            return [node];
        }
        const below = branchTo(node.children, id);
        if (below.length > 0) {
            return [node, ...below];
        }
    }
    return [];
}
