import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';
import type {SoftwareRelease} from './release';

/** The releases query of the software, for the blocks inside a SoftwareReleasesProvider. */
export const [SoftwareReleasesContext, useSoftwareReleases] = createRequiredContext<UseQueryResult<readonly SoftwareRelease[]>>('SoftwareReleases');
