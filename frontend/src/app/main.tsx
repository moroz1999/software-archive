import {StrictMode} from 'react';
import {createRoot} from 'react-dom/client';
import {App} from './App.tsx';
import {createAppRouter} from './router';
import './styles/index.css';

const root = document.getElementById('root');
if (root === null) {
    throw new Error('Root element #root is missing in index.html');
}

const router = createAppRouter();

createRoot(root).render(
    <StrictMode>
        <App router={router}/>
    </StrictMode>,
);
