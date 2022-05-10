package util.assignments.graph;

import java.util.*;

import agent.AgentState;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;
import environment.world.destination.DestinationRep;
import environment.world.packet.PacketRep;
import util.assignments.general.GeneralUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.ChargingStation;
import util.assignments.targets.Destination;
import util.assignments.targets.Packet;
import util.assignments.targets.Target;

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
        if(graph == null) 
            graph = new Graph();

        // Create the old graph string
        // It is used to check later if the graph was updated
        String oldGraphString = graph.toString();
        
        // Loop over the whole perception to create nodes
        for (int x = 0; x <= agentPerception.getWidth(); x++) {
            for (int y = 0; y <= agentPerception.getHeight(); y++) {
                // Get the perception of the cell
                CellPerception cellPerception = agentPerception.getCellAt(x,y);

                // Check if the cell is null and continue with the next cell if so
                if(cellPerception == null) continue;

                // Check if the cell is not walkable and continue with next cell if so
                // Check if the cell contains a wall or a glass wall
                if(cellPerception.containsWall() || cellPerception.containsGlassWall()) continue;

                // Get the coordinate of the cell
                int cellX = cellPerception.getX();
                int cellY = cellPerception.getY();
                Coordinate cellCoordinate = new Coordinate(cellX, cellY);

                // Create a node
                Optional<Target> target = GraphUtils.extractTarget(cellPerception);
                Node node = new Node(cellCoordinate, target);

                // Add the node to the graph
                graph.addNode(node);

                // Loop over neighbourhood to add edges
                for(int i = -1; i <= 1; i++) {
                    for(int j = -1; j <= 1; j++) {
                        // Check if the neighbour cell equals the cell and continue with the next cell if so
                        // Check if both i and j are 0
                        if(i == 0 && j == 0) continue;

                        // Get the position of the neighbour cell
                        int neighbourCellX = cellX + i;
                        int neighbourCellY = cellY + j;

                        // Get the perception of the neighbour cell
                        CellPerception neighbourCellPerception = agentPerception.getCellPerceptionOnAbsPos(neighbourCellX, neighbourCellY);

                        // Check if the neighbour cell is null and continue with the next cell if so
                        if(neighbourCellPerception == null) continue;

                        // Check if the neighbour cell is not walkable and continue with next cell if so
                        // Check if the neighbour cell contains a wall or a glass wall
                        if(neighbourCellPerception.containsWall() || neighbourCellPerception.containsGlassWall()) continue;

                        // Get the coordinate of the neighbour cell
                        Coordinate neighbourCellCoordinate = new Coordinate(neighbourCellX, neighbourCellY);

                        // Create a neighbour node
                        Optional<Target> neighbourTarget = GraphUtils.extractTarget(neighbourCellPerception);
                        Node neighbourNode = new Node(neighbourCellCoordinate, neighbourTarget);

                        // Check if both the node and the neighbour node are not walkable and continue with the next cell if so
                        // Only allow edges between free node
                        if (!node.isWalkable() && !neighbourNode.isWalkable()) continue;

                        // Add the edge between the nodes
                        graph.addEdge(node, neighbourNode);
                    }
                }
            }
        }

        // Update the memory
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.GRAPH, graph));

        // Check if the graph string does not equal the old graph string
        // It checks if the graph was updated
        if(!graph.toString().equals(oldGraphString))
            // Update the memory
            MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.UPDATED_GRAPH, true));
    }

    /**
     * Update the graph based on another one
     * 
     * @param agentState The current state of the agent
     * @param updatedGraph The other graph
     */
    public static void update(AgentState agentState, Graph updatedGraph) {
        // Get the current graph
        Graph currentGraph = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class);

        // Check if current graph is null and create one if so
        if(currentGraph == null) 
            currentGraph = new Graph();
        
        // Loop over the whole updated graph
        for(Node updateNode: updatedGraph.getMap().keySet()) {
            // Get the position of the update node
            int updateNodeX = updateNode.getCoordinate().getX();
            int updateNodeY = updateNode.getCoordinate().getY();

            // Add the node to the graph
            currentGraph.addNode(updateNode);

            // Loop over neighbourhood to add edges
            for(int i = -1; i <= 1; i++) {
                for(int j = -1; j <= 1; j++) {
                    // Check if the neighbour cell equals the cell and continue with the next cell if so
                    // Check if both i and j are 0
                    if(i == 0 && j == 0) continue;

                    // Get the coordinate of the neighbour cell
                    int neighbourCellX = updateNodeX + i;
                    int neighbourCellY = updateNodeY + j;
                    Coordinate neighbourCellCoordinate = new Coordinate(neighbourCellX, neighbourCellY);

                    // Get the optional node from the graph
                    Optional<Node> neighbourNode = currentGraph.getNode(neighbourCellCoordinate);
                    
                    // Check if the neighbour node is empty and continue with the next cell if so
                    if(neighbourNode.isEmpty()) continue;

                    // Check if both the update node and the neighbour node are not walkable and continue with the next cell if so
                    // Only allow edges between free node
                    if (!updateNode.isWalkable() && !neighbourNode.get().isWalkable()) continue;

                    // Add the edge between the nodes
                    currentGraph.addEdge(updateNode, neighbourNode.get());
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
     * A function to perform A* search, finding as such a path between the agent's current position
     * and the target coordinate
     * 
     * @param agentState The current state of the agent
     * @param targetCoordinate The coordinate of the target that should be reached
     * @return The coordinate (first of path) to which the agent should move if it exists, otherwise empty
     */
    public static Optional<Coordinate> performAStarSearch(AgentState agentState, Coordinate targetCoordinate) {
        // Get the coordinate of the agent
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        Coordinate agentCoordinate = new Coordinate(agentX, agentY);

        // Get the graph
        Graph graph = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class);

        // Check if graph is null and return empty if so
        if(graph == null)
            return Optional.empty();

        // Create the start and end node
        Node startNode = new Node(agentCoordinate);
        Node targetNode = new Node(targetCoordinate);

        // Set the costs of the start node
        startNode.setGCost(0);
        startNode.setHCost(GraphUtils.calculateHeuristic(startNode, targetNode));

        // Create the open and close list
        PriorityQueue<Node> openList = new PriorityQueue<>();
        PriorityQueue<Node> closeList = new PriorityQueue<>();

        // Add the start node to the open list
        openList.add(startNode);

        // Create a result node
        Node resultNode = null;

        // Perform A*
        while(!openList.isEmpty()) {
            Node node = openList.peek();
            Coordinate nodeCoordinate = node.getCoordinate();

            if(node.equals(targetNode)) {
                resultNode = node;

                break;
            }

            Optional<Node> graphNode = graph.getNode(nodeCoordinate);
            if(graphNode.isPresent()) {
                if(graphNode.get().isWalkable()) {
                    GraphUtils.extractNeighbours(graph, node, targetNode, openList, closeList);
                }
            }

            openList.remove(node);
            closeList.add(node);
        }

        // Check if the result node is null and return empty if so
        if (resultNode == null)
            return Optional.empty();

        // Calculate the path
        ArrayList<Coordinate> path = new ArrayList<>();
        while(resultNode.getParent() != null) {
            path.add(resultNode.getCoordinate());
            resultNode = resultNode.getParent();

            // Check if the result node is null and break if so
            if (resultNode == null) break;
        }

        // Reverse the path
        Collections.reverse(path);

        // Return the first element of the path (which defines the next move)
        return Optional.of(path.get(0));
    }

    ///////////
    // UTILS //
    ///////////

    /**
     * Extract an optional target from the cell perception
     * 
     * @param cellPerception The perception of the cell
     * @return A target if one exists, otherwise empty
     */
    private static Optional<Target> extractTarget(CellPerception cellPerception) {
        // Get the coordinate of the target
        Coordinate targetCoordinate = new Coordinate(cellPerception.getX(), cellPerception.getY());

        // Check if the cell contains a packet
        if (cellPerception.containsPacket()) {
            return Optional.of(new Packet(targetCoordinate, Objects.requireNonNull(cellPerception.getRepOfType(PacketRep.class)).getColor().getRGB()));
        }
        // Check if the cell contains any destination
        if (cellPerception.containsAnyDestination()) {
            return Optional.of(new Destination(targetCoordinate, Objects.requireNonNull(cellPerception.getRepOfType(DestinationRep.class)).getColor().getRGB()));
        }
        // Check if the cell contains a charging station
        if (cellPerception.containsEnergyStation()) {
            return Optional.of(new ChargingStation(targetCoordinate));
        }

        return Optional.empty();
    }

    /**
     * Extract the neighbours around the node and adds them to openList for further evaluation
     * It is used in A* search.
     * 
     * @param graph The graph
     * @param node The node that neighbours should be extracted around
     * @param targetNode The destination node for the search
     * @param openList The list of open nodes (unvisited nodes)
     * @param closeList The list of closed nodes (visited nodes)
     */
    private static void extractNeighbours(Graph graph, Node node, Node targetNode, PriorityQueue<Node> openList, PriorityQueue<Node> closeList) {
        for (Node neighbourNode : graph.getMap().get(node)) {
            double totalGCost = node.getGCost() + 1;

            if (!openList.contains(neighbourNode) && !closeList.contains(neighbourNode)) {
                neighbourNode.setParent(node);
                neighbourNode.setGCost(totalGCost);
                neighbourNode.setHCost(calculateHeuristic(neighbourNode, targetNode));

                openList.add(neighbourNode);
            } else {
                if (totalGCost < neighbourNode.getGCost()) {
                    neighbourNode.setParent(node);
                    neighbourNode.setGCost(totalGCost);
                    neighbourNode.setHCost(calculateHeuristic(neighbourNode, targetNode));

                    if (closeList.contains(neighbourNode)) {
                        closeList.remove(neighbourNode);
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
     * @param node The node for which the heuristic value should be calculated
     * @return The heuristic value of a node
     */
    private static double calculateHeuristic(Node reference, Node node) {
        // Get the coordinate of the reference and the node
        Coordinate referenceCoordinate = reference.getCoordinate();
        Coordinate nodeCoordinate = node.getCoordinate();

        // Calculate and return the euclidean distance
        return GeneralUtils.calculateEuclideanDistance(referenceCoordinate, nodeCoordinate);
    }
}