package util.assignments.graph;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.PriorityQueue;

import agent.behavior.assignment_1_B.utils.NodeType;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import agent.AgentState;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;
import util.assignments.general.GeneralUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;

/**
 * A class that implements functions regarding the graph
 */
public class GraphUtils {
    
    ///////////
    // BUILD //
    ///////////

    /**
     * Build the graph based on the perception of the agent
     * 
     * @param agentState The current state of the agent
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    public static void build(AgentState agentState) throws JsonParseException, JsonMappingException, IOException {
        // Get the perception the agent
        Perception agentPerception = agentState.getPerception();

        // Get the graph
        Graph graph = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class);

        // Check if graph is null and create one if so
        if(graph == null)
            graph = new Graph();
        
        // Loop over the whole perception to create nodes
        for (int x = 0; x < agentPerception.getWidth(); x++) {
            for (int y = 0; y < agentPerception.getHeight(); y++) {
                CellPerception cellPerception = agentPerception.getCellAt(x,y);

                // Check if the cell is null and continue with the next cell if so
                if(cellPerception == null) continue;

                // Check if the cell contains no packet nor destination and is not walkable and continue with the next cell if so
                if(!(cellPerception.containsPacket() || cellPerception.containsAnyDestination()) && !cellPerception.isWalkable()) continue;
        
                // Get the position of the cell
                int cellX = cellPerception.getX();
                int cellY = cellPerception.getY();
                Coordinate cellCoordinate = new Coordinate(cellX, cellY);

                // Create a node
                Node cellNode = new Node(cellCoordinate);

                // Add the node to the graph
                graph.addNode(cellNode);

                // Loop over neighbourhood to add edges
                for(int i = -1; i <= 1; i++) {
                    for(int j = -1; j <= 1; j++) {
                        // Get the position of the neighbour cell
                        int neighbourCellX = cellX + i;
                        int neighbourCellY = cellY + j;

                        // Get the corresponding neighbour cell
                        CellPerception neighbourCellPerception = agentPerception.getCellPerceptionOnAbsPos(neighbourCellX, neighbourCellY);

                        // Check if the neighbour cell is null or not walkable and continue with the next cell if so
                        if(neighbourCellPerception == null || !neighbourCellPerception.isWalkable()) continue;
                        
                        // Check if the neighbour cell contains no packet nor destination and is not walkable and continue with the next cell if so
                        if(!(neighbourCellPerception.containsPacket() || neighbourCellPerception.containsAnyDestination()) && !neighbourCellPerception.isWalkable()) continue;

                        // Get the position of the neighbour cell
                        Coordinate neighbourCellCoordinate = new Coordinate(neighbourCellX, neighbourCellY);

                        // Create a node
                        Node neighbourNode = new Node(neighbourCellCoordinate);

                        // Check if node is equal to cell and continue with the next cell if so
                        if(cellNode.equals(neighbourNode)) continue;

                        // Add the edges between the cells
                        graph.addEdge(cellNode, neighbourNode);
                    }
                }
            }
        }

        // Update the memory
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.GRAPH, graph));
    }

    ////////////
    // SEARCH //
    ////////////

    public static Coordinate performAStarSearch(AgentState agentState, Coordinate target) throws JsonParseException, JsonMappingException, IOException {
        // Get the position of the agent
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        Coordinate agentCoordinate = new Coordinate(agentX, agentY);

        // Get the graph
        Graph graph = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class);

        // Check if graph is null and raise exception if so
        if(graph == null) throw new IllegalArgumentException("No graph to perform A* search on");
        
        // Define the nodes
        Node startNode = new Node(agentCoordinate);
        Node targetNode = new Node(target);

        // Define priority queues
        PriorityQueue<Node> closeList = new PriorityQueue<>();
        PriorityQueue<Node> openList = new PriorityQueue<>();

        // Set costs of start node
        startNode.setGCost(0);
        startNode.setHCost(calculateHeuristic(startNode, targetNode));

        // Add start node to open list
        openList.add(startNode);

        // Define a resulting node
        Node result = null;

        // Perform A*
        while(!openList.isEmpty()) {
            Node node = openList.peek();

            if(node.equals(targetNode)) {
                result = node;
                break;
            }   

            for(Node neighbourNode: graph.getMap().get(node)) {
                double totalGCost = node.getGCost() + 1;

                if(!openList.contains(neighbourNode) && !closeList.contains(neighbourNode)){
                    neighbourNode.setParent(node);
                    neighbourNode.setGCost(totalGCost);
                    neighbourNode.setHCost(calculateHeuristic(neighbourNode, targetNode));

                    openList.add(neighbourNode);
                } else {
                    if(totalGCost < neighbourNode.getGCost()){
                        neighbourNode.setParent(node);
                        neighbourNode.setGCost(totalGCost);
                        neighbourNode.setHCost(calculateHeuristic(neighbourNode, targetNode));
    
                        if(closeList.contains(neighbourNode)){
                            closeList.remove(neighbourNode);
                            openList.add(neighbourNode);
                        }
                    }
                }
            }

            openList.remove(node);
            closeList.add(node);
        }

        // Calculate the path
        ArrayList<Coordinate> path = new ArrayList<>();
        while(result.getParent() != null) {
            path.add(result.getCoordinate());
            result = result.getParent();
        }
        Collections.reverse(path);

        // Return the first element of the path (which defines the next move)
        return path.get(0);
    }

    /**
     * A function to calculate the heuristic value of a node with a given reference
     * 
     * @param reference The reference by means of which the heuristic value is calculated
     * @param node The node for which the heuristic value should be calculated
     * @return The heuristic value of a node
     */
    private static double calculateHeuristic(Node reference, Node node) {
        Coordinate referenceCoordinate = reference.getCoordinate();
        Coordinate nodeCoordinate = node.getCoordinate();

        return GeneralUtils.calculateEuclideanDistance(referenceCoordinate, nodeCoordinate);
    }

}
