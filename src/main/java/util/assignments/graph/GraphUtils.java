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
import util.assignments.task.Task;

/**
 * A class that implements functions regarding the graph
 */
public class GraphUtils {

    // The cost for traversing over a packet in a computed path using Astar
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
        ArrayList<Node> newNodes = new ArrayList<>();

        // Check if graph is null and create one if so
        if(graph == null) graph = new Graph();
        
        // Loop over the whole perception to create nodes
        for (int x = 0; x <= agentPerception.getWidth(); x++) {
            for (int y = 0; y <= agentPerception.getHeight(); y++) {
                CellPerception cellPerception = agentPerception.getCellAt(x, y);

                // Check if the cell is null and continue with the next cell if so
                if (cellPerception == null) continue;

                // Check if the cell is not walkable and that it is not because of an agent standing there. If so continue with the next cell
                if (cellPerception.containsWall()) continue;

                // Get the position of the cell
                int cellX = cellPerception.getX();
                int cellY = cellPerception.getY();
                Coordinate cellCoordinate = new Coordinate(cellX, cellY);

                // Create a node
                Optional<Target> target = extractTarget(cellPerception);
                Optional<Node> cellNode = graph.getNode(cellCoordinate);

                // If node exists -> update target
                // timeUpdate = true since target comes from perception
                if (cellNode.isPresent()) {
                    cellNode.get().setTarget(target);
                    cellNode.get().setUpdateTime(System.currentTimeMillis());
                } else {
                    // Add the node to the graph
                    cellNode = Optional.of(new Node(cellCoordinate, target));
                    graph.addNode(cellNode.get());
                    newNodes.add(cellNode.get());
                }

                // Check if the cell is the one the agent is currently standing on and continue with the next cell if so
                if (x == 0 && y == 0) continue;

                // Check if the cell contains a charging station
                if (cellPerception.containsEnergyStation())
                    GeneralUtils.addChargingStation(agentState, cellCoordinate);
            }
        }

        for (Node node : newNodes) {

            // Loop over neighbourhood to add edges
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    // Get the position of the neighbour cell
                    int neighbourCellX = node.getCoordinate().getX() + i;
                    int neighbourCellY = node.getCoordinate().getY() + j;

                    // Get the corresponding neighbour cell
                    CellPerception neighbourCellPerception = agentPerception.getCellPerceptionOnAbsPos(neighbourCellX, neighbourCellY);

                    // Check if the neighbour cell is null or not walkable and continue with the next cell if so
                    if (neighbourCellPerception == null) continue;

                    // Check if the cell is not walkable and that it is not because of an agent standing there. If so continue with the next cell
                    if (neighbourCellPerception.containsWall()) continue;

                    // Get the position of the neighbour cell
                    Coordinate neighbourCellCoordinate = new Coordinate(neighbourCellX, neighbourCellY);

                    // Create a node
                    Optional<Target> neighbourTarget = extractTarget(neighbourCellPerception);
                    Node neighbourNode = new Node(neighbourCellCoordinate, neighbourTarget);

                    // Check if node is equal to cell and continue with the next cell if so
                    if (node.equals(neighbourNode)) continue;

                    // Only allow edges between free node,
                    //if (node.containsTarget() && neighbourNode.containsTarget() && !node.containsPacket() && !neighbourNode.containsPacket())
                    //    continue;

                    // Add the edges between the cells
                    graph.addEdge(node, neighbourNode);
                }
            }
        }

        // Update the memory
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.GRAPH, graph));
    }

    /**
     * Checks if there are packets along the path to the goal.
     * Creates priority tasks if that is the case
     * @param agentState The agent state
     * @param path The path to be checked
     */
    public static boolean checkIfBlocked(AgentState agentState, ArrayList<Node> path) {

        // Agent currently does not know a possible path to the node
        if (path == null) return false;

        ArrayList<Node> pathPackets = getPathPackets(path);

        // If packets exists along the path
        if (!pathPackets.isEmpty()) {
            GraphUtils.createPriorityTasks(agentState, pathPackets);
            return true;
        }

        return false;
    }

    /**
     * Creates a task for each packet in the pathPackets list
     * Decides if the agent can handle the task or if the task should be shared to other agents through communication
     * @param agentState The agent state
     * @param pathPackets A list of packets to be used for creating tasks
     */
    private static void createPriorityTasks(AgentState agentState, ArrayList<Node> pathPackets) {
        ArrayList<Packet> taskConditions = new ArrayList<>();
        ArrayList<Task> priorityTasks = MemoryUtils.getListFromMemory(agentState, MemoryKeys.PRIORITY_TASKS, Task.class);
        ArrayList<Task> priorityTasksSend = MemoryUtils.getListFromMemory(agentState, MemoryKeys.PRIORITY_TASKS_SEND, Task.class);

        for (Node packetNode : pathPackets) {
            Packet packet = (Packet) packetNode.getTarget().get();
            Task task = new Task(packet, null);
            task.setConditions(taskConditions);
            taskConditions.add(packet);

            // Check if agent can not handle the task
            if (!priorityTasksSend.contains(task) && agentState.getColor().isPresent() && agentState.getColor().get().getRGB() != packet.getRgbColor()){
                priorityTasksSend.add(task);
            }
            else if (!priorityTasks.contains(task) && agentState.getColor().isPresent() && agentState.getColor().get().getRGB() == packet.getRgbColor()){
                priorityTasks.add(task);
            }
        }

        // Update memory
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.PRIORITY_TASKS, priorityTasks, MemoryKeys.PRIORITY_TASKS_SEND, priorityTasksSend));

    }


    /**
     * Finds all packets along a path of nodes
     * @param path The path of nodes
     * @return A list of all packets along the path
     */
    private static ArrayList<Node> getPathPackets(ArrayList<Node> path) {
        ArrayList<Node> packetNodes = new ArrayList<>();

        for (int i = 0; i < path.size() - 1; i++) {
            if (path.get(i).containsPacket()) packetNodes.add(path.get(i));
        }

        return packetNodes;
    }

    /**
     * Extracts the target (Packet, Destination etc) if one exists in the cell perception
     *
     * @param cellPerception The perception of the cell
     * @return A target if one exists, otherwise empty
     */
    private static Optional<Target> extractTarget(CellPerception cellPerception) {
        Coordinate targetCoordinate = new Coordinate(cellPerception.getX(), cellPerception.getY());

        if (cellPerception.containsPacket()) {
            return Optional.of(new Packet(targetCoordinate, Objects.requireNonNull(cellPerception.getRepOfType(PacketRep.class)).getColor().getRGB()));
        }

        if (cellPerception.containsAnyDestination()) {
            return Optional.of(new Destination(targetCoordinate, Objects.requireNonNull(cellPerception.getRepOfType(DestinationRep.class)).getColor().getRGB()));
        }

        if (cellPerception.containsEnergyStation()) {
            return Optional.of(new ChargingStation(targetCoordinate));
        }

        return Optional.empty();
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
            Coordinate nodeCoordinate = new Coordinate(nodeX, nodeY);

            Optional<Node> graphNode = currentGraph.getNode(nodeCoordinate);

            // If node exists in graph -> update target if updatedGraph has newer update time
            // timeUpdate = false because we should only update the time when new value arrives from perception
            if(graphNode.isPresent()) {
                if (node.getUpdateTime() > graphNode.get().getUpdateTime()) {
                    graphNode.get().setTarget(node.getTarget());
                }
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
                    Optional<Node> neighbourNode = currentGraph.getNode(neighbourCoordinate);

                    // Check if neighbour node is not contained in the graph and continue with next neighbour if so (only connect edges to nodes that is in the current graph)
                    if(neighbourNode.isEmpty()) continue;

                    // Check if node is equal to neighbour and continue with the next neighbour if so
                    if(node.equals(neighbourNode.get())) continue;

                    // Add the edges between the cells
                    currentGraph.addEdge(node, neighbourNode.get());
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
    public static ArrayList<Node> performAStarSearchSub(AgentState agentState, Graph graph, Coordinate target, boolean includePackets) {
        // Get the position of the agent
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        Coordinate agentPosition = new Coordinate(agentX, agentY);

        // Check if graph is null and raise exception if so
        if(graph == null) return null;

        // Define the nodes
        Node startNode = graph.getNode(agentPosition).get();
        Node targetNode = new Node(target);

        // Define priority queues
        PriorityQueue<Node> closeList = new PriorityQueue<>();
        PriorityQueue<Node> openList = new PriorityQueue<>();

        // Set costs of start node
        startNode.setGCost(0);
        startNode.setHCost(calculateHeuristic(startNode, targetNode));
        startNode.setParent(null);

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

            // Guard clause to check if neighbour is acceptable node
            if (!node.containsTarget() || (node.containsPacket() && includePackets))
            {
                extractNeighbours(graph, node, targetNode, openList, closeList, includePackets);
            }

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

    public static ArrayList<Node> performAStarSearch(AgentState agentState, Coordinate target, boolean includePackets) {
        // Get the graph
        Graph graph = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class);

        return performAStarSearchSub(agentState, graph, target, includePackets);

    }

    public static ArrayList<Node> performAStarSearch(AgentState agentState, Graph graph, Coordinate target, boolean includePackets) {
       return performAStarSearchSub(agentState, graph, target, includePackets);
    }

    ///////////
    // UTILS //
    ///////////

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

        for (Node neighbour : graph.getMap().get(node)) {

            Node neighbourNode = graph.getNode(neighbour.getCoordinate()).get();

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