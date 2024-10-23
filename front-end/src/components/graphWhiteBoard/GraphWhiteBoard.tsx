import { Typography } from '@mui/material'
import React from 'react'
import dagre from 'dagre';
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

// implementaiton of dagre in the graph 
const dagreGraph = new dagre.graphlib.Graph();
dagreGraph.setDefaultEdgeLabel(() => ({}));

const nodeWidth = 172;
const nodeHeight = 36;

// layouting nodes and edges as per ranking
const getLayoutedElements = (nodes: any, edges: any) => {
  const isHorizontal = false;
  dagreGraph.setGraph({ rankdir: isHorizontal ? 'LR' : 'TB' });

  // setNodes parsing in dagre
  nodes.forEach((node: any) => {
    dagreGraph.setNode(node.id, { width: nodeWidth, height: nodeHeight });
  });

  // setEdge parsing in dagre
  edges.forEach((edge: any) => {
    dagreGraph.setEdge(edge.source, edge.target);
  });

  dagre.layout(dagreGraph);

  nodes.forEach((node: any) => {
    const nodeWithPosition = dagreGraph.node(node.id);
    node.position = {
      x: nodeWithPosition.x - nodeWidth / 2,
      y: nodeWithPosition.y - nodeHeight / 2,
    };
    return node;
  });

  return { nodes, edges };
};

function GraphWhiteBoard() {
  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);

  const onConnect = useCallback(
    (params: Connection) => setEdges((eds) => addEdge(params, eds)),
    [setEdges],
  );

  // running dagre in nodes
  useEffect(() => {
    const layoutedElements = getLayoutedElements(nodes, edges);
    setNodes([...layoutedElements.nodes]);
    setEdges([...layoutedElements.edges]);
  }, []);

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
