import {
  Box,
  InputAdornment,
  Link,
  List,
  ListItemText,
  TextField,
  ToggleButton,
  ToggleButtonGroup,
  Typography,
} from "@mui/material";
import React, { useState } from "react";
import SearchIcon from "../../assets/SearchIcon";

export interface SidebarProps {
  classNames: string[];
  handleChange: (
    event: React.MouseEvent<HTMLElement>,
    newAlignment: string | null
  ) => void;
  alignment: String;
  extDependencies: string[];
  onSelectClass: (className: string) => void;
}

const Sidebar: React.FC<SidebarProps> = ({
  classNames,
  handleChange,
  alignment,
  extDependencies,
  onSelectClass,
}) => {
  const [searchQuery, setSearchQuery] = useState("");
  const handleSearchChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setSearchQuery(event.target.value);
  };
  const filteredClassNames = classNames.filter((name) =>
    name.toLowerCase().includes(searchQuery.toLowerCase())
  );
  const filteredExtDependencies = extDependencies.filter((name) =>
    name.toLowerCase().includes(searchQuery.toLowerCase())
  );
  return (
    <div>
      {/* the sidebar contains logo, searchbar, list of classes */}
      {/* button section for changing the graph view from internal to external depenency goes here */}
      <TextField
        variant="outlined"
        placeholder="Search..."
        value={searchQuery}
        onChange={handleSearchChange}
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <SearchIcon />
            </InputAdornment>
          ),
        }}
        fullWidth
      />
      <Box mt={3} mb={3}>
        <ToggleButtonGroup
          color="primary"
          value={alignment}
          exclusive
          onChange={handleChange}
          aria-label="Platform"
        >
          <ToggleButton
            value="internal"
            sx={{ textTransform: "none", width: "115px" }}
          >
            Internal
          </ToggleButton>
          <ToggleButton
            value="external"
            sx={{ textTransform: "none", width: "112px" }}
          >
            External
          </ToggleButton>
        </ToggleButtonGroup>
      </Box>
      {alignment === "internal" ? (
        <>
          {" "}
          <Typography variant="h5" gutterBottom>
            Class List 
            <Typography variant="h6"> ({classNames.length} classes) </Typography>
          </Typography>
          {filteredClassNames.map((name: any, index: any) => {
            return (
              <List key={index}>
                <ListItemText>
                  <Link
                    color="inherit"
                    sx={{ cursor: "pointer" }}
                    onClick={() => onSelectClass(name)}
                  >
                    {name}
                  </Link>
                </ListItemText>
              </List>
            );
          })}
        </>
      ) : (
        <>
          <Typography variant="h5" gutterBottom>
            Dependency List
          </Typography>
          {filteredExtDependencies.map((name: any, index: any) => {
            return (
              <List key={index}>
                <ListItemText>{name}</ListItemText>
              </List>
            );
          })}
        </>
      )}
    </div>
  );
};

export default Sidebar;
