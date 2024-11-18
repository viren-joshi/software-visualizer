import {
  Grid2,
  Box,
  Divider,
  Typography,
  List,
  ListItemText,
  Container,
} from "@mui/material";
import Sidebar from "../sidebar/Sidebar";
import GraphWhiteBoard from "../graphWhiteBoard/GraphWhiteBoard";
import { useLocation } from "react-router-dom";
import { useState } from "react";

export interface ClassContainer {
  internalDependencyList: InternalDependency[];
  externalDependencyList: MavenDependency[];
}

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
  groupId: string; // Group identifier for the dependency
  scope: string; // Scope of the dependency (e.g., compile, test, etc.), can be empty
  artifactId: string; // Artifact identifier for the dependency
  version: string; // Version of the artifact (can be empty)
}

function Main() {
  const location = useLocation();
  const { response } = location.state || {};
  const classNames = response.classNames
    .split(",")
    .map((className: string) => className.trim().split(".").pop());
  const extDependencies = response.externalDependencyList.map(
    (externalDependency: MavenDependency) => externalDependency.artifactId
  );
  const [alignment, setAlignment] = useState<String>("internal");

  const [selectedClass, setSelectedClass] = useState<InternalDependency | null>(
    null
  ); // New state for selected class
  const [selectedFilter, setSelectedFilter] = useState<string>(''); // state for new filter
  const handleChange = (
    event: React.MouseEvent<HTMLElement>,
    newAlignment: string | null
  ) => {
    if (newAlignment !== null) {
      setSelectedClass(null);
      setAlignment(newAlignment); // Update the alignment state
    }
  };
  const handleClassSelect = (className: string) => {
    const selected = response.internalDependencyList.find(
      (userClass: InternalDependency) =>
        userClass.name.split(".").pop() === className
    );
    setSelectedClass(selected || null); // Update the selected class
  };
  const handleFilterChange = (filter: string) => {
    setSelectedFilter(filter); // updating selected filter state
  };

  return (
    <Grid2 container spacing={2} sx={{ height: "100vh" }}>
      <Grid2 size={2}>
        <Box
          sx={{
            height: "100%",
            display: "flex",
            flexDirection: "column",
            padding: "20px",
          }}
        >
          <Sidebar
            classNames={classNames}
            handleChange={handleChange}
            alignment={alignment}
            extDependencies={extDependencies}
            onSelectClass={handleClassSelect}
          />
        </Box>
      </Grid2>

      {/* Vertical Divider */}
      <Divider orientation="vertical" flexItem sx={{ borderColor: "black" }} />

      {/* Right part */}
      <Grid2 size={9}>
        <Box
          sx={{
            height: '100%',
            width: '100%',
            display: 'flex',
            flexDirection: 'column',
            padding: '20px 0px 20px 20px',
            boxSizing: 'border-box',
          }}
        >
          <Container sx={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center'
          }}>
            <Typography variant="h5" gutterBottom>
              Internal Dependency Graph
            </Typography>
            <Typography variant="h5" gutterBottom>
              {/* put custom views button here */}
            </Typography>
          </Container>
          
          <GraphWhiteBoard  jsonData={response} alignment={alignment} selectedClass={selectedClass} />
        </Box>
      </Grid2>
    </Grid2>
  );
}

export default Main;
