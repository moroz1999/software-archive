import {StrictMode} from 'react';
import {createRoot} from 'react-dom/client';
import {App} from './App.tsx';
import {createQueryClient} from '@/shared/api';
import {createAppRouter} from './router';
import './styles/index.css';

const root = document.getElementById('root');
if (root === null) {
    throw new Error('Root element #root is missing in index.html');
}

const queryClient = createQueryClient();
const router = createAppRouter(queryClient);

createRoot(root).render(
    <StrictMode>
        <App router={router} queryClient={queryClient}/>
    </StrictMode>,
);
