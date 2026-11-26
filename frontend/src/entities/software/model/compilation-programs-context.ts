import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';
import type {Software} from './software';

export const [CompilationProgramsContext, useCompilationPrograms] =
    createRequiredContext<UseQueryResult<readonly Software[]>>('CompilationPrograms');
