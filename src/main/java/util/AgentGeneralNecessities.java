package util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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


        List<Coordinate> positions = new ArrayList<>(AgentGeneralNecessities.RELATIVE_POSITIONS);

        // Prioritize going straight first
        Coordinate previousPosition = AgentGraphInteraction.getPreviousPosition(agentState);
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

            }
            else AgentGeneralNecessities.moveRandom(agentState, agentAction);

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
        ArrayList<Target> discoveredPackets = getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.DISCOVERED_PACKETS);
        ArrayList<Target> discoveredDestinations = getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.DISCOVERED_DESTINATIONS);
        ArrayList<Target> discoveredBatteryStations = getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.DISCOVERED_BATTERY_STATIONS);
        ArrayList<Target> nonBroadcastedBatteryStations = getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.NON_BROADCASTED_BATTERY_STATIONS);
        Task task = AgentTaskInteraction.getTask(agentState);
        Graph graph = AgentGraphInteraction.getGraph(agentState);

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

                        System.out.println("[AgentGeneralNecessities]{checkPerception} New destination discovered (" + discoveredDestinations.size() + ")");
                    }

                    // Update graph if unknown destination in cell
                    if(!graph.nodeExists(cell.getX(), cell.getY())) {
                        // If this destination is not already in the graph -> add it
                        AgentGraphInteraction.addTargetToGraph(agentState, destination);
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

                        System.out.println("[AgentGeneralNecessities]{checkPerception} New packet discovered (" + discoveredPackets.size() + ")");
                    }

                    // Add node of agent position that says that agent can see packet from position.
                    if (!graph.nodeExists(cell.getX(), cell.getY())) {
                        AgentGraphInteraction.addTargetToGraph(agentState, packet);
                    }
                } else if (cell.containsEnergyStation())
                {
                    BatteryStation batteryStation = new BatteryStation(cellCoordinate);

                    if (discoveredBatteryStations.contains(batteryStation))
                        continue;
                    else
                    {
                        discoveredBatteryStations.add(batteryStation);
                        nonBroadcastedBatteryStations.add(batteryStation);
                        System.out.println(String.format("[AgentGeneralNecessities]{checkPerception} Agent on location (%d,%d) has discovered a new battery station (" + discoveredBatteryStations.size() + ")", agentState.getX(), agentState.getY()));
                    }

                    if (!graph.nodeExists(cell.getX(), cell.getY()))
                    {
                        AgentGraphInteraction.addTargetToGraph(agentState, batteryStation);
                    }
                }
            }
        }

        // Update memory
        AgentTaskInteraction.updateTaskMemory(agentState, discoveredPackets, discoveredDestinations, discoveredBatteryStations, nonBroadcastedBatteryStations);        
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
