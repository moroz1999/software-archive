import {Outlet} from 'react-router';
import {useCurrentUser} from '@/entities/users';
import {NotFoundError} from '@/shared/api';

/** Keeps the management routes out of sight for everyone except administrators. */
export function AdministratorRoute() {
    const {known, user} = useCurrentUser();
    if (!known) {
        return null;
    }
    if (!user?.groups.includes('admin')) {
        throw new NotFoundError('page.not-found');
    }
    return <Outlet/>;
}
