import {
  ThemeProvider,
  createTheme,
  CssBaseline,
  Button,
  AppBar,
  Toolbar,
  Avatar,
  IconButton,
  useMediaQuery,
  Box,
  Divider,
  Typography,
  List,
  ListItemText,
  Container,
} from "@mui/material";
import { CloudUpload, FolderOpen, Menu as MenuIcon } from "@mui/icons-material";
import Sidebar from "../sidebar/Sidebar";
import GraphWhiteBoard from "../graphWhiteBoard/GraphWhiteBoard";
import { useLocation, Navigate } from "react-router-dom";
import { useState } from "react";
import { User } from "firebase/auth";

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
interface MainProps {
  user: User;
}
const theme = createTheme({
  palette: {
    primary: {
      main: "#1976d2",
    },
    secondary: {
      main: "#dc004e",
    },
    background: {
      default: "#f5f5f5",
      paper: "#ffffff",
    },
  },
});
const Main: React.FC<MainProps> = ({ user }) => {
  const location = useLocation();
  const { response } = location.state || {};
  const classNames =
    response?.classNames.map((className: string) =>
      className.trim().split(".").pop()
    ) || [];

  const extDependencies =
    response.externalDependencyList?.map(
      (externalDependency: MavenDependency) => externalDependency.artifactId
    ) || [];
  const [alignment, setAlignment] = useState<string>("internal");
  const [selectedClass, setSelectedClass] = useState<InternalDependency | null>(
    null
  ); // New state for selected class
  const [selectedFilter, setSelectedFilter] = useState<string>(""); // state for new filter
  const [isCustomView, setIsCustomView] = useState(false); // State to track custom view mode

  const handleChange = (newAlignment: string | null) => {
    if (newAlignment !== null) {
      setSelectedClass(null);
      setAlignment(newAlignment); // Update the alignment state
    }
  };
  const handleClassSelect = (className: string) => {
    const selected = response?.internalDependencyList?.find(
      (userClass: InternalDependency) =>
        userClass.name.split(".").pop() === className
    );
    setSelectedClass(selected || null); // Update the selected class
  };
  const handleFilterChange = (filter: string) => {
    setSelectedFilter(filter); // updating selected filter state
  };
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"));
  const openInNewTab = (url: string) => {
    window.open(url, "_blank", "noopener,noreferrer");
  };
  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen);
  };
  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <Box sx={{ display: "flex", flexDirection: "column", height: "100vh" }}>
        <AppBar position="static" color="default" elevation={1}>
          <Toolbar>
            {isMobile && (
              <IconButton
                edge="start"
                color="inherit"
                aria-label="menu"
                onClick={toggleSidebar}
                sx={{ mr: 2 }}
              >
                <MenuIcon />
              </IconButton>
            )}
            <Typography variant="h4" component="div" sx={{ flexGrow: 1 }}>
              Software Visualizer
            </Typography>
            <Button
              color="primary"
              variant="contained"
              startIcon={<CloudUpload />}
              onClick={() => openInNewTab("/upload")}
              sx={{ mr: 2 }}
            >
              Upload File
            </Button>
            <Button
              color="primary"
              variant="contained"
              startIcon={<FolderOpen />}
              onClick={() => openInNewTab("/projects")}
              sx={{ mr: 2 }}
            >
              Saved Projects
            </Button>
            <Box sx={{ display: "flex", alignItems: "center" }}>
              <Avatar src={user.photoURL || undefined} sx={{ mr: 2 }}>
                {user.displayName?.charAt(0) ||
                  user.email?.charAt(0).toUpperCase()}
              </Avatar>
              <Typography variant="subtitle2" sx={{ mr: 1 }}>
                {user.displayName || user.email}
              </Typography>
            </Box>
          </Toolbar>
        </AppBar>
        <Box sx={{ display: "flex", flexGrow: 1, overflow: "hidden" }}>
          <Sidebar
            user={user}
            classNames={classNames}
            extDependencies={extDependencies}
            alignment={alignment}
            onAlignmentChange={handleChange}
            onSelectClass={handleClassSelect}
            open={sidebarOpen}
            onClose={toggleSidebar}
            isMobile={isMobile}
          />
          <Box component="main" sx={{ flexGrow: 1, p: 3, overflow: "auto" }}>
            <GraphWhiteBoard
              jsonData={response}
              alignment={alignment}
              selectedClass={selectedClass}
              isCustomView={isCustomView}
              setIsCustomView={setIsCustomView}
            />
          </Box>
        </Box>
      </Box>
    </ThemeProvider>
  );
};
export default Main;
