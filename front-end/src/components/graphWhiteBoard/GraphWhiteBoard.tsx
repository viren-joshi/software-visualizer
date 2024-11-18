import { List, ListItem, ListItemText, Typography } from '@mui/material'
import React from 'react'
import dagre from 'dagre';
import { useCallback, useEffect } from 'react';
import { ReactFlow,
    Background,
    Edge,
    Node, 
    useNodesState,
    useEdgesState,
    addEdge, 
    Connection,
    MarkerType,

  } from "@xyflow/react";

import '@xyflow/react/dist/style.css';
import { Container, Box } from '@mui/material';
import { ClassContainer, InternalDependency } from '../mainpage/Main';
import Filter from '../filter/Filter';

export interface GraphWhiteBoardProps {
  jsonData: ClassContainer;
  alignment: String;
  selectedClass: InternalDependency | null; // New prop for selected class
}
export interface NodeData extends Record<string, unknown> {
  label: string;
}

export interface Position {
  x: number;
  y: number;
}

// export interface Node {
//   id: string; // Unique identifier for the node
//   type: string; // Type of the node (e.g., 'input', 'output')
//   position: Position; // Position of the node on the canvas
//   data: NodeData; // Custom data associated with the node (e.g., the label)
// }export interface Edge {
//   id: string; // Unique identifier for the edge
//   source: string; // ID of the source node
//   target: string; // ID of the target node
//   label?: string; // Optional label describing the relationship (e.g., 'inheritance', 'composition')
// }

// implementaiton of dagre in the graph
const dagreGraph = new dagre.graphlib.Graph();
dagreGraph.setDefaultEdgeLabel(() => ({}));

const nodeWidth = 172;
const nodeHeight = 36;

// layouting nodes and edges as per ranking
const getLayoutedElements = (nodes: any, edges: any) => {
  const isHorizontal = false;
  dagreGraph.setGraph({ rankdir: isHorizontal ? 'LR' : 'TB', nodesep: 200, ranksep: 150 });

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

const GraphWhiteBoard: React.FC<GraphWhiteBoardProps> = ({
  jsonData,
  alignment,
  selectedClass
}) => {
  const initialNodes: Node[] = [];
  const initialEdges: Edge[] = [];
  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);

  const onConnect = useCallback(
    (params: Connection) => setEdges((eds) => addEdge(params, eds)),
    [setEdges]
  );

  // setting up nodes list
  useEffect(() => {
    const nodesList: Node[] = [];
    const edgesList: Edge[] = [];

    // creating nodes and edges from the jsonData
    jsonData.internalDependencyList.forEach((classItem) => {
      nodesList.push({
        id: classItem.name.split(".").pop()!,
        type: "default",
        data: { label: classItem.name.split(".").pop()! },
        position: { x: 0, y: 0 }, // temporary position, will be set by dagre layout
      });

      // creating inheritance edges
      if (classItem.inherits) {
        edgesList.push({
          id: `e-${classItem.name}-${classItem.inherits}`,
          source: classItem.name.split(".").pop()!,
          target: classItem.inherits.split(".").pop()!,
          label: "inheritance",
        });
      }

      // creating implementation edges
      if (classItem.implementationList.length > 0) {
        classItem.implementationList.forEach((implementationList) => {
          edgesList.push({
            id: `e-${classItem.name}-${implementationList}`,
            source: classItem.name.split('.').pop()!,
            target: implementationList.split('.').pop()!,
            label: 'implementation',
          });
        });
      }
      
      // creating composition edges
      // classItem.variableList.forEach(variable => {
      //   if(variable.datatype.includes(jsonData.classContainer)) {
      //     edgesList.push({
      //       id: `e-${classItem.name}-${variable.identifier}`,
      //       source: classItem.name.split('.').pop()!,
      //       target: variable.datatype.split('.').pop()!,
      //       label: 'composition',
      //       markerEnd: {
      //         type: MarkerType.Arrow,
      //       },
      //     });  
      //   }
        
      // });

      classItem.variableList.forEach((variable) => {
        // Check if the datatype matches any class in the jsonData (composition relationship)
        const targetClass = jsonData.internalDependencyList.find(
          (dep) => dep.name === variable.datatype
        );
    
        if (targetClass) {
          edgesList.push({
            id: `e-${classItem.name}-${variable.datatype}`,
            source: classItem.name.split('.').pop()!,
            target: variable.datatype.split('.').pop()!,
            label: 'composition',
            markerEnd: {
              type: MarkerType.Arrow, // Add arrow marker to indicate dependency direction
            },
          });
        }
      });
    });
    const layoutedElements = getLayoutedElements(nodesList, edgesList);
    setNodes([...layoutedElements.nodes]);
    setEdges([...layoutedElements.edges]);
  }, [setNodes, setEdges]);


  // filter change handler (updating node colors)
  const handleFilterChange = (filter: string) => {
    // get edges that match the selected filter string
    const filteredEdges = edges.filter((edge) => edge.label === filter);

    // Get the node IDs that are involved in the filtered edges
    const filteredNodeIds = new Set<string>();
    filteredEdges.forEach((edge) => {
      filteredNodeIds.add(edge.source); // Add source node
      filteredNodeIds.add(edge.target); // Add target node
    });

    // updating the nodes that are involved in the selected dependency
    setNodes((nodes) => 
      nodes.map((node) => {
        let newColor = '#eee'; //default color

        // set the color for nodes involved
        if(filteredNodeIds.has(node.id)) {
          if(filter == 'inheritance') {
            newColor = '#5F9EA0';
          } else if (filter === 'implementation') {
            newColor = '#E1C16E';
          } else if (filter === 'composition') {
            newColor = '#8A9A5B';
          }
        }

        return {
          ...node,
          style: { ...node.style, backgroundColor: newColor }, // updating node style
        };
      })
    )
  }

  return (
    <div>
      {/* graph goes here */}
      {alignment === "internal" ? (
        selectedClass ? (
          <Container fixed>
            <Typography variant="h5" gutterBottom>
              Class: {selectedClass.name}
            </Typography>
            {selectedClass.variableList?.length > 0 && (
              <>
                <Typography variant="h6" gutterBottom>
                  Variables
                </Typography>
                <List>
                  {(selectedClass.variableList ?? []).map((variable, index) => (
                    <ListItem key={index}>
                      <Box
                        sx={{
                          display: "flex",
                          flexDirection: "row-reverse",
                          alignItems: "center",
                        }}
                      >
                        <ListItemText
                          primary={
                            <Typography
                              variant="body1"
                              sx={{
                                marginRight: "8px",
                                display: "inline",
                              }}
                            >
                              {variable.datatype ?? "Unknown Type"}
                            </Typography>
                          }
                          secondary={
                            <Typography variant="body1" component="span">
                              {variable.identifier ?? "Unnamed Variable"}
                            </Typography>
                          }
                        />
                      </Box>
                    </ListItem>
                  ))}
                </List>
              </>
            )}

            {selectedClass.methodList?.length > 0 && (
              <>
                <Typography variant="h6" gutterBottom>
                  Methods
                </Typography>
                <List>
                  {(selectedClass.methodList ?? []).map((method, index) => (
                    <ListItem key={index}>
                      <ListItemText
                        primary={`${method.methodName ?? "Unnamed Method"}()`}
                      />
                    </ListItem>
                  ))}
                </List>
              </>
            )}
          </Container>
        ) : (
          // <>
          //   <Typography variant="h4" gutterBottom>
          //     Internal Dependency Graph
          //   </Typography>
          //   <Box
          //     sx={{
          //       bgcolor: "#ebedef",
          //       height: "70vh",
          //       padding: "10px",
          //       marginTop: "50px",
          //     }}
          //   >
          //     <ReactFlow
          //       nodes={nodes}
          //       edges={edges}
          //       onNodesChange={onNodesChange}
          //       onEdgesChange={onEdgesChange}
          //       onConnect={onConnect}
          //       fitView
          //     />
          //   </Box>
          // </>
          <>
            <Typography variant="body1" paragraph>
            <Container maxWidth='xl'>
            <Box sx={{ 
                    bgcolor: '#e8edf1',
                    height: '82vh',
                    width: '100%',
                    maxwidth: '100%',
                    padding: '10px', 
                    boxSizing: 'border-box', // keep the padding inside container
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
            <Box sx={{ 
                  display: 'flex',
                  justifyContent: 'flex-end', 
                  marginTop: '10px'
                }}
            >
              <Filter 
                onFilterChange={handleFilterChange} 
              />
            </Box>
        </Container>
          </Typography> 
          </>
        )
      ) : (
        <>
          <Typography variant="h4" gutterBottom>
            External Dependency Graph
          </Typography>
        </>
      )}
    </div>
  );
};

export default GraphWhiteBoard;
