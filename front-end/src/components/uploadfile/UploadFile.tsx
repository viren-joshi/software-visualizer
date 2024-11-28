import { Box,
  Button,
  Card,
  CardContent,
  CardHeader,
  Container,
  Grid,
  TextField,
  Typography,
  Alert,
  Snackbar,
  CircularProgress,
  Avatar,
} from "@mui/material";
import { useState } from "react";
import { useNavigate, Link } from 'react-router-dom';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import CheckCircleIcon from '@mui/icons-material/CheckCircle';
import LogoutIcon from '@mui/icons-material/Logout';
import FolderOpenIcon from '@mui/icons-material/FolderOpen';
import styled from "@emotion/styled";
import { User } from "firebase/auth";
import { getAuth, signOut } from "firebase/auth";
import JSZip from 'jszip';
import axios from "axios";

const VisuallyHiddenInput = styled('input')({
  clip: 'rect(0 0 0 0)',
  clipPath: 'inset(50%)',
  height: 1,
  overflow: 'hidden',
  position: 'absolute',
  bottom: 0,
  left: 0,
  whiteSpace: 'nowrap',
  width: 1,
});

const server_url = process.env.REACT_APP_SERVER_URL;

interface UploadFileProps {
  user: User;
}

const UploadFile: React.FC<UploadFileProps> = ({ user }) => {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [packageName, setPackageName] = useState<string>("");
  const [error, setError] = useState<string | null>(null); 
  const [isUploading, setIsUploading] = useState(false);
  const navigate = useNavigate();
  const auth = getAuth();

  const handleFileChange = async (event: any) => {
    const file = event.target.files?.[0];
    if (file) {
      const isValid = await validateJarFile(file);
      if (isValid) {
        setSelectedFile(file);
        setError(null);
      }
    }
  };

  const handleDrop = async (event: React.DragEvent) => {
    event.preventDefault();
    const file = event.dataTransfer.files[0];
    if (file) {
      const isValid = await validateJarFile(file);
      if (isValid) {
        setSelectedFile(file);
        setError(null);
      }
    }
  };

  const validateJarFile = async (file: File): Promise<boolean> => {
    const fileExtension = file.name.split('.').pop()?.toLowerCase();
    if (fileExtension !== 'jar') {
      setError("Invalid file type. Please upload a JAR file.");
      setSelectedFile(null);
      return false;
    }

    try {
      const zip = new JSZip();
      const contents = await zip.loadAsync(file);
      const classFiles = Object.keys(contents.files).filter(filename => filename.endsWith('.class'));

      if (classFiles.length === 0) {
        setError("The JAR file is empty or contains no classes.");
        setSelectedFile(null);
        return false;
      }

      return true;
    } catch (error) {
      setError("Failed to read the JAR file. Please try again.");
      setSelectedFile(null);
      return false;
    }
  };

  const handleDragOver = (event: React.DragEvent) => {
    event.preventDefault();
  };

  const handleUpload = async () => {
    if (!selectedFile) return;
    setIsUploading(true);
    try {
      const formData = new FormData();
      formData.append('file', selectedFile);
      formData.append('classContainer', packageName);

      const token = localStorage.getItem('soft-viz-tokenID');
      
      const uploadResponse = await axios.post(`${server_url}/initialize/upload`, formData, {
        headers: {
          'Authorization': token,
          'Content-Type': 'multipart/form-data',
        },
      });
      
      if (uploadResponse.status !== 200) {
        console.log("Failed!!");
        throw new Error('Upload failed');
      }

      let project_id = String(uploadResponse.data);
      localStorage.setItem('current-projectId', project_id);
      const internalDependencies = await axios.get(`${server_url}/initialize/intDep`, {
        headers: {
          'Authorization': token,
          'project_id': project_id,
          'Content-Type': 'multipart/form-data',
        },
      });

      const externalDependencies = await axios.get(`${server_url}/initialize/extDep`, {
        headers: {
          'Authorization': token,
          'project_id': project_id,
          'Content-Type': 'multipart/form-data',
        },
      });

      const classList = await axios.get(`${server_url}/initialize/classList`, {
        headers: {
          'Authorization': token,
          'project_id': project_id,
          'Content-Type': 'multipart/form-data',
        },
      });

      console.log('Upload successful:', uploadResponse.data);

      const response = {
        internalDependencyList: internalDependencies.data || [],
        externalDependencyList: externalDependencies.data || [],
        classNames: classList.data || [],
      };

      navigate('/mainpage',  { state: { response: response} });
    } catch (error) {
      setError(`Failed to upload file. Please try again: ${error}`);
    } finally {
      setIsUploading(false);
    }
  };

  const handleSignOut = async () => {
    try {
      await signOut(auth);
      localStorage.removeItem('soft-viz-tokenID');
      navigate('/signin');
    } catch (error) {
      setError('Failed to sign out. Please try again.');
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
            <Grid container spacing={3}>
              <Grid item xs={12} md={6}>
                <Box
                  sx={{
                    border: 2,
                    borderRadius: 2,
                    borderColor: selectedFile ? 'success.main' : 'grey.300',
                    borderStyle: 'dashed',
                    p: 3,
                    textAlign: 'center',
                    transition: 'all 0.3s',
                  }}
                  onDrop={handleDrop}
                  onDragOver={handleDragOver}
                >
                  {selectedFile ? (
                    <Box sx={{ color: 'success.main' }}>
                      <CheckCircleIcon sx={{ fontSize: 48, mb: 1 }} />
                      <Typography variant="body2">{selectedFile.name}</Typography>
                    </Box>
                  ) : (
                    <Box>
                      <CloudUploadIcon sx={{ fontSize: 48, mb: 1, color: 'text.secondary' }} />
                      <Typography variant="body2" color="text.secondary">
                        Drag & Drop or Click to Upload JAR file
                      </Typography>
                    </Box>
                  )}
                  <Button
                    component="label"
                    variant="outlined"
                    sx={{ mt: 2 }}>
                    Browse Files
                    <VisuallyHiddenInput type="file" accept=".jar" onChange={handleFileChange} />
                  </Button>
                </Box>
              </Grid>
              <Grid item xs={12} md={6}>
                <TextField
                  fullWidth
                  label="Package Name"
                  variant="outlined"
                  value={packageName}
                  onChange={(e: React.ChangeEvent<HTMLInputElement>) => setPackageName(e.target.value)}
                  sx={{ mb: 2 }}
                />
                <Button
                  fullWidth
                  variant="contained"
                  color="primary"
                  disabled={!selectedFile || packageName.trim() === "" || isUploading}
                  onClick={handleUpload}
                  sx={{ height: 56 }}
                >
                  {isUploading ? <CircularProgress size={24} /> : 'Visualize'}
                </Button>
              </Grid>
            </Grid>
            <Box sx={{ mt: 3, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Button
                variant="outlined"
                onClick={() => setSelectedFile(null)}
                disabled={!selectedFile}
              >
                Cancel
              </Button>
               <Button
                component={Link}
                to="/projects"
                variant="outlined"
                startIcon={<FolderOpenIcon />}
              >
                Saved Projects
              </Button>
            </Box>
          </CardContent>
        </Card>
      </Container>
      <Snackbar open={!!error} autoHideDuration={6000} onClose={() => setError(null)}>
        <Alert onClose={() => setError(null)} severity="error" sx={{ width: '100%' }}>
          {error}
        </Alert>
      </Snackbar>
    </Box>
  );
};
export default UploadFile;