import {BrowserRouter as Router, Route, Routes} from 'react-router';
import {SoftwareList} from '../../features/software-list/ui/SoftwareList.tsx';

function App() {

    return (
        <Router>
            <Routes>
                <Route path="/" element={<SoftwareList/>}></Route>
            </Routes>
        </Router>
    );
}

export default App;
