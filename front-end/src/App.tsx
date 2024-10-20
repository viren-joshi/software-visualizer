import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import Main from "./components/mainpage/Main";
import NotFound from "./components/NotFound";
import UploadFile from './components/uploadfile/UploadFile';

function App() {
  return (
    <div className="App">
      <Router>
        <Routes>
          <Route path="/" element={ <UploadFile /> } />
          <Route path="/mainpage" element={ <Main /> } />
          <Route path="*" element= {<NotFound />} />
        </Routes>
      </Router>

      <header className="App-header">
        <p>
          Software Visualizer
        </p>
        <UploadFile />
      </header>
    </div>
  );
}

export default App;
