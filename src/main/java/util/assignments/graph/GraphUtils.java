package util.assignments.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.PriorityQueue;

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
     */
    public static void build(AgentState agentState) {
        // Get the perception the agent
        Perception agentPerception = agentState.getPerception();

        // Get the graph
        Graph graph = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class);

        // Check if graph is null and create one if so
        if(graph == null) graph = new Graph();
        
        // Loop over the whole perception to create nodes
        for (int x = 0; x <= agentPerception.getWidth(); x++) {
            for (int y = 0; y <= agentPerception.getHeight(); y++) {
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
                        if(neighbourCellPerception == null) continue;
                        
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

    /**
     * Update the graph based on another one
     * 
     * @param agentState The current state of the agent
     */
    public static void update(AgentState agentState, Graph updatedGraph) {
        // Get the current graph
        Graph currentGraph = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class);

        // Check if current graph is null and create one if so
        if(currentGraph == null) currentGraph = new Graph();
        
        // Loop over the whole updated graph
        for(Node node: updatedGraph.getMap().keySet()) {
            // Get the position of the node
            int nodeX = node.getCoordinate().getX();
            int nodeY = node.getCoordinate().getY();

            // Check if the current graph already contains the node and continue with next node if so
            if(currentGraph.getMap().containsKey(node)) continue;

            // Add the node to the current graph
            currentGraph.addNode(node);

            // Loop over neighbourhood to add edges
            for(int i = -1; i <= 1; i++) {
                for(int j = -1; j <= 1; j++) {
                    // Get the position of the neighbour cell
                    int neighbourCellX = nodeX + i;
                    int neighbourCellY = nodeY + j;
                    Coordinate neighbourCoordinate = new Coordinate(neighbourCellX, neighbourCellY);

                    // Define neighbour node
                    Node neighbourNode = new Node(neighbourCoordinate);

                    // Check if neighbour node is not contained in the graph and continue with next neighbour if so
                    if(!currentGraph.getMap().containsKey(neighbourNode)) continue;

                    // Check if node is equal to neighbour and continue with the next neighbour if so
                    if(node.equals(neighbourNode)) continue;

                    // Add the edges between the cells
                    currentGraph.addEdge(node, neighbourNode);
                }
            }
        }

        // Update the memory
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.GRAPH, currentGraph));
    }

    ////////////
    // SEARCH //
    ////////////

    /**
     * A function to perform A* search, finding a such a path between the agent's current position
     * and the target coordinate
     * 
     * @param agentState The current state of the agent
     * @param target The target position that should be reached
     * @return The coordinate (first of path) to which the agent should move
     */
    public static Coordinate performAStarSearch(AgentState agentState, Coordinate target) {
        // Get the position of the agent
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        Coordinate agentPosition = new Coordinate(agentX, agentY);

        // Get the graph
        Graph graph = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class);

        // Check if graph is null and raise exception if so
        if(graph == null) return null;

        // Define the nodes
        Node startNode = new Node(agentPosition);
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

        // Ensure that result isn't null
        if (result == null) return new Coordinate(agentX, agentY);

        // Calculate the path
        ArrayList<Coordinate> path = new ArrayList<>();
        while(result.getParent() != null) {
            path.add(result.getCoordinate());
            result = result.getParent();

            // Ensure that the result isn't null for the next iteration
            if (result == null) break;
        }

        // Reverse the path
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
