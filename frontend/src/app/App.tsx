import {BrowserRouter as Router, Route, Routes} from 'react-router';
import {Software} from '../pages/Software.tsx';

function App() {

    return (
        <Router>
            <Routes>
                <Route path="/" element={<Software/>}></Route>
            </Routes>
        </Router>
    );
}

export default App;
