package util.assignments.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;

import agent.AgentState;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;
import util.assignments.general.GeneralUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.Target;

/**
 * A class that implements functions regarding the graph
 */
public class GraphUtils {

    // A data member holding the cost for traversing over a packet in a computed path using A*
    private static final int PACKET_COST = 100;
    
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

        // Check if the graph is null
        if(graph == null) {
            // Create a graph
            graph = new Graph();
        }

        // Create a list of new nodes
        ArrayList<Node> newNodes = new ArrayList<>();
        
        // Loop over the perception
        for (int x = 0; x <= agentPerception.getWidth(); x++) {
            for (int y = 0; y <= agentPerception.getHeight(); y++) {
                // Get the perception of the cell
                CellPerception cellPerception = agentPerception.getCellAt(x, y);

                // Check if the cell is null
                if (cellPerception == null) {
                    continue;
                }

                // Check if the cell contains a wall or a glass wall
                if (cellPerception.containsWall() || cellPerception.containsGlassWall()) {
                    continue;
                }

                // Get the coordinate of the cell
                int cellX = cellPerception.getX();
                int cellY = cellPerception.getY();
                Coordinate cellCoordinate = new Coordinate(cellX, cellY);

                // Get the optional graph node
                Optional<Node> graphNode = graph.getNode(cellCoordinate);

                // Get the optional target
                Optional<Target> target = GeneralUtils.extractTarget(cellPerception);

                // Check if the graph node is present
                if(graphNode.isPresent()) {
                    // Set the optional target
                    graphNode.get().setTarget(target);
                    graphNode.get().setUpdateTime(System.currentTimeMillis());
                }
                else {
                    // Create a node
                    Node cellNode = new Node(cellCoordinate, target);

                    // Add the node to the graph
                    graph.addNode(cellNode);

                    // Add the node to the list of new nodes
                    newNodes.add(cellNode);
                }

                // Check if the cell contains a charging station
                if (cellPerception.containsEnergyStation())
                    // Add a charging station
                    GeneralUtils.discoverChargingStation(agentState, cellCoordinate);
            }
        }

        // Loop over the new nodes
        for(Node newNode: newNodes) {
            // Loop over the neighbourhood
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    // Check if i = 0 and j = 0
                    if(i == 0 && j == 0) {
                        continue;
                    }

                    // Get the coordinate of the neighbour cell
                    int neighbourCellX = newNode.getCoordinate().getX() + i;
                    int neighbourCellY = newNode.getCoordinate().getY() + j;
                    Coordinate neighCoordinate = new Coordinate(neighbourCellX, neighbourCellY);

                    // Get the optional graph node
                    Optional<Node> graphNode = graph.getNode(neighCoordinate);

                    // Check if the graph node is empty
                    if(graphNode.isEmpty()) {
                        continue;
                    }

                    // Add an edge between the new node and the graph node (neighbour node)
                    graph.addEdge(newNode, graphNode.get());
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

        // Check if the current graph is null
        if(currentGraph == null) {
            // Create a graph
            currentGraph = new Graph();
        }

        // Create a list of new nodes
        ArrayList<Node> newNodes = new ArrayList<>();

        // Loop over the updated graph
        for(Node updatedNode: updatedGraph.getMap().keySet()) {
            // Get the optional graph node
            Optional<Node> graphNode = currentGraph.getNode(updatedNode.getCoordinate());

            // Check if the graph node is present
            if(graphNode.isPresent()) {
                // Check if the update time of the updated node is larger than the update time of the graph node
                if(updatedNode.getUpdateTime() > graphNode.get().getUpdateTime()) {
                    // Set the target
                    graphNode.get().setTarget(updatedNode.getTarget());
                }

                continue;
            }


            // Add the updated node to the current graph
            currentGraph.addNode(updatedNode);

            // Add the updated node to the list of new nodes
            newNodes.add(updatedNode);
        }

        // Loop over the new nodes
        for(Node newNode: newNodes) {
            // Loop over the neighbourhood
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    // Check if i = 0 and j = 0
                    if(i == 0 && j == 0) {
                        continue;
                    }

                    // Get the coordinate of the neighbour cell
                    int neighbourCellX = newNode.getCoordinate().getX() + i;
                    int neighbourCellY = newNode.getCoordinate().getY() + j;
                    Coordinate neighCoordinate = new Coordinate(neighbourCellX, neighbourCellY);

                    // Get the optional graph node
                    Optional<Node> graphNode = currentGraph.getNode(neighCoordinate);

                    // Check if the graph node is empty
                    if(graphNode.isEmpty()) {
                        continue;
                    }

                    // Add an edge between the new node and the current graph node (neighbour node)
                    currentGraph.addEdge(newNode, graphNode.get());
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
     * Perform A* search with the graph
     * 
     * @param agentState The current state of the agent
     * @param targetCoordinate The target coordinate that should be reached
     * @param includePackets Whether packets are allowed in the path
     * @return The path between agent and the target
     */
    public static ArrayList<Node> performAStarSearch(AgentState agentState, Coordinate targetCoordinate, boolean includePackets) {
        // Get the coordinate of the agent
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        Coordinate agentCoordinate = new Coordinate(agentX, agentY);

        // Get the graph
        Graph graph = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class);

        // Check if graph is null
        if(graph == null) {
            return null;
        }

        // Get the start node and target node
        Node startNode = graph.getNode(agentCoordinate).get();
        Node targetNode = graph.getNode(targetCoordinate).get();

        // Set costs and parent of the start node
        startNode.setGCost(0);
        startNode.setHCost(GraphUtils.calculateHeuristic(startNode, targetNode));
        startNode.setParent(null);

        // Create priority queues
        PriorityQueue<Node> closeList = new PriorityQueue<>();
        PriorityQueue<Node> openList = new PriorityQueue<>();

        // Add the start node to the open list
        openList.add(startNode);

        // Create a result node
        Node resultNode = null;

        // Loop until the open list is empty
        while(!openList.isEmpty()) {
            // Get the first node of the open list
            Node node = openList.peek();

            // Check if the node equals the target node
            if(node.equals(targetNode)) {
                // Define the result node
                resultNode = node;

                break;
            }

            // Check if the node contains a packet and the path may include packets or if the node does not contain a target 
            if((node.containsPacket() && includePackets) || (!node.containsTarget())) {
                // Extract the neighbours of the node
                GraphUtils.extractNeighbours(graph, node, targetNode, openList, closeList, includePackets);
            }

            // Remove the node from the open list
            openList.remove(node);

            // Add the node to the close list
            closeList.add(node);
        }

        // Check if the result node is null
        if(resultNode == null) {
            return null;
        }

        // Create the path
        ArrayList<Node> path = new ArrayList<>();

        // Loop until the result node has no parent
        while(resultNode.getParent() != null) {
            // Add the result node to the path
            path.add(resultNode);

            // Redefine the result node
            resultNode = resultNode.getParent();

            // Check if the result node is null
            if (resultNode == null) {
                break;
            }
        }

        // Reverse the path
        Collections.reverse(path);

        return path;
    }

    ///////////
    // UTILS //
    ///////////

    /**
     * Extract the neighbours around the node and adds them to openList for further evaluation
     * 
     * @param graph The graph
     * @param node The node from which the neighbours should be extracted
     * @param targetNode The target node
     * @param openList The open list
     * @param closeList The clost list
     * @param includePackets Whether packets are allowed in the path
     */
    private static void extractNeighbours(Graph graph, Node node, Node targetNode, PriorityQueue<Node> openList, PriorityQueue<Node> closeList, boolean includePackets) {
        // Get the neighbours nodes
        List<Node> neighboursNode = graph.getMap().get(node);

        // Loop over the neighbours nodes
        for (Node neighbourNode: neighboursNode) {
            // Get if the neighbour node contains a packet as an integer
            int containsPacketInt = neighbourNode.containsPacket()? 1 : 0;

            // Calculate the total path cost
            double totalGCost = node.getGCost() + 1 + containsPacketInt * PACKET_COST;

            // Check if the open list does not contain the neighbourd node and the close list does not contain the neighbour node
            if (!openList.contains(neighbourNode) && !closeList.contains(neighbourNode)) {
                // Set the costs and parent of the neighbour node
                neighbourNode.setGCost(totalGCost);
                neighbourNode.setHCost(GraphUtils.calculateHeuristic(neighbourNode, targetNode));
                neighbourNode.setParent(node);

                // Add the neighbour node to the open list
                openList.add(neighbourNode);
            } else {
                // Check if the total path cost is smaller than the path cost of the neighbour node
                if (totalGCost < neighbourNode.getGCost()) {
                    // Set the costs and parent of the neighbour node
                    neighbourNode.setGCost(totalGCost);
                    neighbourNode.setHCost(calculateHeuristic(neighbourNode, targetNode));
                    neighbourNode.setParent(node);

                    // Check if the close list contains the neigbour node
                    if (closeList.contains(neighbourNode)) {
                        // Remove the neighbour node from the close list
                        closeList.remove(neighbourNode);

                        // Add the neighbour node to the open list
                        openList.add(neighbourNode);
                    }
                }
            }
        }
    }

    /**
     * Calculate the heuristic value of a node with a given reference
     * 
     * @param reference The reference by means of which the heuristic value is calculated
     * @param node The node for which the heuristic value is calculated
     * @return The heuristic value of the node
     */
    private static double calculateHeuristic(Node reference, Node node) {
        // Get the coordinate of the reference node and the coordinate of the node
        Coordinate referenceCoordinate = reference.getCoordinate();
        Coordinate nodeCoordinate = node.getCoordinate();

        // Calculate the Euclidean distance
        return GeneralUtils.calculateEuclideanDistance(referenceCoordinate, nodeCoordinate);
    }

    /**
     * Is the path blocked by some packets?
     * 
     * @param path The path
     * @return True if the path is blocked by packets, otherwise false
     */
    public static boolean isPathBlocked(ArrayList<Node> path) {
        // Check and return if any node in the path contains a packet
        return path.stream().filter(n -> n.containsPacket()).findAny().isPresent();
    }
}