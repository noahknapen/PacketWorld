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

    // The cost for traversing over a packet in a computed path using Astar
    private static final int PACKET_COST = 0;
    
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

                // Check if the cell is not walkable and that it is not because of an agent standing there. If so continue with the next cell
                if(cellPerception.containsWall()) continue;

                // Get the position of the cell
                int cellX = cellPerception.getX();
                int cellY = cellPerception.getY();
                Coordinate cellCoordinate = new Coordinate(cellX, cellY);

                // Create a node
                Target target = extractTarget(cellCoordinate, cellPerception);
                Node cellNode = new Node(cellCoordinate, target);

                // Add the node to the graph or update if target exists
                graph.addNode(cellNode);

                // Check if cellNode is a packet and if its blocked
                if (cellNode.containsTarget()){
                    checkBlockadingPackets(agentState, graph, cellNode);
                }

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

                        // Check if the cell is not walkable and that it is not because of an agent standing there. If so continue with the next cell
                        if(neighbourCellPerception.containsWall()) continue;

                        // Get the position of the neighbour cell
                        Coordinate neighbourCellCoordinate = new Coordinate(neighbourCellX, neighbourCellY);

                        // Create a node
                        Target neighbourTarget = extractTarget(neighbourCellCoordinate, neighbourCellPerception);
                        Node neighbourNode = new Node(neighbourCellCoordinate, neighbourTarget);

                        // Check if node is equal to cell and continue with the next cell if so
                        if(cellNode.equals(neighbourNode)) continue;

                        // Only allow edges between free node,
                        if (!cellNode.containsTarget() && !neighbourNode.containsTarget() && (!cellNode.containsPacket() || !neighbourNode.containsPacket())) continue;

                        // Add the edges between the cells
                        graph.addEdge(cellNode, neighbourNode);
                    }
                }
            }
        }

        // Update the memory
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.GRAPH, graph));
    }

    private static Graph checkBlockadingPackets(AgentState agentState, Graph graph, Node cellNode) {
        ArrayList<Node> path = GraphUtils.performAStarSearch(agentState, cellNode.getCoordinate(), true);

        if (path == null) {
            ArrayList<Node> noPathNodes = MemoryUtils.getListFromMemory(agentState, MemoryKeys.NO_PATH_NODES, Node.class);
            noPathNodes.add(cellNode);
            MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.NO_PATH_NODES, noPathNodes));
            return graph;
        }

        Node firstBlockadingPacket = getFirstPathPacket(path);

        // Check if a packet is blocking the path
        if (firstBlockadingPacket != null) {
            Packet packet = (Packet) firstBlockadingPacket.getTarget();

            // Set packet as prio
            graph.getNode(packet.getCoordinate()).setPrioPacket(true);

            if (agentState.getColor().isPresent() && agentState.getColor().get().getRGB() == packet.getRgbColor()) {

                // I can handle it myself
                ArrayList<Packet> prioPackets = MemoryUtils.getListFromMemory(agentState, MemoryKeys.PRIO_PACKETS, Packet.class);
                if (!prioPackets.contains(packet)) {
                    prioPackets.add(packet);
                    MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.PRIO_PACKETS, prioPackets));
                }
            }
        }

        return graph;
    }

    private static Node getFirstPathPacket(ArrayList<Node> path) {
        for (Node node : path) {
            if (node.containsPacket()) return node;
        }

        return null;
    }

    /**
     * Extracts the target (Packet, Destination etc) if one exists in the cell perception
     * @param coordinate Cell coordinate
     * @param cellPerception The cell perception
     * @return Target (or null if cell does not contain any target)
     */
    private static Target extractTarget(Coordinate coordinate, CellPerception cellPerception) {
        if (cellPerception.containsPacket()) {
            return new Packet(coordinate, Objects.requireNonNull(cellPerception.getRepOfType(PacketRep.class)).getColor().getRGB());
        }

        if (cellPerception.containsAnyDestination()) {
            return new Destination(coordinate, Objects.requireNonNull(cellPerception.getRepOfType(DestinationRep.class)).getColor().getRGB());
        }

        if (cellPerception.containsEnergyStation()) {
            return new ChargingStation(coordinate);
        }

        return null;
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

            // If node exists in graph -> update target if updatedGraph has newer value
            if(currentGraph.getMap().containsKey(node)) {
                Node n = currentGraph.getNode(node.getCoordinate());
                if (node.getUpdateTime() > n.getUpdateTime()) n.setTarget(node.getTarget());
                continue;
            }

            // Add the node to the current graph (this node is new in the current graph)
            currentGraph.addNode(node);

            // Loop over neighbourhood to add edges
            for(int i = -1; i <= 1; i++) {
                for(int j = -1; j <= 1; j++) {
                    // Get the position of the neighbour cell
                    int neighbourCellX = nodeX + i;
                    int neighbourCellY = nodeY + j;
                    Coordinate neighbourCoordinate = new Coordinate(neighbourCellX, neighbourCellY);

                    // Define neighbour node
                    Node neighbourNode = currentGraph.getNode(neighbourCoordinate);

                    // Check if neighbour node is not contained in the graph and continue with next neighbour if so (only connect edges to nodes that is in the current graph)
                    if(neighbourNode == null) continue;

                    // Check if node is equal to neighbour and continue with the next neighbour if so
                    if(node.equals(neighbourNode)) continue;

                    // Only allow edges between free node, free nodes and targets and between packets
                    if (!node.containsTarget() && !neighbourNode.containsTarget() && (!node.containsPacket() || !neighbourNode.containsPacket())) continue;

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
     * @param includePackets True if you should allow packets in path
     * @return The coordinate (first of path) to which the agent should move
     */
    public static ArrayList<Node> performAStarSearch(AgentState agentState, Coordinate target, boolean includePackets) {
        // Get the position of the agent
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        Coordinate agentPosition = new Coordinate(agentX, agentY);

        // Get the graph
        Graph graph = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class);

        // Check if graph is null and raise exception if so
        if(graph == null) return null;

        // Define the nodes
        Node startNode = graph.getNode(agentPosition);
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

            extractNeighbours(graph, node, targetNode, openList, closeList, includePackets);

            openList.remove(node);
            closeList.add(node);
        }

        // Ensure that result isn't null
        if (result == null)
            return null;

        // Calculate the path
        ArrayList<Node> path = new ArrayList<>();
        while(result.getParent() != null) {
            path.add(result);
            result = result.getParent();

            // Ensure that the result isn't null for the next iteration
            if (result == null) break;
        }

        // Reverse the path
        Collections.reverse(path);

        // Return the first element of the path (which defines the next move)
        return path;
    }

    /**
     * Extracts the neighbours around the node and adds them to openList for further evaluation. Used in A* search.
     * @param graph The graph object
     * @param node The node that neighbours should be extracted around
     * @param targetNode The destination node for the search
     * @param openList The list of open nodes (unvisited nodes)
     * @param closeList The list of closed nodes (visited nodes)
     * @param includePackets
     */
    private static void extractNeighbours(Graph graph, Node node, Node targetNode, PriorityQueue<Node> openList, PriorityQueue<Node> closeList, boolean includePackets) {
        for (Node neighbourNode : graph.getMap().get(node)) {

            // Guard clause to check if neighbour is acceptable node
            if (!neighbourNode.containsTarget() && (!includePackets || !neighbourNode.containsPacket())) continue;

            // Convert boolean to int
            int containsPacketInt = neighbourNode.containsPacket() ? 1: 0;

            double totalGCost = node.getGCost() + 1 + containsPacketInt * PACKET_COST;

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