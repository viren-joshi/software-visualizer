import { Box, Button, CircularProgress, Grid2, TextField, Typography } from "@mui/material";
import { useState } from "react";
import { useNavigate } from 'react-router-dom';
import PlusSignIcon from "../../assets/PlusSignIcon";
import axios from "axios";

const UploadFile = () => {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [fileName, setFileName] = useState<string>("");
  const [packageName, setPackageName] = useState<string>("");
  const [error, setError] = useState<string | null>(null); 
  const [loading, setLoading] = useState<boolean>(false);
  const navigate = useNavigate();

  const handleFileChange = (event: any) => {
    const file = event.target.files[0];
    if (file) {
      const fileExtension = file.name.split('.').pop().toLowerCase();
      if (fileExtension !== 'jar') {
        setError("Invalid file type. Please upload a JAR file.");
        setSelectedFile(null);
       
      } else {
        setError(null);
        setSelectedFile(file);
        setFileName(file.name);
      }
    }
  };
  const handleDrop = (event: React.DragEvent) => {
    event.preventDefault();
    const file = event.dataTransfer.files[0];
    if (file && file.name) {
      const fileExtension = file.name.split('.').pop()?.toLowerCase();
      if (fileExtension !== 'jar') {
        setError("Invalid file type. Please upload a JAR file.");
        setSelectedFile(null);
        
      } else {
        setError(null);
        setSelectedFile(file);
        setFileName(file.name);
      }
    }
  };

  const handleDragOver = (event: React.DragEvent) => {
    event.preventDefault();
  };

  const navigateToNextPage =async () =>{
    setLoading(true); // Start loading
    try {
    const formData = new FormData();
    formData.append('file', selectedFile!);
    formData.append('classContainer', packageName!);
    const response = await axios.post('http://csci5308-vm8.research.cs.dal.ca:8080/initialize/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    navigate('/mainpage',  { state: { response: response.data } });
  } catch (error) {
    console.error("Error uploading file:", error);
  } finally {
    setLoading(false); // Stop loading
  }};
  return (
    <>
    {loading && ( 
      <Box
      sx={{
        position: 'fixed',
        top: 0,
        left: 0,
        width: '100%',
        height: '100%',
        backgroundColor: 'rgba(255, 255, 255, 0.8)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 1000,
      }}
    >
          <CircularProgress size={60} />
    </Box>
  )}
    <Grid2 container spacing={2}>
      <Grid2 size={8}>
        <Typography variant="h4" gutterBottom>
        Software Visualizer
        </Typography>
      </Grid2>
      <Grid2 size={4}>
        <Button variant="outlined" sx={{ textTransform: 'none' }}>Sign In</Button>
      </Grid2>
      <Grid2>
      <Grid2 container >
      <Grid2 size={{md:6} } >
        <Box
          sx={{
            border: '1px solid grey',
            borderRadius: '8px',
            padding: '20px',
            textAlign: 'center',
            width: '400px',
            height:'200px'
          }}
          onDrop={handleDrop}
          onDragOver={handleDragOver}
          >
          <PlusSignIcon/>
          <Typography>Drag & Drop</Typography>
          <Typography display="block">OR</Typography>
          <label htmlFor="upload-photo" style={{cursor: 'pointer',display:"block",textDecoration:"underline",color:"blue"}}>Browse</label>
          <input  style={{ display: 'none' }} 
            id="upload-photo"  
            name="upload-photo"  
            type="file"
            accept=".jar"
            onChange={handleFileChange}
          />
          
          {error && (
              <Typography variant="body2" color="error" mt={1}>
                {error}
              </Typography>
            )}
          {selectedFile &&  (
              <Typography variant="body2" color="success" mt={1}>
              {fileName + ` File uploaded successfully`}
            </Typography>
            )
            }
        </Box>
        </Grid2>
        <Grid2 size={{md:6}} >
        <Box
          sx={{
            border: '1px solid grey',
            borderRadius: '8px',
            padding: '20px',
            width: '400px',
            height:'200px'
          }}
          >
            <Typography>
              enter package name
            </Typography>
            <TextField fullWidth onChange={(event)=> setPackageName(event.target.value)}  value={packageName}/>
            <Box mt={4} >
            <Button variant="contained" color="primary" disabled={packageName.trim() === "" || !selectedFile || loading}
          onClick={navigateToNextPage} fullWidth style={{textTransform:'none'}}>visualize</Button>
            </Box>
            </Box>
        </Grid2>
        </Grid2>
      </Grid2>
    </Grid2>
      
      <Box mt={2}>
        <Button variant="outlined" onClick={() => setSelectedFile(null)} disabled={loading}>
          Cancel
        </Button>
      </Box>
    </>
  );
};

export default UploadFile;

