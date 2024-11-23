import {
  Drawer,
  List,
  ListItem,
  ListItemText,
  ListItemButton,
  TextField,
  Typography,
  Button,
  Avatar,
  Box,
  Tabs,
  Tab,
  Divider,
} from '@mui/material';
import React, { useState } from "react";
import { useNavigate } from 'react-router-dom';
import { Search as SearchIcon, Logout as LogoutIcon } from '@mui/icons-material';
import { User } from 'firebase/auth';
import { getAuth, signOut } from 'firebase/auth';

interface SidebarProps {
  classNames: string[]
  
  alignment: string
  extDependencies: string[]
  onAlignmentChange: (
    newAlignment: string | null
  ) => void;
  onSelectClass: (className: string) => void
  open: boolean
  onClose: () => void
  isMobile: boolean
  user: User
}

const Sidebar: React.FC<SidebarProps> = ({
  classNames,
 
  alignment,
  extDependencies,
  onSelectClass,
  user,
  onAlignmentChange,
  open,
  onClose,
  isMobile,
}) => {
  const [searchQuery, setSearchQuery] = useState("")
  const filteredClassNames = classNames.filter((name) =>
    name.toLowerCase().includes(searchQuery.toLowerCase())
  )

  const filteredExtDependencies = extDependencies.filter((name) =>
    name.toLowerCase().includes(searchQuery.toLowerCase())
  )
  const navigate = useNavigate()
  const auth = getAuth()

  const handleSignOut = async () => {
    try {
      await signOut(auth)
      localStorage.removeItem('soft-viz-tokenID')
      navigate('/signin')
    } catch (error) {
      console.error('Failed to sign out', error)
    }
  }

  const drawerContent = (
    <>
      <Box sx={{ p: 2 }}>
        <TextField
          fullWidth
          variant="outlined"
          placeholder="Search..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          InputProps={{
            startAdornment: <SearchIcon color="action" />,
          }}
          size="small"
        />
      </Box>
      <Divider />
      <Tabs
        value={alignment}
        onChange={(event, newValue) => { 

          onAlignmentChange(newValue)

        }}
        centered
        sx={{ borderBottom: 1, borderColor: 'divider' }}
      >
        <Tab label="Internal" value="internal" />
        <Tab label="External" value="external" />
      </Tabs>
      <List sx={{ flexGrow: 1, overflow: 'auto' }}>
        {alignment === 'internal'
          ? filteredClassNames.map((name, index) => (
              <ListItem key={index} disablePadding>
                <ListItemButton onClick={() => onSelectClass(name)}>
                  <ListItemText primary={name} />
                </ListItemButton>
              </ListItem>
            ))
          : filteredExtDependencies.map((name, index) => (
              <ListItem key={index}>
                <ListItemText primary={name} />
              </ListItem>
            ))}
      </List>
      <Divider />
      <Box sx={{ p: 2 }}>
        <Button
          fullWidth
          variant="outlined"
          startIcon={<LogoutIcon />}
          onClick={handleSignOut}
        >
          Sign out
        </Button>
      </Box>
    </>
  )

  if (isMobile) {
    return (
      <Drawer
        variant="temporary"
        open={open}
        onClose={onClose}
        ModalProps={{
          keepMounted: true, // Better open performance on mobile.
        }}
        sx={{
          '& .MuiDrawer-paper': { boxSizing: 'border-box', width: 240 },
        }}
      >
        {drawerContent}
      </Drawer>
    )
  }

  return (
    <Drawer
      variant="persistent"
      open={open}
      sx={{
        width: 240,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: 240,
          boxSizing: 'border-box',
          top: 64, // height of AppBar
          height: 'calc(100% - 64px)',
        },
      }}
    >
      {drawerContent}
    </Drawer>
  )
}

export default Sidebar;