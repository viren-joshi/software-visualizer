import { Box, Button, Grid, Typography } from "@mui/material";
import { useState } from "react";
import CloudIcon from "./assets/CloudIcon";

const UploadFile = () => {
  const [selectedFile, setSelectedFile] = useState(null);

  // const handleFileChange = (event) => {
  //   setSelectedFile(event.target.files[0]);
  // };

  return (
    <Box
      sx={{
        border: '1px dashed grey',
        padding: '16px',
        borderRadius: '8px',
        textAlign: 'center',
        width: '400px',
        margin: 'auto',
      }}
    >
      <Typography variant="h6" gutterBottom>
        File Upload
      </Typography>
      <Box
        sx={{
          border: '1px dashed grey',
          borderRadius: '8px',
          padding: '20px',
          cursor: 'pointer',
        }}
        //onClick={() => document.getElementById('file-input').click()}
      >
        <CloudIcon/>
        <Typography>Click or drag file to this area to upload</Typography>
        <Typography variant="caption" display="block">
          Formats accepted are .jar
        </Typography>
      </Box>

      <input
        id="file-input"
        type="file"
        accept=".jar"
        style={{ display: 'none' }}
        //onChange={handleFileChange}
      />

      <Box mt={2}>
        <Button variant="outlined" onClick={() => setSelectedFile(null)}>
          Cancel
        </Button>
        <Button
          variant="contained"
          color="primary"
          sx={{ ml: 2 }}
          disabled={!selectedFile}
        >
          Continue
        </Button>
      </Box>
    </Box>
  );
};

export default UploadFile;
