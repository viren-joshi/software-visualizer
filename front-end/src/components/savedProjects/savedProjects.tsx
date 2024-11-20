import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Button,
  Card,
  CardContent,
  CardHeader,
  Container,
  Grid,
  Typography,
  Avatar,
} from '@mui/material';
import LogoutIcon from '@mui/icons-material/Logout';
import UploadFileIcon from '@mui/icons-material/UploadFile';
import { User } from 'firebase/auth';
import { getAuth, signOut } from 'firebase/auth';

interface Project {
  id: string;
  name: string;
  createdAt: string;
}

interface SavedProjectsProps {
  user: User;
}

const SavedProjects: React.FC<SavedProjectsProps> = ({ user }) => {
  const [projects, setProjects] = useState<Project[]>([]);
  const navigate = useNavigate();
  const auth = getAuth();

  useEffect(() => {
    // TODO: Fetch projects from backend
    // This is a placeholder. Replace with actual API call.
    const fetchProjects = async () => {
      // Simulating API call
      const response = await new Promise<Project[]>((resolve) => 
        setTimeout(() => resolve([
          { id: '1', name: 'Project 1', createdAt: '2023-05-01T12:00:00Z' },
          { id: '2', name: 'Project 2', createdAt: '2023-05-02T14:30:00Z' },
          { id: '3', name: 'Project 3', createdAt: '2023-05-03T09:15:00Z' },
          { id: '4', name: 'Project 4', createdAt: '2023-05-04T16:45:00Z' },
          { id: '5', name: 'Project 5', createdAt: '2023-05-05T10:20:00Z' },
        ]), 1000)
      );
      setProjects(response.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()));
    };

    fetchProjects();
  }, []);

  const handleSignOut = async () => {
    try {
      await signOut(auth);
      localStorage.removeItem('soft-viz-tokenID');
      navigate('/signin');
    } catch (error) {
      console.error('Failed to sign out', error);
    }
  };

  const handleUploadFile = () => {
    navigate('/upload');
  };

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'linear-gradient(to right, #e0f2f1, #e8eaf6)',
      }}
    >
      <Container maxWidth="md">
        <Card elevation={3}>
          <CardHeader
            title={
                <Typography variant="h4" align="left" gutterBottom>
                  Software Visualizer
                </Typography>
              }
            action={
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Avatar src={user.photoURL || undefined}>{user.displayName?.charAt(0) || user.email?.charAt(0).toUpperCase()}</Avatar>
                <Typography variant="subtitle1">{user.displayName || user.email}</Typography>
                <Button
                  variant="outlined"
                  startIcon={<LogoutIcon />}
                  onClick={handleSignOut}
                >
                  Sign Out
                </Button>
              </Box>
            }
          />
          <CardContent>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
              <Typography variant="h5">
                Your Saved Projects
              </Typography>
              <Button
                variant="contained"
                startIcon={<UploadFileIcon />}
                onClick={handleUploadFile}
              >
                Upload File
              </Button>
            </Box>
            <Box sx={{ mb: 3 }}>
              {projects.length === 0 ? (
                <Typography variant="body1" color="text.secondary">
                  There are no saved projects. You can start by uploading your JAR file.
                </Typography>
              ) : (
                <Grid container spacing={2}>
                  {projects.map((project) => (
                    <Grid item key={project.id} xs={12} sm={6} md={4}>
                      <Card 
                        variant="outlined" 
                        sx={{ 
                          height: 200, 
                          display: 'flex', 
                          flexDirection: 'column',
                          justifyContent: 'space-between'
                        }}
                      >
                        <CardContent>
                          <Typography variant="h6" noWrap>{project.name}</Typography>
                          <Typography variant="body2" color="text.secondary">
                            Created: {new Date(project.createdAt).toLocaleString()}
                          </Typography>
                        </CardContent>
                        <Box sx={{ p: 2, mt: 'auto' }}>
                          <Button variant="contained" color="primary" fullWidth>
                            Open Project
                          </Button>
                        </Box>
                      </Card>
                    </Grid>
                  ))}
                </Grid>
              )}
            </Box>
          </CardContent>
        </Card>
      </Container>
    </Box>
  );
};

export default SavedProjects;