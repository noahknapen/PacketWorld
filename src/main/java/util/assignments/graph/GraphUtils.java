package util.assignments.graph;

import java.util.*;

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

                Node cellNode;

                // Create a node
                if (cellPerception.containsPacket() || cellPerception.containsEnergyStation() || cellPerception.containsAnyDestination()) {
                    cellNode = new Node(cellCoordinate, false);
                } else {
                    cellNode = new Node(cellCoordinate, true);
                }

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
                        Node neighbourNode;

                        if (cellPerception.containsPacket() || cellPerception.containsEnergyStation() || cellPerception.containsAnyDestination()) {
                            neighbourNode = new Node(neighbourCellCoordinate, false);
                        } else {
                            neighbourNode = new Node(neighbourCellCoordinate, true);
                        }

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

            for(Node edge : updatedGraph.getMap().get(node)) {
                currentGraph.addEdge(node, edge);
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
        Coordinate agentPosition = new Coordinate(agentState.getX(), agentState.getY());

        // Get the graph
        Graph graph = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class);

        // Check if graph is null and raise exception if so
        if(graph == null) return null;

        // Define the start and end node
        Node startNode = new Node(agentPosition, true);
        Node targetNode = new Node(target, true);

        // Define priority queues
        PriorityQueue<Node> openList = new PriorityQueue<>();
        HashMap<Node, Double> openListWithCost = new HashMap<>();
        PriorityQueue<Node> closeList = new PriorityQueue<>();
        HashMap<Node, Double> closeListWithCost = new HashMap<>();

        // Set costs of start node
        startNode.setGCost(0);
        startNode.setHCost(calculateHeuristic(startNode, targetNode));

        // Add start node to open list
        openList.add(startNode);

        // Define a resulting node
        Node result = null;

        // Perform A*
        while(!openList.isEmpty()) {
            Node node = openList.poll();

            if(node.equals(targetNode)) {
                result = node;
                break;
            }

            for(Node neighbourNode: graph.getMap().get(node)) {

                if (!neighbourNode.isWalkable()) continue;

                neighbourNode.setParent(node);
                neighbourNode.setHCost(calculateHeuristic(neighbourNode, targetNode));
                neighbourNode.setGCost(node.getGCost() + 1);

                if (openListWithCost.containsKey(neighbourNode)) {
                    if (neighbourNode.getFCost() > openListWithCost.get(neighbourNode)) continue;
                }

                if (closeListWithCost.containsKey(neighbourNode)) {
                    if (neighbourNode.getFCost() > closeListWithCost.get(neighbourNode)) continue;
                }

                openList.add(neighbourNode);
                openListWithCost.put(neighbourNode, neighbourNode.getFCost());

            }

            closeList.add(node);
            closeListWithCost.put(node, node.getFCost());
        }

        // Ensure that result isn't null
        if (result == null) return agentPosition;

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
        System.out.printf("%s: %s", agentState.getName(), path);
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
