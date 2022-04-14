package util;

import java.awt.Color;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import agent.AgentAction;
import agent.AgentState;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;
import environment.world.destination.DestinationRep;
import environment.world.packet.PacketRep;
import util.graph.AgentGraphInteraction;
import util.graph.Graph;
import util.targets.Target;
import util.targets.BatteryStation;
import util.targets.Destination;
import util.targets.Packet;
import util.task.AgentTaskInteraction;
import util.task.Task;

public class AgentGeneralNecessities {

    private final static ArrayList<Coordinate> RELATIVE_POSITIONS = new ArrayList<>(List.of(
        new Coordinate(1, 1), 
        new Coordinate(-1, -1),
        new Coordinate(1, 0), 
        new Coordinate(-1, 0),
        new Coordinate(0, 1), 
        new Coordinate(0, -1),
        new Coordinate(1, -1), 
        new Coordinate(-1, 1)
    ));

    
    /**
     * Move randomly
     *
     * @param agentState Current state of agent
     * @param agentAction Perform an action with agent
     */
    public static void moveRandom(AgentState agentState, AgentAction agentAction) {
        // Retrieve position
        Perception perception = agentState.getPerception();
        int agentX = agentState.getX();
        int agentY = agentState.getY();

        // Retrieve a list of relative positions
        List<Coordinate> Positions = new ArrayList<>(AgentGeneralNecessities.RELATIVE_POSITIONS);

        // Prioritize going straight first by removing it from the list and later adding it as the first element
        Coordinate previousPosition = AgentGraphInteraction.getPreviousPosition(agentState);
        int vecX = agentState.getX() - previousPosition.getX();
        int vecY = agentState.getY() - previousPosition.getY();
        int dx = Integer.signum(vecX);
        int dy = Integer.signum(vecY);

        Coordinate inFront = new Coordinate(dx, dy);
        Positions.remove(inFront);

        // Shuffle relative positions and add the coordinate for going straight in the front
        Collections.shuffle(Positions);
        Positions.add(0, inFront);

        // Loop over all relative positions
        for (Coordinate relativePosition : Positions) {

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

                return;
            }
        }

        agentAction.skip();
    }

    /**
     * Move towards a specific position
     * 
     * @param agentState Current state of agent
     * @param agentAction Perform an action with agent
     * @param position Position to move towards
     */
    public static void moveToPosition(AgentState agentState, AgentAction agentAction, Coordinate position) {
        // Retrieve positions
        Perception perception = agentState.getPerception();
        int agentX = agentState.getX();
        int agentY = agentState.getY();

        Coordinate agentPosition = new Coordinate(agentX, agentY);
        int positionX = position.getX();
        int positionY = position.getY();

        // Retrieve path
        List<Coordinate> path = AgentGeneralNecessities.getPath(agentState);

        // Check if position is in current perception
        if(AgentGeneralNecessities.positionInPerception(agentState, position)) {
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

            } else {
                AgentGeneralNecessities.moveRandom(agentState, agentAction);
            }

            path.clear();
            AgentGraphInteraction.updateMappingMemory(agentState, null, path, null, null, null);
        }
        else {
            Graph graph = AgentGraphInteraction.getGraph(agentState);
            Coordinate shouldBeHerePosition = getShouldBeHerePosition(agentState);

            // If path exists -> Just follow the path.
            if (!path.isEmpty()) {

                // If previous movement failed for some reason -> Try again.
                if (!agentPosition.equals(shouldBeHerePosition)) {
                    if (shouldBeHerePosition != null)
                        moveToPosition(agentState, agentAction, shouldBeHerePosition);
                    else
                        AgentGeneralNecessities.moveRandom(agentState, agentAction);
                    return;
                }

                Coordinate nextCoordinate = path.remove(0); // TODO: Maybe path should not be linked list. (Stack?)
                shouldBeHerePosition = nextCoordinate;

                agentAction.step(nextCoordinate.getX(), nextCoordinate.getY());

                AgentGraphInteraction.updateMappingMemory(agentState, null, path, null, null, shouldBeHerePosition);
            }

            // If agent position outside the graph -> Move to the closest node first.
            else if (!graph.nodeExists(agentPosition))
            {
                Coordinate closestNodeCoordinate = graph.closestFreeNodeCoordinate(agentState.getPerception(), agentPosition);
                if (closestNodeCoordinate != null)
                    moveToPosition(agentState, agentAction, closestNodeCoordinate);
                else
                    AgentGeneralNecessities.moveRandom(agentState, agentAction);
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

                    AgentGraphInteraction.updateMappingMemory(agentState, null, path, null, null, shouldBeHerePosition);
                }
                else
                {
                    AgentGeneralNecessities.moveRandom(agentState, agentAction);
                }
            }
        }
    }

    /**
     * Check perception of agent
     *  
     * @param agentState Current state of agent
     */
    public static void checkPerception(AgentState agentState) {
        // Retrieve discovered packets, discovered destinations and task
        Perception perception = agentState.getPerception();
        Graph graph = AgentGraphInteraction.getGraph(agentState);

        // Loop over whole perception
        for (int x = 0; x < perception.getWidth(); x++) {
            for (int y = 0; y < perception.getHeight(); y++) {
                CellPerception cell = perception.getCellAt(x,y);

                if(cell == null) continue;

                Coordinate cellCoordinate = new Coordinate(cell.getX(), cell.getY());

                // Check if current cell contains a destination
                if(cell.containsAnyDestination()) AgentGeneralNecessities.destinationDiscovered(cell, cellCoordinate, graph, agentState);
                // Check if current cell contains a packet
                else if(cell.containsPacket()) AgentGeneralNecessities.packetDiscovered(cell, cellCoordinate, graph, agentState);
                // Check if cell contains an energy station
                else if (cell.containsEnergyStation()) AgentGeneralNecessities.chargingStationDiscovered(cell, cellCoordinate, graph, agentState);
            }
        }
    }

    /**
     * A function that helps with the checking of the pereception. If it sees a new destination, it will add this to
     * the graph and to the discovered destination.
     *
     * @param cellPerception: A perception of the cell, used to get information about that cell
     * @param cellCoordinate: The coordinates of the cell
     * @param graph: The graph of the agent
     * @param agentState: The state of the agent
     */
    private static void destinationDiscovered(CellPerception cellPerception, Coordinate cellCoordinate, Graph graph, AgentState agentState) {
        // Retrieve the list of already discovered Destinations
        ArrayList<Target> discoveredDestinations = getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.DISCOVERED_DESTINATIONS);

        // Retrieve the color of the discovered cell
        Color destinationColor = Objects.requireNonNull(cellPerception.getRepOfType(DestinationRep.class)).getColor();

        // Create a destination from the previous two attributes
        Destination destination = new Destination(cellCoordinate, destinationColor);

        // Guard clause to check if destination was not discovered yet
        if(discoveredDestinations.contains(destination)) return;

        // Add destination to discovered destinations
        discoveredDestinations.add(destination);
        System.out.println("[AgentGeneralNecessities]{checkPerception} New destination discovered (" + discoveredDestinations.size() + ")");

        // If this destination is not already in the graph -> add it
        if(!graph.nodeExists(cellPerception.getX(), cellPerception.getY())) AgentGraphInteraction.addTargetToGraph(agentState, destination);

        // Update the memory
        AgentTaskInteraction.updateTaskMemory(agentState, null, discoveredDestinations, null, null);
    }

    /**
     * A function that helps with the checking of the perception. If it sees a new packet, it will add this to
     * the graph and to the discovered packet.
     *
     * @param cellPerception: A perception of the cell, used to get information about that cell
     * @param cellCoordinate: The coordinates of the cell
     * @param graph: The graph of the agent
     * @param agentState: The state of the agent
     */
    private static void packetDiscovered(CellPerception cellPerception, Coordinate cellCoordinate, Graph graph, AgentState agentState) {
        // Retrieve the current task of the agent
        Task task = AgentTaskInteraction.getTask(agentState);

        // Retrieve the list of already discovered packet
        ArrayList<Target> discoveredPackets = getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.DISCOVERED_PACKETS);

        // Retrieve the color of the discovered cell
        Color packetColor = Objects.requireNonNull(cellPerception.getRepOfType(DestinationRep.class)).getColor();

        // Create a packet from the previous two attributes
        Packet packet = new Packet(cellCoordinate, packetColor);

        // Guard clause to check if packet was not discovered yet
        if (discoveredPackets.contains(packet)) return;
        if (task != null && task.getPacket().equals(packet)) return;

        // Add destination to discovered packet
        discoveredPackets.add(packet);
        System.out.println("[AgentGeneralNecessities]{checkPerception} New packet discovered (" + discoveredPackets.size() + ")");

        // If this packet is not already in the graph -> add it
        if (!graph.nodeExists(cellPerception.getX(), cellPerception.getY())) AgentGraphInteraction.addTargetToGraph(agentState, packet);

        // Update memory
        AgentTaskInteraction.updateTaskMemory(agentState, discoveredPackets, null, null, null);

    }

    /**
     * A function that helps with the checking of the perception. If it sees a new charging station, it will add this to
     * the graph and to the discovered charging stations and non-broadcasted charging stations.
     *
     * @param cellPerception: A perception of the cell, used to get information about that cell
     * @param cellCoordinate: The coordinates of the cell
     * @param graph: The graph of the agent
     * @param agentState: The state of the agent
     */
    private static void chargingStationDiscovered(CellPerception cellPerception, Coordinate cellCoordinate, Graph graph, AgentState agentState) {
        // Retrieve the list of already discovered battery stations and non-broadcasted battery stations
        ArrayList<Target> discoveredBatteryStations = getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.DISCOVERED_BATTERY_STATIONS);
        ArrayList<Target> nonBroadcastedBatteryStations = getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.NON_BROADCASTED_BATTERY_STATIONS);

        // Create a batterStation object from the given coordinates
        BatteryStation batteryStation = new BatteryStation(cellCoordinate);

        // Guard clause for when the battery station was already discovered
        if (discoveredBatteryStations.contains(batteryStation)) return;

        // Add the batteryStation to the discovered list and non broadcasted list
        discoveredBatteryStations.add(batteryStation);
        nonBroadcastedBatteryStations.add(batteryStation);
        System.out.printf("[AgentGeneralNecessities]{checkPerception} Agent on location (%d,%d) has discovered a new battery station (" + discoveredBatteryStations.size() + ")%n", agentState.getX(), agentState.getY());

        // If this battery station is not already in the graph -> add it
        if (!graph.nodeExists(cellPerception.getX(), cellPerception.getY())) AgentGraphInteraction.addTargetToGraph(agentState, batteryStation);

        // Update memory
        AgentTaskInteraction.updateTaskMemory(agentState, null, null, discoveredBatteryStations, nonBroadcastedBatteryStations);
    }

    /**
     * Check if position is in current perception
     *
     * @param agentState Current state of agent
     * @param position Position to check
     * @return True if position is in current perception
     */
    private static boolean positionInPerception(AgentState agentState, Coordinate position) {
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
     * Retrieve should be here position from memory
     * 
     * @param agentState Current state of agent
     * @return Should be here position
     */ 
    private static Coordinate getShouldBeHerePosition(AgentState agentState) {
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
     * Retrieve path from memory
     * Create path if not yet created
     * 
     * @param agentState Current state of agent
     * @return Path: List of coordinate
     */ 
    private static List<Coordinate> getPath(AgentState agentState) {
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
 

    /** Retrieve specified type of targets from memory
     * Create a list if this type of targets has not yet been created
     * 
     * @param agentState The current state of the agent
     * @param memoryKey The string specifying the key specifying the memoryfragment. This string can be fetched from the {@code MemoryKeys} class.
     * @return List of discovered targets of this type
     */
    public static ArrayList<Target> getDiscoveredTargetsOfSpecifiedType(AgentState agentState, String memoryKey)
    {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        Gson gson = new Gson();
        // Check if list of discovered packets exists in memory
        if(memoryFragments.contains(memoryKey)) {
            // Retrieve list of discovered packets 
            String discoveredTargetsString = agentState.getMemoryFragment(memoryKey);
            return gson.fromJson(discoveredTargetsString, new TypeToken<ArrayList<Target>>(){}.getType());
        }
        else {
            // Create list of discovered packets
            ArrayList<Target> discoveredTargets = new ArrayList<Target>();

            // Add list of discovered packets to memory
            String discoveredTargetsString = gson.toJson(discoveredTargets);
            agentState.addMemoryFragment(memoryKey, discoveredTargetsString);

            return discoveredTargets;
        }

    }
}
