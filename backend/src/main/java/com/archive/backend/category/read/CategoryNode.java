package com.archive.backend.category.read;

import java.util.List;
import java.util.UUID;

/**
 * A category of the tree with its subcategories, in their order.
 */
public record CategoryNode(UUID id, String title, List<CategoryNode> children) {}
