package agent.behavior.assignment_1_B;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import agent.behavior.assignment_1_A.utils.Destination;
import agent.behavior.assignment_1_A.utils.Packet;
import agent.behavior.assignment_1_A.utils.Task;
import agent.behavior.assignment_1_B.utils.Graph;
import agent.behavior.assignment_1_B.utils.MemoryKeys;
import agent.behavior.assignment_1_B.utils.NodeType;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;
import environment.world.destination.DestinationRep;
import environment.world.packet.PacketRep;

public class MoveToPacketBehavior extends Behavior {

    private final static ArrayList<Coordinate> RELATIVE_POSITIONS = new ArrayList<Coordinate>(List.of(
        new Coordinate(1, 1), 
        new Coordinate(-1, -1),
        new Coordinate(1, 0), 
        new Coordinate(-1, 0),
        new Coordinate(0, 1), 
        new Coordinate(0, -1),
        new Coordinate(1, -1), 
        new Coordinate(-1, 1)
    ));

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        // TODO Auto-generated method stub
    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        System.out.println("[MoveToPacketBehavior]{act}");

        // Update agents previous position
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        Coordinate agentPosition = new Coordinate(agentX, agentY);

        // Handle graph
        handleGraph(agentState);

        // Check perception
        checkPerception(agentState);

        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();
        // Check if task exists in memory
        if(memoryFragments.contains(MemoryKeys.TASK)) {
            // Retrieve task and position
            String taskString = agentState.getMemoryFragment(MemoryKeys.TASK);
            Task task = Task.fromJson(taskString);
            Coordinate position = task.getPacket().getCoordinate();

            // Move to position
            moveToPosition(agentState, agentAction, position);
        }
        else moveRandom(agentState, agentAction);

        updateMappingMemory(agentState, null, null, agentPosition, null, null);
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Check perception of agent
     *  
     * @param agentState Current state of agent
     */
    private void checkPerception(AgentState agentState) {
        // Retrieve discovered packets, discovered destinations and task
        Perception perception = agentState.getPerception();
        ArrayList<Packet> discoveredPackets = getDiscoveredPackets(agentState);
        ArrayList<Destination> discoveredDestinations = getDiscoveredDestinations(agentState);
        Task task = getTask(agentState);
        Graph graph = getGraph(agentState);

        // Loop over whole perception
        for (int x = 0; x < perception.getWidth(); x++) {
            for (int y = 0; y < perception.getHeight(); y++) {
                CellPerception cell = perception.getCellAt(x,y);

                if(cell == null) continue;

                Coordinate cellCoordinate = new Coordinate(cell.getX(), cell.getY());

                // Check if current cell contains a destination
                if(cell.containsAnyDestination()) {
                    Color destinationColor = cell.getRepOfType(DestinationRep.class).getColor();

                    Destination destination = new Destination(cellCoordinate, destinationColor);

                    // Check if destination was not discoverd yet
                    if(discoveredDestinations.contains(destination)) continue;
                    else {
                        discoveredDestinations.add(destination);

                        System.out.println("[MoveToPacketBehavior]{checkPerception} New destination discovered (" + discoveredDestinations.size() + ")");
                    }

                    // Update graph if unknown destination in cell
                    if(!graph.nodeExists(cell.getX(), cell.getY())) {
                        // If this destination is not already in the graph -> add it
                        addDestinationToGraph(agentState, destination);
                    }
                }
                // Check if current cell contains a packet
                else if(cell.containsPacket()) {
                    Color packetColor = cell.getRepOfType(PacketRep.class).getColor();
                    
                    Packet packet= new Packet(cellCoordinate, packetColor);

                    // Check if packet was not discoverd yet
                    if(discoveredPackets.contains(packet)) continue;
                    // Check if packet is not currently handled (hence should not be added to list again)
                    else if(task != null && task.getPacket().equals(packet)) continue;
                    else {
                        discoveredPackets.add(packet);

                        System.out.println("[MoveToPacketBehavior]{checkPerception} New packet discovered (" + discoveredPackets.size() + ")");
                    }

                    // Add node of agent position that says that agent can see packet from position.
                    if (!graph.nodeExists(cell.getX(), cell.getY())) {
                        addPacketToGraph(agentState, packet);
                    }
                }
            }
        }

        // Update memory
        updateTaskMemory(agentState, discoveredPackets, discoveredDestinations);        
    }

    /**
     * Adds new destination to graph
     * Draws an edge from:
     *     From edgeStartPos -> agentPos
     *     agentPos -> destinationPos
     * 
     * @param agentState Current state of agent
     * @param destination New destination to be added to the graph
     */
    private void addDestinationToGraph(AgentState agentState, Destination destination) {
        Coordinate agentPosition = new Coordinate(agentState.getX(), agentState.getY());
        Graph graph = getGraph(agentState);
        Coordinate edgeStartPosition = getEdgeStartPosition(agentState);

        // If agent position is not in the graph -> Add the position and an edge from edgeStartPos.
        if (!graph.nodeExists(agentPosition)) {
            graph.addNode(agentPosition, NodeType.FREE);
            graph.addEdge(edgeStartPosition, agentPosition);
            edgeStartPosition = agentPosition;
        }

        graph.addNode(destination.getCoordinate(), NodeType.DESTINATION);

        // TODO: Check if path is free from obstacles (It should be but not sure)
        graph.addEdge(agentPosition, destination.getCoordinate(), NodeType.DESTINATION);

        updateMappingMemory(agentState, graph, null, null, edgeStartPosition, null);
    }

    /**
     * Adds new packet to graph
     * Draws an edge from:
     *     From edgeStartPos -> agentPos
     *     agentPos -> packetPos
     * 
     * @param agentState Current state of agent
     * @param packet New packet to be added to the graph
     */
    private void addPacketToGraph(AgentState agentState, Packet packet) {
        // TODO: This and addDestinationToGraph are similar -> Maybe its possible to restructure to reuse the same code.
        Coordinate agentPosition = new Coordinate(agentState.getX(), agentState.getY());
        Graph graph = getGraph(agentState);
        Coordinate edgeStartPosition = getEdgeStartPosition(agentState);

        // If agent position is not in the graph -> Add the position and an edge from edgeStartPos.
        if (!graph.nodeExists(agentPosition)) {
            graph.addNode(agentPosition, NodeType.FREE);
            graph.addEdge(edgeStartPosition, agentPosition);
            edgeStartPosition = agentPosition;
        }

        graph.addNode(packet.getCoordinate(), NodeType.PACKET);

        // TODO: Check if path is free from obstacles (It should be but not sure)
        graph.addEdge(agentPosition, packet.getCoordinate(), NodeType.PACKET);

        updateMappingMemory(agentState, graph, null, null, edgeStartPosition, null);
    }

    /**
     * Dynamically builds the graph
     * Agent saves potential starts of edges in edgeStartPos.
     *
     * If agent goes in a straight line -> agents previous position lies on the line between
     * edgeStartPos and agents current position.
     *
     * If agent turns -> agents previous position DOES NOT lie on the line between
     * edgeStartPos and agents current position
     * -> create new edge between edgeStartPos and agents previous position.
     *
     * @param agentState Current state of agent
     */
    private void handleGraph(AgentState agentState) {
        // Retrieve positions
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        Coordinate agentPosition = new Coordinate(agentX, agentY);

        Graph graph = getGraph(agentState);
        Coordinate previousPosition = getPreviousPosition(agentState);
        Coordinate edgeStartPosition = getEdgeStartPosition(agentState);

        if (!edgeStartPosition.equals(previousPosition) && !previousPosition.equals(agentPosition)) {
            if (!graph.onTheLine(edgeStartPosition, agentPosition, previousPosition)) {
                if (!graph.nodeExists(previousPosition)) graph.addNode(previousPosition, NodeType.FREE);
                graph.addEdge(edgeStartPosition, previousPosition);
                edgeStartPosition = previousPosition;
            }
        }

        // Update mapping memory
        updateMappingMemory(agentState, graph, null, null, edgeStartPosition, null);
    }

    /**
     * Move towards a specific position
     * 
     * @param agentState Current state of agent
     * @param agentAction Perform an action with agent
     * @param position Position to move towards
     */
    private void moveToPosition(AgentState agentState, AgentAction agentAction, Coordinate position) {
        // Retrieve positions
        Perception perception = agentState.getPerception();
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        Coordinate agentPosition = new Coordinate(agentX, agentY);
        int positionX = position.getX();
        int positionY = position.getY();

        // Retrieve path
        List<Coordinate> path = getPath(agentState);

        // Check if position is in current perception
        if(positionInPerception(agentState, position)) {
            // Calculate move
            int dX = positionX - agentX;
            int dY = positionY - agentY;
            int relativePositionX = (dX > 0) ? 1 : ((dX < 0) ? -1 : 0);
            int relativePositionY = (dY > 0) ? 1 : ((dY < 0) ? -1 : 0);
            CellPerception cellPerception = perception.getCellPerceptionOnRelPos(relativePositionX, relativePositionY);

            // Check if cell is walkable
            if (cellPerception != null && cellPerception.isWalkable()) {
                int newPositionX = agentX + relativePositionX;
                int newPositionY = agentY + relativePositionY;

                // Perform a step 
                agentAction.step(newPositionX, newPositionY);

                System.out.println("[MoveToPacketBehavior]{moveToPosition} Agent: (" + agentX + ", " + agentY + ") Position: (" + positionX + ", " + positionY + ")");
            }
            else moveRandom(agentState, agentAction);

            path.clear();
            updateMappingMemory(agentState, null, path, null, null, null);
        }
        else {
            Graph graph = getGraph(agentState);
            Coordinate shouldBeHerePosition = getShouldBeHerePosition(agentState);

            // If path exists -> Just follow the path.
            if (!path.isEmpty()) {

                // If previous movement failed for some reason -> Try again.
                if (!agentPosition.equals(shouldBeHerePosition)) {
                    if (shouldBeHerePosition != null)
                        moveToPosition(agentState, agentAction, shouldBeHerePosition);
                    else
                        moveRandom(agentState, agentAction);
                    return;
                }

                Coordinate nextCoordinate = path.remove(0); // TODO: Maybe path should not be linked list. (Stack?)
                shouldBeHerePosition = nextCoordinate;

                agentAction.step(nextCoordinate.getX(), nextCoordinate.getY());

                updateMappingMemory(agentState, null, path, null, null, shouldBeHerePosition);
            }

            // If agent position outside the graph -> Move to the closest node first.
            else if (!graph.nodeExists(agentPosition))
            {
                Coordinate closestNodeCoordinate = graph.closestFreeNodeCoordinate(agentState.getPerception(), agentPosition);
                if (closestNodeCoordinate != null)
                    moveToPosition(agentState, agentAction, closestNodeCoordinate);
                else
                    moveRandom(agentState, agentAction);
            }

            // Search for path from current position to the desired position.
            else
            {
                // Perform Dijkstra's algorithm
                path = graph.doSearch(agentPosition, position);

                if (!path.isEmpty())
                {
                    Coordinate nextCoordinate = path.remove(0); // TODO: Maybe path should not be linked list. (Stack?)
                    shouldBeHerePosition = nextCoordinate;
                    agentAction.step(nextCoordinate.getX(), nextCoordinate.getY());

                    updateMappingMemory(agentState, null, path, null, null, shouldBeHerePosition);
                }
                else
                {
                    moveRandom(agentState, agentAction);
                }
            }
        }
    }

    /**
     * Move randomly
     * 
     * @param agentState Current state of agent
     * @param agentAction Perform an action with agent
     */
    private void moveRandom(AgentState agentState, AgentAction agentAction) {
        // Retrieve position
        Perception perception = agentState.getPerception();
        int agentX = agentState.getX();
        int agentY = agentState.getY();


        List<Coordinate> positions = new ArrayList<>(MoveToPacketBehavior.RELATIVE_POSITIONS);

        // Prioritize going straight first
        Coordinate previousPosition = getPreviousPosition(agentState);
        int vecX = agentState.getX() - previousPosition.getX();
        int vecY = agentState.getY() - previousPosition.getY();
        int dx = Integer.signum(vecX);
        int dy = Integer.signum(vecY);

        Coordinate inFront = new Coordinate(dx, dy);
        positions.remove(inFront);

        // Shuffle relative positions and add the coordinate for going straight in the front
        Collections.shuffle(positions);
        positions.add(0, inFront);

        // Loop over all relative positions
        for (Coordinate relativePosition : positions) {
            // Calculate move
            int relativePositionX = relativePosition.getX();
            int relativePositionY = relativePosition.getY();
            CellPerception cellPerception = perception.getCellPerceptionOnRelPos(relativePositionX, relativePositionY);

            //Check if cell is walkable
            if (cellPerception != null && cellPerception.isWalkable()) {
                int newPositionX = agentX + relativePositionX;
                int newPositionY = agentY + relativePositionY;
                
                // Perform a step
                agentAction.step(newPositionX, newPositionY);

                System.out.println("[MoveToPacketBehavior]{moveRandom} Random move");
                
                return;
            }
        }

        agentAction.skip();
    }

    /**
     * Check if position is in current perception
     *
     * @param agentState Current state of agent
     * @param position Position to check
     * @return True if position is in current perception
     */
    private boolean positionInPerception(AgentState agentState, Coordinate position) {
        // Retrieve position
        Perception perception = agentState.getPerception();
        int positionX = position.getX();
        int positionY = position.getY();

        // Loop over whole perception
        for (int x = 0; x < perception.getWidth(); x++) {
            for (int y = 0; y < perception.getHeight(); y++) {
                CellPerception cell = perception.getCellAt(x,y);

                if(cell == null) continue;

                int cellX = cell.getX();
                int cellY = cell.getY();

                // Check if coordinates correspond
                if(cellX == positionX && cellY == positionY) return true;
            }
        }

        return false;
    }

    /**
     * Retrieve discovered packets from memory
     * Create list if not yet created
     * 
     * @param agentState Current state of agent
     * @return List of discovered packets
     */
    private ArrayList<Packet> getDiscoveredPackets(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        Gson gson = new Gson();
        // Check if list of discovered packets exists in memory
        if(memoryFragments.contains(MemoryKeys.DISCOVERED_PACKETS)) {
            // Retrieve list of discovered packets 
            String discoveredPacketsString = agentState.getMemoryFragment(MemoryKeys.DISCOVERED_PACKETS);
            return gson.fromJson(discoveredPacketsString, new TypeToken<ArrayList<Packet>>(){}.getType());
        }
        else {
            // Create list of discovered packets
            ArrayList<Packet> discoveredPackets = new ArrayList<Packet>();

            // Add list of discovered packets to memory
            String discoveredPacketsString = gson.toJson(discoveredPackets);
            agentState.addMemoryFragment(MemoryKeys.DISCOVERED_PACKETS, discoveredPacketsString);

            return discoveredPackets;
        }
    }

    /**
     * Retrieve discovered destinations from memory
     * Create list if not yet created
     * 
     * @param agentState Current state of agent
     * @return List of discovered destinations
     */ 
    private ArrayList<Destination> getDiscoveredDestinations(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        Gson gson = new Gson();
        // Check if list of discovered destinations exists in memory
        if(memoryFragments.contains(MemoryKeys.DISCOVERED_DESTINATIONS)) {
            // Retrieve list of discovered destinations 
            String discoveredDestinationsString = agentState.getMemoryFragment(MemoryKeys.DISCOVERED_DESTINATIONS);
            return gson.fromJson(discoveredDestinationsString, new TypeToken<ArrayList<Destination>>(){}.getType());
        }
        else {
            // Create list of discovered destinations
            ArrayList<Destination> discoveredDestinations = new ArrayList<Destination>();

            // Add list of discovered destinations to memory
            String discoveredDestinationsString = gson.toJson(discoveredDestinations);
            agentState.addMemoryFragment(MemoryKeys.DISCOVERED_DESTINATIONS, discoveredDestinationsString);

            return discoveredDestinations;
        }
    }

    /**
     * Retrieve task from memory
     * 
     * @param agentState Current state of agent
     * @return Task
     */
    private Task getTask(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        // Check if task exists in memory
        if(memoryFragments.contains(MemoryKeys.TASK)) {
            // Retrieve task
            String taskString = agentState.getMemoryFragment(MemoryKeys.TASK);
            return Task.fromJson(taskString);
        }
        else return null;
    }

        /**
     * Retrieve graph from memory
     * Create graph if not yet created
     * 
     * @param agentState Current state of agent
     * @return Graph
     */
    private Graph getGraph(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        // Check if graph exists in memory
        if(memoryFragments.contains(MemoryKeys.GRAPH)) {
            // Retrieve graph
            String graphString = agentState.getMemoryFragment(MemoryKeys.GRAPH);
            return Graph.fromJson(graphString);
        }
        else {
            // Create graph
            Graph graph = new Graph(agentState.getX(), agentState.getY());
        
            // Add graph to memory
            String graphString = graph.toJson();
            agentState.addMemoryFragment(MemoryKeys.GRAPH, graphString);

            return graph;
        }
    }

    /**
     * Retrieve path from memory
     * Create path if not yet created
     * 
     * @param agentState Current state of agent
     * @return Path: List of coordinate
     */ 
    private List<Coordinate> getPath(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        Gson gson = new Gson();
        // Check if path exists in memory
        if(memoryFragments.contains(MemoryKeys.PATH)) {
            // Retrieve path
            String pathString = agentState.getMemoryFragment(MemoryKeys.PATH);
            return gson.fromJson(pathString, new TypeToken<List<Coordinate>>(){}.getType());
        }
        else {
            // Create path
            List<Coordinate> path = new LinkedList<>();

            // Add path to memory
            String pathString = gson.toJson(path);
            agentState.addMemoryFragment(MemoryKeys.PATH, pathString);

            return path;
        }
    }

    /**
     * Retrieve previous position from memory
     * Create previous position if not yet created
     * 
     * @param agentState Current state of agent
     * @return Previous position
     */ 
    private Coordinate getPreviousPosition(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        Gson gson = new Gson();
        // Check if previous position exists in memory
        if(memoryFragments.contains(MemoryKeys.PREVIOUS_POSITION)) {
            // Retrieve previous position
            String previousPositionString = agentState.getMemoryFragment(MemoryKeys.PREVIOUS_POSITION);
            return gson.fromJson(previousPositionString, Coordinate.class);
        }
        else {
            // Create previous position
            int agentX = agentState.getX();
            int agentY = agentState.getY();
            Coordinate previousPosition = new Coordinate(agentX, agentY);

            // Add edge start position to memory
            String previousPositionString = gson.toJson(previousPosition);
            agentState.addMemoryFragment(MemoryKeys.PREVIOUS_POSITION, previousPositionString);

            return previousPosition;
        }
    }

    /**
     * Retrieve edge start position from memory
     * Create edge start position if not yet created
     * 
     * @param agentState Current state of agent
     * @return Edge start position
     */ 
    private Coordinate getEdgeStartPosition(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        Gson gson = new Gson();
        // Check if edge start position exists in memory
        if(memoryFragments.contains(MemoryKeys.EDGE_START_POSITION)) {
            // Retrieve edge start position
            String edgeStartPositionString = agentState.getMemoryFragment(MemoryKeys.EDGE_START_POSITION);
            return gson.fromJson(edgeStartPositionString, Coordinate.class);
        }
        else {
            // Create edge start position
            int agentX = agentState.getX();
            int agentY = agentState.getY();
            Coordinate edgeStartPosition = new Coordinate(agentX, agentY);

            // Add edge start position to memory
            String edgeStartPositionString = gson.toJson(edgeStartPosition);
            agentState.addMemoryFragment(MemoryKeys.EDGE_START_POSITION, edgeStartPositionString);

            return edgeStartPosition;
        }
    }

    /**
     * Retrieve should be here position from memory
     * 
     * @param agentState Current state of agent
     * @return Should be here position
     */ 
    private Coordinate getShouldBeHerePosition(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        // Check if should be here position exists in memory
        if(memoryFragments.contains(MemoryKeys.SHOULD_BE_HERE_POSITION)) {
            // Retrieve should be here position
            Gson gson = new Gson();
            String shouldBeHereString = agentState.getMemoryFragment(MemoryKeys.SHOULD_BE_HERE_POSITION);
            return gson.fromJson(shouldBeHereString, Coordinate.class);
        }
        else return null;
    }

    /**
     * Update task memory of agent
     * 
     * @param agentState Current state of the agent
     * @param discoveredPackets List of discovered packets
     * @param discoveredDestinations List of discovered destinations
     */
    private void updateTaskMemory(AgentState agentState, ArrayList<Packet> discoveredPackets, ArrayList<Destination> discoveredDestinations) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();
                
        // Remove discovered packets and discovered destinations from memory
        if(memoryFragments.contains(MemoryKeys.DISCOVERED_PACKETS)) agentState.removeMemoryFragment(MemoryKeys.DISCOVERED_PACKETS);
        if(memoryFragments.contains(MemoryKeys.DISCOVERED_DESTINATIONS)) agentState.removeMemoryFragment(MemoryKeys.DISCOVERED_DESTINATIONS);

        // Add updated discovered packets and updated discovered destinations to memory
        Gson gson = new Gson();
        String discoveredPacketsString = gson.toJson(discoveredPackets);
        String discoveredDestinationsString = gson.toJson(discoveredDestinations);
        agentState.addMemoryFragment(MemoryKeys.DISCOVERED_PACKETS, discoveredPacketsString);
        agentState.addMemoryFragment(MemoryKeys.DISCOVERED_DESTINATIONS, discoveredDestinationsString);
        
        System.out.println("[MoveToPacketBehavior]{updateTaskMemory} Discovered packets and discovered destinations updated in memory");
    }

    /**
     * Update mapping memory of agent
     * 
     * @param agentState Current state of the agent
     * @param graph Graph
     * @param path Path
     * @param previousPosition Previous position
     * @param edgeStartPosition Edge start position
     * @param shouldBeHerePosition Should be here position
     */
    private void updateMappingMemory(AgentState agentState, Graph graph, List<Coordinate> path, Coordinate previousPosition, Coordinate edgeStartPosition, Coordinate shouldBeHerePosition) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();
        
        Gson gson = new Gson();
        if(graph != null) {
            // Remove graph from memory
            if(memoryFragments.contains(MemoryKeys.GRAPH)) agentState.removeMemoryFragment(MemoryKeys.GRAPH);
            
            // Add updated graph to memory
            String graphString = graph.toJson();
            agentState.addMemoryFragment(MemoryKeys.GRAPH, graphString);

            System.out.println("[MoveToPacketBehavior]{updateMappingMemory} Graph updated in memory");
        }

        if(path != null) {
            // Remove path from memory
            if(memoryFragments.contains(MemoryKeys.PATH)) agentState.removeMemoryFragment(MemoryKeys.PATH);
            
            // Add updated path to memory
            String pathString = gson.toJson(path);
            agentState.addMemoryFragment(MemoryKeys.PATH, pathString);

            System.out.println("[MoveToPacketBehavior]{updateMappingMemory} Path updated in memory");
        }

        if(previousPosition != null) {
            // Remove previous position from memory
            if(memoryFragments.contains(MemoryKeys.PREVIOUS_POSITION)) agentState.removeMemoryFragment(MemoryKeys.PREVIOUS_POSITION);
            
            // Add updated previous position to memory
            String previousPositionString = gson.toJson(previousPosition);
            agentState.addMemoryFragment(MemoryKeys.PREVIOUS_POSITION, previousPositionString);

            System.out.println("[MoveToPacketBehavior]{updateMappingMemory} Previous position updated in memory");
        }

        if(edgeStartPosition != null) {
            // Remove edge start position from memory
            if(memoryFragments.contains(MemoryKeys.EDGE_START_POSITION)) agentState.removeMemoryFragment(MemoryKeys.EDGE_START_POSITION);
            
            // Add updated edge start position to memory
            String edgeStartPositionString = gson.toJson(edgeStartPosition);
            agentState.addMemoryFragment(MemoryKeys.EDGE_START_POSITION, edgeStartPositionString);

            System.out.println("[MoveToPacketBehavior]{updateMappingMemory} Edge start position updated in memory");
        }
        
        if(shouldBeHerePosition != null) {
            // Remove should be here position from memory
            if(memoryFragments.contains(MemoryKeys.SHOULD_BE_HERE_POSITION)) agentState.removeMemoryFragment(MemoryKeys.SHOULD_BE_HERE_POSITION);
            
            // Add updated should be here position to memory
            String shouldBeHerePositionString = gson.toJson(shouldBeHerePosition);
            agentState.addMemoryFragment(MemoryKeys.SHOULD_BE_HERE_POSITION, shouldBeHerePositionString);

            System.out.println("[MoveToPacketBehavior]{updateMappingMemory} Should be here position updated in memory");
        }
    }
}