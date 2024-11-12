import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Button,
  Card,
  CardContent,
  Container,
  Tab,
  Tabs,
  TextField,
  Typography,
  Alert,
  Snackbar,
} from '@mui/material';
import { styled } from '@mui/material/styles';
import { createUserWithEmailAndPassword, getAuth, signInWithEmailAndPassword, updateProfile } from "firebase/auth";
import axios from 'axios';
const FormData = require('form-data');
interface TabPanelProps {
    children?: React.ReactNode;
    index: number;
    value: number;
}

function TabPanel(props: TabPanelProps) {
    const { children, value, index, ...other } = props;

    return (
      <div
        role="tabpanel"
        hidden={value !== index}
        id={`simple-tabpanel-${index}`}
        aria-labelledby={`simple-tab-${index}`}
        {...other}
      >
        {value === index && (
          <Box sx={{ p: 3 }}>
            <Typography component="div">{children}</Typography>
          </Box>
        )}
      </div>
    );
}

const StyledCard = styled(Card)(({ theme }) => ({
  maxWidth: 400,
  margin: 'auto',
  marginTop: theme.spacing(8),
}));

const SignInSignUp = (): React.ReactElement => {
  const [tabValue, setTabValue] = useState(0);
  const [userName, setUserName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const navigate = useNavigate();
  const auth = getAuth();

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
    setUserName('');
    setEmail('');
    setPassword('');
    setConfirmPassword('');
    setError(null);
    setSuccess(null);
  };

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (tabValue === 0) {
      // Sign In logic
      if (email && password) {
        try {
          await signInWithEmailAndPassword(auth, email, password).then((userCredential) => {
            auth.currentUser?.getIdToken().then(async (token) => {
              console.log(token);
              document.cookie = `tokenID=${token}`;
              // Send token to back-end. TODO.
              let data = new FormData();

              let config = {
                method: 'post',
                maxBodyLength: Infinity,
                url: 'http://localhost:8080/auth/verifyToken',
                headers: { 
                  'Authorization': token, 
                  'Content-Type': 'application/json', 
                },
                data : data
              };

              await axios.request(config)
              .then((response) => {
                if (response.status !== 200) {
                  console.log('Failed to sign in');
                } else {
                  localStorage.setItem('soft-viz-tokenID', token);
                  console.log('Successfully signed in');
                  console.log(response.data); 
                  navigate('/');
                }
              })
              .catch((error) => {
                console.log(error);
              });
            });
          });
          
          
        } catch (error) {
          setError('Failed to sign in. Please check your credentials.');
        }
      } else {
        setError('Please fill in all fields');
      }
    } else {
      // Sign Up logic
      if(!userName || !email || !password || !confirmPassword) {}
      if (password !== confirmPassword) {
        setError('Passwords do not match');
      } else {
          try {
            console.log(userName + ' ' + email + ' ' + password);
            
            let userSignUpData = new FormData();
            userSignUpData.append('name', userName);
            userSignUpData.append('email', email);
            userSignUpData.append('password', password);
            let config = {
              method: 'post',
              maxBodyLength: Infinity,
              url: 'http://localhost:8080/auth/signup',
              data : userSignUpData
            };
            console.log(config);
            await axios.request(config).then(async (response) => {
              if (response.status !== 200) {
                throw new Error('Failed to sign up');
              } 
              let responseData = JSON.stringify(response.data);
              await signInWithEmailAndPassword(auth, email, password).then(async (userCredential) => {
                await auth.currentUser?.getIdToken().then((token) => {
                  localStorage.setItem('soft-viz-tokenID', token);
                  console.log('Successfully signed in & up');
                  console.log(responseData); 
                  setSuccess('Successfully signed up!');
                  navigate('/');
                });
              })
            });
          }
          catch (error) {
            console.log(error);
            setError('Failed to sign up. Please try again.');
          }
      }
    }
  };

  return (
    <Container component="main" maxWidth="xs">
      <StyledCard>
        <CardContent>
          <Tabs value={tabValue} onChange={handleTabChange} centered>
            <Tab label="Sign In" />
            <Tab label="Sign Up" />
          </Tabs>
          <form onSubmit={handleSubmit}>
            <TabPanel value={tabValue} index={0}>
              <TextField
                variant="outlined"
                margin="normal"
                required
                fullWidth
                id="email"
                label="Email Address"
                name="email"
                autoComplete="email"
                autoFocus
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
              <TextField
                variant="outlined"
                margin="normal"
                required
                fullWidth
                name="password"
                label="Password"
                type="password"
                id="password"
                autoComplete="current-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
              <Button
                type="submit"
                fullWidth
                variant="contained"
                color="primary"
                sx={{ mt: 3, mb: 2 }}
              >
                Sign In
              </Button>
            </TabPanel>
            <TabPanel value={tabValue} index={1}>
              <TextField
                variant="outlined"
                margin="normal"
                required
                fullWidth
                id="name"
                label="Name"
                name="name"
                autoComplete="given-name"
                autoFocus
                value={userName}
                onChange={(e) => setUserName(e.target.value)} />
              <TextField
                variant="outlined"
                margin="normal"
                required
                fullWidth
                id="email"
                label="Email Address"
                name="email"
                autoComplete="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
              <TextField
                variant="outlined"
                margin="normal"
                required
                fullWidth
                name="password"
                label="Password"
                type="password"
                id="password"
                autoComplete="new-password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
              <TextField
                variant="outlined"
                margin="normal"
                required
                fullWidth
                name="confirmPassword"
                label="Confirm Password"
                type="password"
                id="confirmPassword"
                autoComplete="new-password"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
              />
              <Button
                type="submit"
                fullWidth
                variant="contained"
                color="primary"
                sx={{ mt: 3, mb: 2 }}
              >
                Sign Up
              </Button>
            </TabPanel>
          </form>
        </CardContent>
      </StyledCard>
      <Snackbar open={!!error || !!success} autoHideDuration={6000} onClose={() => { setError(null); setSuccess(null); }}>
        <Alert onClose={() => { setError(null); setSuccess(null); }} severity={error ? "error" : "success"} sx={{ width: '100%' }}>
          {error || success}
        </Alert>
      </Snackbar>
    </Container>
  );
};

export default SignInSignUp;
