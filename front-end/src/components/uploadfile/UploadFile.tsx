import { Box, Button, Grid2, Input, TextField, Typography } from "@mui/material";
import { SetStateAction, useState } from "react";
import { useNavigate } from 'react-router-dom';
import PlusSignIcon from "../../assets/PlusSignIcon";

const UploadFile = () => {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [fileName,setFileName]= useState<String>();
  const [packageName,setPackageName]= useState<String>("");
  const [error, setError] = useState<string | null>(null); 
  const navigate = useNavigate();

  const handleFileChange = (event: any) => {
    const file = event.target.files[0];
    if (file) {
      const fileExtension = file.name.split('.').pop().toLowerCase();
      if (fileExtension !== 'jar') {
        setError("Invalid file type. Please upload a JAR file.");
        setSelectedFile(null); // Clear the selected file
       
      } else {
        setError(null); // Clear error
        setSelectedFile(file); // Set the valid JAR file
        setFileName(file.name);
      }
    }
  };
  const handleDrop = (event: React.DragEvent) => {
    event.preventDefault(); // Prevent default behavior (file open)
    const file = event.dataTransfer.files[0];
    if (file && file.name) {
      const fileExtension = file.name.split('.').pop()?.toLowerCase();
      if (fileExtension !== 'jar') {
        setError("Invalid file type. Please upload a JAR file.");
        setSelectedFile(null); // Clear the selected file
        
      } else {
        setError(null); // Clear error
        setSelectedFile(file); // Set the valid JAR file
        setFileName(file.name);
      }
    }
  };

  const handleDragOver = (event: React.DragEvent) => {
    event.preventDefault(); // Prevent default behavior (file open)
  };

  const navigateToNextPage =() =>{
    console.log(packageName);
    navigate('/mainpage');
  };
  return (
    <>
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
          onDrop={handleDrop} // handle file drop
          onDragOver={handleDragOver} // prevent default behavior
          //onClick={() => document.getElementById('file-input').click()}
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
            <Button variant="contained" color="primary"  disabled={packageName.trim() === "" || !selectedFile }
          onClick={navigateToNextPage} fullWidth style={{textTransform:'none'}}>visualize</Button>
            </Box>
            </Box>
        </Grid2>
        </Grid2>
      </Grid2>
    </Grid2>
      
      
      <Box mt={2}>
        <Button variant="outlined" onClick={() => setSelectedFile(null)}>
          Cancel
        </Button>
      </Box>
    </>
  );
};

export default UploadFile;
