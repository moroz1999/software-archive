import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';
import type {ReleaseFile} from './release-file';

export const [ReleaseFilesContext, useReleaseFiles] = createRequiredContext<UseQueryResult<ReleaseFile | null>>('ReleaseFiles');
