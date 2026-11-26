import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {compilationProgramsQuery} from '../api/software-queries';
import {CompilationProgramsContext} from './compilation-programs-context';
import type {SoftwareId} from './software';

interface CompilationProgramsProviderProps {
    readonly id: SoftwareId;
    readonly enabled: boolean;
    readonly children: ReactNode;
}

export function CompilationProgramsProvider({id, enabled, children}: CompilationProgramsProviderProps) {
    const query = useQuery({...compilationProgramsQuery(id), enabled});
    return <CompilationProgramsContext value={query}>{children}</CompilationProgramsContext>;
}
