import { Typography } from '@mui/material'
import React from 'react'
import { ChangeEventHandler, useCallback, useState, useEffect } from 'react';
import { ReactFlow, 
    useNodesState,
    useEdgesState,
    addEdge, 
    Connection
  } from "@xyflow/react";

import '@xyflow/react/dist/style.css';
import { Container, Box } from '@mui/material';

import { initialNodes } from './Nodes';
import { initialEdges } from './Edges';

function GraphWhiteBoard() {
  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);

  const onConnect = useCallback(
    (params: Connection) => setEdges((eds) => addEdge(params, eds)),
    [setEdges],
  );

  return (
    <div>
      {/* graph goes here */}
      <Typography variant="h4" gutterBottom>
            Dependency Graph 
          </Typography>
          <Typography variant="body1" paragraph>
            <Container fixed>
            <Box sx={{ 
                    bgcolor: '#ebedef', 
                    height: '70vh',
                    padding: '10px', 
                    marginTop: '50px'
                }}>
                <ReactFlow 
                    nodes={nodes} 
                    edges={edges} 
                    onNodesChange={onNodesChange}
                    onEdgesChange={onEdgesChange}
                    onConnect={onConnect}
                    fitView
                >
                </ReactFlow>
            </Box>
            
        </Container>
          </Typography>
    </div>
  )
}

export default GraphWhiteBoard
