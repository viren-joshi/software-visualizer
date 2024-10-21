import { Grid2, Box, Divider, Typography, Button } from '@mui/material';

function Main() {
  return (
    <Grid2 container spacing={2} sx={{ height: '100vh' }}>
      {/* Left part */}
      <Grid2 size={3}>
        <Box
          sx={{
            height: '100%',
            display: 'flex',
            flexDirection: 'column',
            //justifyContent: 'center',
            padding: '20px',
          }}
        >
          <Typography variant="h4" gutterBottom>
            Classes & Entitites
          </Typography>
          <Typography variant="body1">
            Class-A
          </Typography>
          <Typography variant="body1">
            Class-B
          </Typography>
          {/* <Button variant="contained">Left Button</Button> */}
        </Box>
      </Grid2>

      {/* Vertical Divider */}
      <Divider orientation="vertical" flexItem  sx={{ borderColor: 'black' }}/>

      {/* Right part */}
      <Grid2 size={6}>
        <Box
          sx={{
            height: '100%',
            display: 'flex',
            flexDirection: 'column',
            //justifyContent: 'center',
            padding: '20px',
          }}
        >
          <Typography variant="h4" gutterBottom>
            Dependency Graph 
          </Typography>
          <Typography variant="body1" paragraph>
            Content goes here for the right part.
          </Typography>
        </Box>
      </Grid2>
    </Grid2>
  );
}

export default Main
