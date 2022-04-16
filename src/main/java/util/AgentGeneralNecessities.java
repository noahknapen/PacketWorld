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
     * A function that moves the agent randomly. It accomplishes this by shuffling the relative positions to the agent
     * and checking in order if they are walkable and if so, walk on that position otherwise go to the next in line.
     *  @param agentState Current state of agent
     * @param agentAction Perform an action with agent
     */
    public static void moveRandom(AgentState agentState, AgentAction agentAction) {
        // Retrieve position
        Perception perception = agentState.getPerception();
        int agentX = agentState.getX();
        int agentY = agentState.getY();

        // Retrieve a list of relative positions
        List<Coordinate> Positions = new ArrayList<>(AgentGeneralNecessities.RELATIVE_POSITIONS);

        //if (targetPosition == null) {
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
        /*}
        else
        {
            Positions.sort(
                    (c1, c2) -> (int) (AgentGraphInteraction.getGraph(agentState).calculateDistance(c2, targetPosition)
                    - AgentGraphInteraction.getGraph(agentState).calculateDistance(c1, targetPosition)));

            // Update visited node
            List<Coordinate> visitedNodes = AgentGraphInteraction.getVisited(agentState);
            visitedNodes.add(new Coordinate(agentX, agentY));
            AgentGraphInteraction.updateMappingMemory(agentState, null, null, null, null, null, visitedNodes);
        }
        */

        // Loop over all relative positions
        for (Coordinate relativePosition : Positions) {

            // Calculate move
            int relativePositionX = relativePosition.getX();
            int relativePositionY = relativePosition.getY();
            CellPerception cellPerception = perception.getCellPerceptionOnRelPos(relativePositionX, relativePositionY);

            //Check if cell is walkable
            if (cellPerception != null && cellPerception.isWalkable()) { // && (targetPosition == null || !AgentGeneralNecessities.isVisited(agentState, cellPerception))) {
                int newPositionX = agentX + relativePositionX;
                int newPositionY = agentY + relativePositionY;

                // Perform a step
                agentAction.step(newPositionX, newPositionY);

                return;
            }
        }

        agentAction.skip();
    }

    private static boolean isVisited(AgentState agentState, CellPerception cellPerception) {
        List<Coordinate> visitedNodes = AgentGeneralNecessities.getVisited(agentState);
        Coordinate coordinate = new Coordinate(cellPerception.getX(), cellPerception.getY());
        return visitedNodes.contains(coordinate);
    }

    /**
     * Move towards a specific position. This is accomplished by using two help-functions. The first function is when
     * the position is in the perception of the agent. The agent can directly move to it. The second one is when it is
     * not directly in the agent's perception.
     * 
     * @param agentState Current state of agent
     * @param agentAction Perform an action with agent
     * @param position Position to move towards
     */
    public static void moveToPosition(AgentState agentState, AgentAction agentAction, Coordinate position) {
        if(AgentGeneralNecessities.positionInPerception(agentState, position)) {
            // Position is in current perception
            AgentGeneralNecessities.moveToPositionWhenInCurrentPerception(agentState, position, agentAction);
        }
        else {
            // Position is not in current perception
            moveToPositionWhenNotInCurrentPerception(agentState, agentAction, position);
        }
    }

    /**
     * A help-function to move to a given position when it is not in the current perception. It checks if there exists a
     * path, if so follow the path. Otherwise, if the agent is outside the graph, it needs to move to a node inside the
     * graph. Lastly, search for a path using dijkstra algorithm.
     *
     * @param agentState: Current state of the agent
     * @param agentAction: Used to perform an action with the agent
     * @param position: The position the agent needs to go to
     */
    private static void moveToPositionWhenNotInCurrentPerception(AgentState agentState, AgentAction agentAction, Coordinate position) {
        Graph graph = AgentGraphInteraction.getGraph(agentState);
        List<Coordinate> path = new ArrayList<>(AgentGeneralNecessities.getPath(agentState));
        Coordinate agentPosition = new Coordinate(agentState.getX(), agentState.getY());
        Coordinate shouldBeHerePosition = getShouldBeHerePosition(agentState);


        // If path exists -> Just follow the path.
        if (!path.isEmpty()) {

            // If previous movement failed for some reason -> Try again.
            if (shouldBeHerePosition != null && !agentPosition.equals(shouldBeHerePosition)) {
                moveToPosition(agentState, agentAction, shouldBeHerePosition);
                return;
            }

            // Take the nextCoordinate and remove it from the path
            Coordinate nextCoordinate = path.remove(0);
            shouldBeHerePosition = nextCoordinate;

            // Check if nextCoordinate is valid
            if (Math.abs(nextCoordinate.getX() - agentPosition.getX()) > 1 || Math.abs(nextCoordinate.getY() - agentPosition.getY()) > 1) {
                // If illegal path point -> Delete the path to recompute it.
                graph.setCurrentPath(new ArrayList<>());
                agentAction.skip();
            }
            else
            {
                // Go to the nextCoordinate
                agentAction.step(nextCoordinate.getX(), nextCoordinate.getY());
                graph.setCurrentPath(path);
            }
                // Update memory
                AgentGraphInteraction.updateMappingMemory(agentState, graph, null, null, null, shouldBeHerePosition, null);
        }


        // If position does not exist in graph -> Try to move towards the position.
        else if (!graph.nodeExists(position)) {
            // Retrieve a list of relative positions
            List<Coordinate> Positions = new ArrayList<>(AgentGeneralNecessities.RELATIVE_POSITIONS);
            Positions.sort(
                    (c1, c2) -> (int) (graph.calculateDistance(c2, position)
                            - graph.calculateDistance(c1, position)));

            List<Coordinate> visitedNodes = new ArrayList<>(AgentGeneralNecessities.getVisited(agentState));
            visitedNodes.add(agentPosition);
            AgentGraphInteraction.updateMappingMemory(agentState, null, null, null, null, null, visitedNodes);

            Perception perception = agentState.getPerception();

            // Loop over all relative positions
            for (Coordinate relativePosition : Positions) {

                // Calculate move
                int relativePositionX = relativePosition.getX();
                int relativePositionY = relativePosition.getY();
                CellPerception cellPerception = perception.getCellPerceptionOnRelPos(relativePositionX, relativePositionY);

                //Check if cell is walkable
                if (cellPerception != null && cellPerception.isWalkable()
                        && !AgentGeneralNecessities.isVisited(agentState, cellPerception)) {
                    int newPositionX = agentPosition.getX() + relativePositionX;
                    int newPositionY = agentPosition.getY() + relativePositionY;

                    // Perform a step
                    agentAction.step(newPositionX, newPositionY);

                    shouldBeHerePosition = new Coordinate(newPositionX, newPositionY);

                    // Update memory
                    AgentGraphInteraction.updateMappingMemory(agentState, graph, null, null,
                            null, shouldBeHerePosition, null);

                    return;
                }
            }
            AgentGeneralNecessities.moveRandom(agentState, agentAction);
        }

        // Search for path from current position to the desired position.
        else {

            // Retrieve the closest node to the agent position
            Coordinate edgeStartPosition = AgentGraphInteraction.getEdgeStartPosition(agentState);

            // Perform Dijkstra's algorithm
            List<Coordinate> graphPath = new ArrayList<>(graph.doSearch(edgeStartPosition, position));

            if (!graphPath.isEmpty()) {

                // Add path points from agentPosition to the edgeStartPosition and then add the graph path
                path = new ArrayList<>(graph.generatePathPoints(agentPosition, edgeStartPosition));
                path.addAll(graphPath);

                boolean test = testPath(path);
                // Retrieve the next coordinate from the path
                Coordinate nextCoordinate = path.remove(0);

                // Take a step towards the next coordinate
                agentAction.step(nextCoordinate.getX(), nextCoordinate.getY());

                // Update memory
                graph.setCurrentPath(path);
                AgentGraphInteraction.updateMappingMemory(agentState, graph, null, null, null, nextCoordinate, new ArrayList<>());
            } else {

                // Should never come in here
                // Make a random move
                AgentGeneralNecessities.moveRandom(agentState, agentAction);
            }
        }
    }


    private static boolean testPath(List<Coordinate> path) {
        for (int i = 0; i < path.size() - 1; i++) {
            if (Math.abs(path.get(i).getX() - path.get(i+1).getX()) > 1 ||
                Math.abs(path.get(i).getY() - path.get(i+1).getY()) > 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * A help-function to move to a given position when it is in the current perception.
     *
     * @param agentState: Current state of the agent
     * @param agentAction: Used to perform an action with the agent
     * @param position: The position the agent needs to go to
     */
    private static void moveToPositionWhenInCurrentPerception(AgentState agentState, Coordinate position, AgentAction agentAction) {
        Perception perception = agentState.getPerception();
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        int positionX = position.getX();
        int positionY = position.getY();

        // Calculate move
        int dX = positionX - agentX;
        int dY = positionY - agentY;

        // Check if position reached

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
            // Take a random step
            AgentGeneralNecessities.moveRandom(agentState, agentAction);
        }

        // Update the memory
        AgentGraphInteraction.updateMappingMemory(agentState, null, null, null, null, null, new ArrayList<>());
    }

    /**
     * Check perception of the agent. It uses help-functions to handle newly found destinations, packets and
     * battery stations
     *  
     * @param agentState Current state of agent
     */
    public static void checkPerception(AgentState agentState) {
        // Retrieve perception and the graph
        Perception perception = agentState.getPerception();
        Graph graph = AgentGraphInteraction.getGraph(agentState);

        // Loop over whole perception
        for (int x = 0; x < perception.getWidth(); x++) {
            for (int y = 0; y < perception.getHeight(); y++) {
                CellPerception cell = perception.getCellAt(x,y);

                // A guard clause to ensure cell isn't null
                if(cell == null) continue;

                // Create a coordinate object
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
        Color packetColor = Objects.requireNonNull(cellPerception.getRepOfType(PacketRep.class)).getColor();

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


        // TODO: Simplify this
        // If this battery station is not already in the graph -> add it
        int batteryChargeX = batteryStation.getCoordinate().getX();
        int batteryChargeY = batteryStation.getCoordinate().getY() - 1;
        Coordinate batteryChargingCoordinate = new Coordinate(batteryChargeX, batteryChargeY);

        // Create a batterStation object from the given coordinates
        BatteryStation batteryChargeStation = new BatteryStation(batteryChargingCoordinate);
        if (!graph.nodeExists(batteryChargingCoordinate)) AgentGraphInteraction.addTargetToGraph(agentState, batteryChargeStation);

        // Update memory
        AgentTaskInteraction.updateTaskMemory(agentState, null, null, discoveredBatteryStations, nonBroadcastedBatteryStations);
    }

    /**
     * Check if the given position is in current perception of the agent.
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
     * Retrieve path from memory or create a new path if not yet created.
     * 
     * @param agentState Current state of agent
     * @return Path: List of coordinate
     */ 
    private static List<Coordinate> getPath(AgentState agentState) {
        // Retrieve memory of agent
        Graph graph = AgentGraphInteraction.getGraph(agentState);
        return graph.getCurrentPath();

        /*
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
        */
    }

    /**
     * Retrieve visited from memory or create a new visited if not yet created.
     *
     * @param agentState Current state of agent
     * @return Visited: List of coordinate
     */
    public static ArrayList<Coordinate> getVisited(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        Gson gson = new Gson();
        // Check if path exists in memory
        if(memoryFragments.contains(MemoryKeys.VISITED_NODES)) {
            // Retrieve path
            String visitedString = agentState.getMemoryFragment(MemoryKeys.VISITED_NODES);
            return gson.fromJson(visitedString, new TypeToken<List<Coordinate>>(){}.getType());
        }
        else {
            // Create path
            ArrayList<Coordinate> visitedNodes = new ArrayList<>();

            // Add path to memory
            String visitedString = gson.toJson(visitedNodes);
            agentState.addMemoryFragment(MemoryKeys.VISITED_NODES, visitedString);

            return visitedNodes;
        }
    }

    /**
     * Retrieve specified type of targets from memory
     * Create a list if this type of targets has not yet been created
     * 
     * @param agentState The current state of the agent
     * @param memoryKey The string specifying the key specifying the memoryfragment. This string can be fetched from the {@code MemoryKeys} class.
     *
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
