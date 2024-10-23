import { List, ListItemText, Typography } from '@mui/material'
import React from 'react'

export interface SidebarProps {
  classNames: string[];
}

const Sidebar:React.FC<SidebarProps> = ({ classNames
}) => {
  return (
    <div>
      {/* the sidebar contains logo, searchbar, list of classes */}
      {/* button section for changing the graph view from internal to external depenency goes here */}
      <Typography variant="h4" gutterBottom>
            Classes & Entitites
          </Typography>
          {classNames.map((name: any, index: any) => {
              return (
              <List key={index} >
                <ListItemText>{name}</ListItemText>
              </List>
              )
            })}
    </div>
  )
};

export default Sidebar;
