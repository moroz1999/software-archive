
/** Where a visitor registers. */
export const REGISTER_PATH = '/register';

/** The page a confirmation link leads to, with its `token`. */
export const VERIFY_EMAIL_PATH = '/verify-email';

/** The administrator-only part of the archive and its users section. */
export const MANAGEMENT_PATH = '/management';
export const MANAGEMENT_USERS_PATH = '/management/users';

/** The route of the page an administrator edits a user on. */
export const MANAGEMENT_USER_EDIT_ROUTE = `${MANAGEMENT_USERS_PATH}/:id/edit`;

/** The page an administrator edits a user on. */
export function managementUserEditPath(id: string): string {
    return `${MANAGEMENT_USERS_PATH}/${id}/edit`;
}
