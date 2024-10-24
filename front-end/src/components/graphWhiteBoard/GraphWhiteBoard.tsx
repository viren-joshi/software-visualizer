import { Typography } from '@mui/material'
import React from 'react'
import dagre from 'dagre';
import { useCallback, useEffect } from 'react';
import { ReactFlow, 
    useNodesState,
    useEdgesState,
    addEdge, 
    Connection
  } from "@xyflow/react";

import '@xyflow/react/dist/style.css';
import { Container, Box } from '@mui/material';
import { ClassContainer } from '../mainpage/Main';

// interfaces
export interface GraphWhiteBoardProps {
  jsonData: ClassContainer

}
export interface NodeData extends Record<string, unknown> {
  label: string;
}

export interface Position {
  x: number;
  y: number;
}

export interface Node {
  id: string; // Unique identifier for the node
  type: string; // Type of the node (e.g., 'input', 'output')
  position: Position; // Position of the node on the canvas
  data: NodeData; // Custom data associated with the node (e.g., the label)
}

export interface Edge {
  id: string; // Unique identifier for the edge
  source: string; // ID of the source node
  target: string; // ID of the target node
  label?: string; // Optional label describing the relationship (e.g., 'inheritance', 'composition')
}

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

const GraphWhiteBoard:React.FC<GraphWhiteBoardProps> = ({jsonData}) =>  {
  const initialNodes: Node[] = [];
  const initialEdges: Edge[] = [];
  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);

  const onConnect = useCallback(
    (params: Connection) => setEdges((eds) => addEdge(params, eds)),
    [setEdges],
  );

  // setting up nodes list
  useEffect(() => {
    const nodesList: Node[] = [];
    const edgesList: Edge[] = [];

    // creating nodes and edges from the jsonData
    jsonData.userClassList.forEach((classItem) => {
      nodesList.push({
        id: classItem.name.split('.').pop()!,
        type: 'default',
        data: { label: classItem.name.split('.').pop()! },
        position: { x: 0, y: 0 }, // temporary position, will be set by dagre layout
      });

      // creating inheritance edges
      if (classItem.inherits) {
        edgesList.push({
          id: `e-${classItem.name}-${classItem.inherits}`,
          source: classItem.name.split('.').pop()!,
          target: classItem.inherits.split('.').pop()!,
          label: 'inheritance',
        });
      }

      // creating implementation edges
      if (classItem.implementationList.length > 0) {
        classItem.implementationList.forEach((implClass) => {
          edgesList.push({
            id: `e-${classItem.name}-${implClass}`,
            source: classItem.name.split('.').pop()!,
            target: implClass.split('.').pop()!,
            label: 'implementation',
          });
        });
      }
    });
    const layoutedElements = getLayoutedElements(nodesList, edgesList);
    setNodes([...layoutedElements.nodes]);
    setEdges([...layoutedElements.edges]);
  }, [setNodes, setEdges]);

  return (
    <div>
      {/* graph goes here */}
      <Typography variant="h4" gutterBottom>
            Internal Dependency Graph 
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
