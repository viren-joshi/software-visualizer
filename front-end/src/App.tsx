import { BrowserRouter as Router, Route, Routes, Navigate } from 'react-router-dom';
import { useEffect, useState } from 'react';
import { onAuthStateChanged, User } from "firebase/auth";
import Main from "./components/mainpage/Main";
import NotFound from "./components/NotFound";
import UploadFile from './components/uploadfile/UploadFile';
import SignInSignUp from './components/signIn/SignInSignUp';
import SavedProjects from './components/savedProjects/savedProjects';
import { Box, CircularProgress } from '@mui/material';
import { auth } from './firebase-setup';

function App() {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (currentUser) => {
      setUser(currentUser);
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh' }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <div className="App">
      <Router>
        <Routes>
          <Route 
            path="/" 
            element={user ? <Navigate to="/projects" replace /> : <Navigate to="/signin" replace />} 
          />
          <Route 
            path="/signin" 
            element={user ? <Navigate to="/projects" replace /> : <SignInSignUp />} 
          />
          <Route 
            path="/projects" 
            element={user ? <SavedProjects user={user} /> : <Navigate to="/signin" replace />} 
          />
          <Route 
            path="/upload" 
            element={user ? <UploadFile user={user} /> : <Navigate to="/signin" replace />} 
          />
          <Route 
            path="/mainpage" 
            element={user ? <Main /> : <Navigate to="/signin" replace />} 
          />
          <Route path="*" element={<NotFound />} />
        </Routes>
      </Router>
    </div>
  );
}

export default App;
