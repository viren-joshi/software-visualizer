import { Grid2, Box, Divider, Typography, List, ListItemText } from '@mui/material';
import Sidebar from '../sidebar/Sidebar';
import GraphWhiteBoard from '../graphWhiteBoard/GraphWhiteBoard';
import { useLocation } from 'react-router-dom';
import { useState } from 'react';

export interface ClassContainer {
  internalDependencyList: InternalDependency[];
  externalDependencyList: MavenDependency[];
};

export interface InternalDependency {
  name: string;
  inherits: string;
  classType: string;
  variableList: Variable[];
  methodList: Method[];
  isNested: boolean;
  isControllerClass: boolean;
  nestedClassesList: InternalDependency[];
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

export interface MavenDependency {
  groupId: string;  // Group identifier for the dependency
  scope: string;  // Scope of the dependency (e.g., compile, test, etc.), can be empty
  artifactId: string;  // Artifact identifier for the dependency
  version: string;  // Version of the artifact (can be empty)
}

function Main() {
  const location = useLocation(); 
  const { response } = location.state || {};
  const classNames = response.classNames.split(',').map((className: string) => className.trim().split('.').pop());
  const extDependencies= response.externalDependencyList.map((externalDependency: MavenDependency) => externalDependency.artifactId);
  const [alignment, setAlignment] = useState<String>('internal');
  const handleChange = (
    event: React.MouseEvent<HTMLElement>,
    newAlignment: string | null
  ) => {
    if (newAlignment !== null) {
      setAlignment(newAlignment); // Update the alignment state
    }
  };
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
          <Sidebar classNames={classNames} handleChange={handleChange} alignment={alignment} extDependencies={extDependencies}/>
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
            <GraphWhiteBoard  jsonData={response} alignment={alignment}/>
        </Box>
      </Grid2>
    </Grid2>
  );
}

export default Main
