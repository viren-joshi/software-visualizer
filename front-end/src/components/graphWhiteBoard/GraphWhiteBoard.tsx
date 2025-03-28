import {
  Button,
  List,
  ListItem,
  ListItemText,
  Typography,
} from "@mui/material";
import React from "react";
import dagre from "dagre";
import axios from 'axios';
import { useCallback, useEffect } from "react";
import {
  ReactFlow,
  Background,
  Edge,
  Node,
  useNodesState,
  useEdgesState,
  addEdge,
  Connection,
  MarkerType,
  ConnectionMode,
} from "@xyflow/react";
import "@xyflow/react/dist/style.css";
import { Container, Box } from "@mui/material";
import {
  ClassContainer,
  InternalDependency,
  MavenDependency,
} from "../mainpage/Main";
import Filter from "../filter/Filter";
import Tree from "react-d3-tree";

const server_url = process.env.REACT_APP_SERVER_URL;

export interface TreeNode {
  name: string;
  children?: TreeNode[];
}

export interface GraphWhiteBoardProps {
  jsonData: ClassContainer;
  alignment: String;
  selectedClass: InternalDependency | null; // New prop for selected class
  isCustomView?: boolean;
  setIsCustomView: React.Dispatch<React.SetStateAction<boolean>>;
}

export interface NodeData extends Record<string, unknown> {
  label: string;
}

export interface Position {
  x: number;
  y: number;
}

// implementaiton of dagre in the graph
const dagreGraph = new dagre.graphlib.Graph();
dagreGraph.setDefaultEdgeLabel(() => ({}));

const nodeWidth = 172;
const nodeHeight = 36;

// layouting nodes and edges as per ranking
const getLayoutedElements = (nodes: any, edges: any) => {
  const isHorizontal = false;
  dagreGraph.setGraph({
    rankdir: isHorizontal ? "LR" : "TB",
    nodesep: 200,
    ranksep: 150,
  });

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
  selectedClass,
  isCustomView,
  setIsCustomView,
}) => {
  const initialNodes: Node[] = [];
  const initialEdges: Edge[] = [];
  const [nodes, setNodes, onNodesChange] = useNodesState(initialNodes);
  const [edges, setEdges, onEdgesChange] = useEdgesState(initialEdges);

  const onConnect = useCallback((params: Connection) => {
    // Prompt the user for a label
    const label = window.prompt("Enter a label for this edge:", "");
    // Add the new edge with the label
    const newEdge = {
      ...params,
      label: label || "", // Use the provided label or an empty string if none
      markerEnd: { type: MarkerType.Arrow },
    };
    setEdges((eds) => addEdge(newEdge, eds));
  }, []);

  // setting up nodes list
  useEffect(() => {
    if (isCustomView) {
      // setNodes([]);
      setEdges([]);
      return;
    }
    const nodesList: Node[] = [];
    const edgesList: Edge[] = [];

    // creating nodes and edges from the jsonData
    jsonData.internalDependencyList.forEach((classItem) => {
      nodesList.push({
        id: classItem.name,
        type: "default",
        data: { label: classItem.name.split(".").pop()! },
        position: { x: 0, y: 0 }, // temporary position, will be set by dagre layout
      });

      // creating inheritance edges
      if (classItem.inherits) {
        edgesList.push({
          id: `e-${classItem.name}-${classItem.inherits}`,
          source: classItem.name,
          target: classItem.inherits,
          label: "inheritance",
          markerEnd: {
            type: MarkerType.Arrow,
          },
        });
      }

      // creating implementation edges
      if (classItem.implementationList.length > 0) {
        classItem.implementationList.forEach((implementation) => {
          // Validate that the implementation target exists in the jsonData
          const targetClass = jsonData.internalDependencyList.find(
            (dep) => dep.name === implementation
          );

          if (targetClass) {
            edgesList.push({
              id: `e-${classItem.name}-${implementation}`,
              source: classItem.name,
              target: implementation,
              label: "implementation",
              markerEnd: {
                type: MarkerType.Arrow,
              },
            });
          }
        });
      }

      // creating composition edges
      classItem.variableList.forEach((variable) => {
        // Check if the datatype matches any class in the jsonData (composition relationship)
        const targetClass = jsonData.internalDependencyList.find(
          (dep) => dep.name === variable.datatype
        );

        if (targetClass) {
          edgesList.push({
            id: `e-${classItem.name}-${variable.datatype}`,
            source: classItem.name,
            target: variable.datatype,
            label: "composition",
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
  }, [isCustomView, jsonData, setNodes, setEdges]);

  // filter change handler (updating node colors)
  const handleFilterChange = (filter: string) => {
    // user clicks 'none' to remove filters
    if (filter === "nofilter") {
      setNodes((nodes) =>
        nodes.map((node) => ({
          ...node,
          style: { ...node.style, backgroundColor: "white" }, // Reset to default node color
        }))
      );
      setEdges((edges) =>
        edges.map((edge) => ({
          ...edge,
          style: { stroke: "#888", strokeWidth: 1 }, // Reset edge style
          markerEnd: { type: MarkerType.Arrow, color: "#888" }, // Reset arrow style
        }))
      );
      return;
    }

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
        let newColor = "#eee"; //default color

        // set the color for nodes involved
        if (filteredNodeIds.has(node.id)) {
          if (filter == "inheritance") {
            newColor = "#5F9EA0";
          } else if (filter === "implementation") {
            newColor = "#E1C16E";
          } else if (filter === "composition") {
            newColor = "#8A9A5B";
          }
        }

        return {
          ...node,
          style: { ...node.style, backgroundColor: newColor }, // updating node style
        };
      })
    );

    // update edge styles based on the selected filter
    setEdges((edges) =>
      edges.map((edge) => {
        let newEdgeStyle = { stroke: "#ddd", strokeWidth: 1 }; // default edge style for unselected edges
        let arrowStyle = { type: MarkerType.Arrow, color: "#888" }; // default arrow style

        // apply specific styles for edges matching the selected filter
        if (edge.label === filter) {
          if (filter === "inheritance") {
            newEdgeStyle = { stroke: "#5F9EA0", strokeWidth: 2 };
            arrowStyle = { type: MarkerType.Arrow, color: "#5F9EA0" };
          } else if (filter === "implementation") {
            newEdgeStyle = { stroke: "#E1C16E", strokeWidth: 2 };
            arrowStyle = { type: MarkerType.Arrow, color: "#E1C16E" };
          } else if (filter === "composition") {
            newEdgeStyle = { stroke: "#8A9A5B", strokeWidth: 2 };
            arrowStyle = { type: MarkerType.Arrow, color: "#8A9A5B" };
          }
        }
        return {
          ...edge,
          style: { ...edge.style, ...newEdgeStyle }, // updating edge style
          markerEnd: arrowStyle,
        };
      })
    );
  };
  const processTreeData = (dependencies: MavenDependency[]): TreeNode => {
    const treeData: TreeNode = {
      name: "Dependencies",
      children: dependencies.map((dependency) => ({
        name: dependency.artifactId,
      })),
    };

    return treeData;
  };

  const toggleCustomView = () => {
    setIsCustomView(!isCustomView);

    console.log(); // Toggle custom view
  };

  const saveGraph = () => {
    const graph = {
      nodes: nodes.map((node) => ({
        id: node.id,
        data: node.data,
        position: node.position,
      })),
      edges: edges.map((edge) => ({
        id: edge.id.replace(/^xy-edge__/, ""),
        source: edge.source,
        target: edge.target,
        label: edge.label,
        markerEnd: {
          type: MarkerType.Arrow,
        },
      })),
    };
    console.log("Graph data:", graph);
    // Save graph to a database, file, or localStorage if needed
    try {
      let graphData = JSON.stringify({
        nodes: graph.nodes,
        edges: graph.edges,
        projectId: localStorage.getItem("current-projectId"),
      });
      let config = {
        method: "post",
        url: `${server_url}/initialize/createCustomView`,
        headers: {
          "Authorization": localStorage.getItem("soft-viz-tokenID"),
          "Content-Type": "application/json",
        },
        data: graphData,
      }

      axios.request(config).then((response) => {
        alert("Graph saved successfully.");
      });
      
    } catch (error) {
      console.error("Failed to save the graph:", error);
      alert("Failed to save the graph. Please try again.");
    }

  };
  const treeData = processTreeData(jsonData.externalDependencyList);
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
          <>
            <Container
              sx={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
              }}
            >
              <Typography variant="h5" gutterBottom>
                Internal Dependency Graph
              </Typography>
              <Typography variant="h5" gutterBottom>
                <Button variant="contained" onClick={toggleCustomView}>
                  {isCustomView ? "Exit Custom View" : "Custom View"}
                </Button>
                {isCustomView && (
                  <Button
                    variant="contained"
                    onClick={saveGraph}
                    sx={{ marginLeft: "20px" }}
                  >
                    Save View
                  </Button>
                )}
              </Typography>
            </Container>
            <Typography variant="body1" paragraph>
              <Container maxWidth="xl">
                <Box
                  sx={{
                    bgcolor: "#e8edf1",
                    height: "82vh",
                    width: "100%",
                    maxwidth: "100%",
                    padding: "10px",
                    boxSizing: "border-box", // keep the padding inside container
                  }}
                >
                  <ReactFlow
                    nodes={nodes}
                    edges={edges}
                    defaultEdgeOptions={{
                      style: {
                        stroke: "#888",
                        strokeWidth: 1,
                      },
                      markerEnd: {
                        type: MarkerType.Arrow,
                        color: "#888",
                      },
                    }}
                    onNodesChange={onNodesChange}
                    onEdgesChange={onEdgesChange}
                    onConnect={onConnect}
                    fitView
                    snapToGrid={true}
                    attributionPosition="top-right"
                    connectionMode={ConnectionMode.Loose}
                  ></ReactFlow>
                </Box>
                <Box
                  sx={{
                    display: "flex",
                    justifyContent: "flex-end",
                    marginTop: "10px",
                  }}
                >
                  <Filter onFilterChange={handleFilterChange} />
                </Box>
              </Container>
            </Typography>
          </>
        )
      ) : (
        <>
          <Typography variant="h5" gutterBottom>
            External Dependency Graph
          </Typography>
          <Container maxWidth={false} style={{ height: "100vh", padding: 0 }}>
            <Box
              sx={{
                bgcolor: "#e8edf1",
                height: "82vh",
                width: "100%",
                maxWidth: "100%",
                padding: "10px",
                boxSizing: "border-box",
              }}
            >
              <Tree
                data={treeData}
                orientation="vertical"
                translate={{
                  x: window.innerWidth / 2,
                  y: window.innerHeight / 2,
                }}
                pathFunc="diagonal"
                nodeSize={{ x: 300, y: 200 }}
                separation={{ siblings: 0.5, nonSiblings: 1.0 }}
                zoomable // Enables zoom and pan
                scaleExtent={{ min: 0.5, max: 2 }} // Set zoom levels
                renderCustomNodeElement={(rd3tProps) => (
                  <g>
                    <circle
                      r={10}
                      fill="purple"
                      stroke="black"
                      strokeWidth={1}
                    />
                    <text
                      dy={
                        rd3tProps.nodeDatum.name === "Dependencies" ? -15 : 20
                      }
                      fontSize={12}
                      textAnchor="middle"
                      style={{ fill: "black" }}
                    >
                      {rd3tProps.nodeDatum.name}
                    </text>
                  </g>
                )}
                pathClassFunc={() => "link"}
              />
            </Box>
          </Container>
        </>
      )}
    </div>
  );
};

export default GraphWhiteBoard;
