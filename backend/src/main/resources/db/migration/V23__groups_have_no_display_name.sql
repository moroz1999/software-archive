-- A group is always shown under its own name: none of its labels is a display name (docs/domain/authors.md).
UPDATE label SET is_display_name = FALSE WHERE group_id IS NOT NULL AND is_display_name;
