import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
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
  CircularProgress,
} from '@mui/material';
import LogoutIcon from '@mui/icons-material/Logout';
import UploadFileIcon from '@mui/icons-material/UploadFile';
import { User } from 'firebase/auth';
import { getAuth, signOut } from 'firebase/auth';

const server_url = process.env.REACT_APP_SERVER_URL;

interface Project {
  projectId: string;
  custom_view: string;
}

interface SavedProjectsProps {
  user: User;
}

const SavedProjects: React.FC<SavedProjectsProps> = ({ user }) => {
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const auth = getAuth();

  useEffect(() => {
    const fetchProjects = async () => {
      try {
        const idToken = await user.getIdToken();
        const response = await axios.get<Project[]>(`${server_url}/initialize/userProjects`, {
          headers: {
            'Authorization': idToken,
          },
        });

        if (response.status === 200) {
          setProjects(response.data);
        } else {
          console.error('Failed to fetch projects');
        }
      } catch (error) {
        console.error('Error fetching projects:', error);
      }
    };

    fetchProjects();
  }, [user]);

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

  const handleOpenProject = async (projectId: string) => {
    setLoading(true);
    try {
      const idToken = await user.getIdToken();
      
      const [internalDependencies, externalDependencies, classList] = await Promise.all([
        axios.get(`${server_url}/initialize/intDep`, {
          headers: {
            'Authorization': idToken,
            'project_id': projectId,
          },
        }),
        axios.get(`${server_url}/initialize/extDep`, {
          headers: {
            'Authorization': idToken,
            'project_id': projectId,
          },
        }),
        axios.get(`${server_url}/initialize/classList`, {
          headers: {
            'Authorization': idToken,
            'project_id': projectId,
          },
        }),
      ]);

      const response = {
        internalDependencyList: internalDependencies.data || [],
        externalDependencyList: externalDependencies.data || [],
        classNames: classList.data || [],
      };

      navigate('/mainpage', { state: { response: response } });
    } catch (error) {
      console.error('Error opening project:', error);
    } finally {
      setLoading(false);
    }
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
                    <Grid item key={project.projectId} xs={12} sm={6} md={4}>
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
                          <Typography variant="h6" noWrap>Project {project.projectId}</Typography>
                          <Typography variant="body2" color="text.secondary">
                            Custom View: {project.custom_view || 'None'}
                          </Typography>
                        </CardContent>
                        <Box sx={{ p: 2, mt: 'auto' }}>
                          <Button 
                            variant="contained" 
                            color="primary" 
                            fullWidth
                            onClick={() => handleOpenProject(project.projectId)}
                            disabled={loading}
                          >
                            {loading ? <CircularProgress size={24} /> : 'Open Project'}
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