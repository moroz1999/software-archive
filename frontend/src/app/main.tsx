import {StrictMode} from 'react';
import {createRoot} from 'react-dom/client';
import {App} from './App.tsx';
import {createQueryClient, loadSession} from '@/shared/api';
import {createAppRouter} from './router';
import './styles/index.css';

const root = document.getElementById('root');
if (root === null) {
    throw new Error('Root element #root is missing in index.html');
}

// Who is signed in is asked before the first route preloads: what it reads waits for the answer (CurrentUserProvider
// reports a failure).
loadSession().catch(() => undefined);
const queryClient = createQueryClient();
const router = createAppRouter(queryClient);

createRoot(root).render(
    <StrictMode>
        <App router={router} queryClient={queryClient}/>
    </StrictMode>,
);
