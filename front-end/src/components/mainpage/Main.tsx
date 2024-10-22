import { Grid2, Box, Divider, Typography, List, ListItemText } from '@mui/material';
import Sidebar from '../sidebar/Sidebar';
import GraphWhiteBoard from '../graphWhiteBoard/GraphWhiteBoard';
import { useLocation } from 'react-router-dom';

export interface ClassContainer {
  classContainer: string;
  userClassList: UserClass[];
  externalDependencyList: string[];
};

export interface UserClass {
  name: string;
  inherits: string;
  classType: string;
  variableList: Variable[];
  methodList: Method[];
  isNested: boolean;
  isControllerClass: boolean;
  nestedClassesList: UserClass[];
  annotations: string[];
  implementationList: string[];
}

export interface Variable {
  identifier: string;
  datatype: string;
  annotationList: string[];
  isStatic: boolean;
  isAnnotated: boolean;
}

export interface Method {
  methodName: string;
  annotations: string[];
  isStatic: boolean;
}

function Main() {
  const location = useLocation();
  const { response } = location.state || {};
  const classNames = response.userClassList.map((userClass:UserClass) => userClass.name.split('.').pop());
  return (
    <Grid2 container spacing={2} sx={{ height: '100vh' }}>
      <Grid2 size={3}>
        <Box
          sx={{
            height: '100%',
            display: 'flex',
            flexDirection: 'column',
            padding: '20px',
          }}
        >
          <Sidebar classNames={classNames}/>
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
            padding: '20px',
          }}
        >
          <GraphWhiteBoard/>
        </Box>
      </Grid2>
    </Grid2>
  );
}

export default Main
