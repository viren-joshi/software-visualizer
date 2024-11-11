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
import { getAuth, createUserWithEmailAndPassword, signInWithEmailAndPassword, updateProfile } from "firebase/auth";

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

const SignInSignUp: React.FC = () => {
  const [tabValue, setTabValue] = useState(0);
  const [firstName, setFirstName] = useState('');
  const [lastName, setLastName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const navigate = useNavigate();
  const auth = getAuth();

  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
    setFirstName('');
    setLastName('');
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
          await signInWithEmailAndPassword(auth, email, password);
          setSuccess('Successfully signed in!');
          navigate('/');
        } catch (error) {
          setError('Failed to sign in. Please check your credentials.');
        }
      } else {
        setError('Please fill in all fields');
      }
    } else {
      // Sign Up logic
      if (firstName && lastName && email && password && confirmPassword) {
        if (password !== confirmPassword) {
          setError('Passwords do not match');
        } else {
          try {
            const userCredential = await createUserWithEmailAndPassword(auth, email, password);
            await updateProfile(userCredential.user, {
              displayName: `${firstName} ${lastName}`
            });
            setSuccess('Successfully signed up!');
            navigate('/');
          } catch (error) {
            setError('Failed to sign up. Please try again.');
          }
        }
      } else {
        setError('Please fill in all fields');
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
