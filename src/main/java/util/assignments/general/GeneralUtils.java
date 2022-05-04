package util.assignments.general;

import java.util.ArrayList;
import java.util.Map;

import java.util.List;

import agent.AgentCommunication;
import agent.AgentState;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;
import environment.world.agent.AgentRep;
import environment.world.destination.DestinationRep;
import environment.world.packet.PacketRep;
import util.assignments.graph.Graph;
import util.assignments.graph.Node;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.ChargingStation;
import util.assignments.targets.Destination;
import util.assignments.targets.Packet;
import util.assignments.task.Task;

import java.awt.*;
import java.util.Optional;

/**
 * A class that implements general functions
 */
public class GeneralUtils {

    // Energy values
    public static final int WALK_WITHOUT_PACKET = 10;
    public static final int WALK_WITH_PACKET = 25;

    /**
     * Check the perception of the agent. Perform the appropriate action when there is something in a cell in the
     * perception of the agent.
     *  
     * @param agentState The current state of the agent
     */
    public static void checkPerception(AgentState agentState) {
        // Get the perception of the agent
        Perception agentPerception = agentState.getPerception();

        // Loop over the whole perception
        for (int x = 0; x <= agentPerception.getWidth(); x++) {
            for (int y = 0; y <= agentPerception.getHeight(); y++) {
                CellPerception cellPerception = agentPerception.getCellAt(x, y);

                // Check if the cell is null and continue with the next cell if so
                if (cellPerception == null) continue;

                // Get the coordinates of the cell
                Coordinate cellCoordinate = new Coordinate(cellPerception.getX(), cellPerception.getY());

                // Check if the cell contains a packet
                if (cellPerception.containsPacket()) addPacket(agentState, cellPerception, cellCoordinate);

                // Check if the cell contains a destination
                if (cellPerception.containsAnyDestination()) addDestination(agentState, cellPerception, cellCoordinate);

                // Check if the cell contains a charging station
                if (cellPerception.containsEnergyStation()) addChargingStation(agentState, cellCoordinate);
            }
        }
    }

    /**
     * A function that adds a packet to its memory.
     *
     * @param agentState: The state of the agent
     * @param cellPerception: The perception of the cell
     * @param cellCoordinate: The coordinates of the cell
     */
    private static void addPacket(AgentState agentState, CellPerception cellPerception, Coordinate cellCoordinate) {
        // Retrieve memory fragments
        Task task = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.TASK, Task.class);
        ArrayList<Packet> discoveredPackets = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_PACKETS, Packet.class);

        // Get the color of the packet
        Color packetColor = cellPerception.getRepOfType(PacketRep.class).getColor();
        int packetRgbColor = packetColor.getRGB();

        // Create the corresponding packet
        Packet packet = new Packet(cellCoordinate, packetRgbColor);

        // Check if the packet was already discovered and continue with the next cell if so
        if(discoveredPackets.contains(packet)) return;

        // Check if packet is currently handled and continue with the next cell if so because it should not be added to list again
        if(task != null && task.getPacket().isPresent() && task.getPacket().get().equals(packet)) return;

        // Add the packet to the list of discovered packets
        discoveredPackets.add(packet);

        // Inform
        System.out.printf("%s: Discovered a new packet (%s) [%s]\n", agentState.getName(), packet, discoveredPackets.size());

        // Update memory
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.DISCOVERED_PACKETS, discoveredPackets));
    }

    /**
     * A function that adds a charging stattion to its memory.
     *
     * @param agentState: The state of the agent
     * @param cellCoordinate: The coordinates of the cell
     */
    private static void addChargingStation(AgentState agentState, Coordinate cellCoordinate) {
        // Retrieve the memory fragment
        ArrayList<ChargingStation> discoveredChargingStations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_CHARGING_STATIONS, ChargingStation.class);

        // Create the corresponding charging station
        ChargingStation chargingStation = new ChargingStation(cellCoordinate);

        // Check if the charging station was already discovered and continue with next cell if so
        if(discoveredChargingStations.contains(chargingStation)) return;

        // Add the charging station to the list of discovered charging stations
        discoveredChargingStations.add(chargingStation);

        // Inform
        System.out.printf("%s: Discovered a new charging station (%s) [%s]\n", agentState.getName(), chargingStation, discoveredChargingStations.size());

        // Update memory
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.DISCOVERED_CHARGING_STATIONS, discoveredChargingStations, MemoryKeys.UPDATED_STATIONS, true));
    }

    /**
     * A function that adds a destination to its memory.
     *
     * @param agentState: The state of the agent
     * @param cellPerception: The perception of the cell
     * @param cellCoordinate: The coordinates of the cell
     */
    private static void addDestination(AgentState agentState, CellPerception cellPerception, Coordinate cellCoordinate) {
        // Retrieve the memory fragments
        ArrayList<Destination> discoveredDestinations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_DESTINATIONS, Destination.class);

        // Get the color of the destination
        int destinationRgbColor = cellPerception.getRepOfType(DestinationRep.class).getColor().getRGB();

        // Create the corresponding destination
        Destination destination = new Destination(cellCoordinate, destinationRgbColor);

        // Check if the destination was already discovered
        if(discoveredDestinations.contains(destination)) return;

        // Add the destination to the list of discovered destinations
        discoveredDestinations.add(destination);

        // Inform
        System.out.printf("%s: Discovered a new destination (%s) [%s]\n", agentState.getName(), destination, discoveredDestinations.size());

        // Update memory
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.DISCOVERED_DESTINATIONS, discoveredDestinations, MemoryKeys.UPDATED_STATIONS, true));
    }

    /**
     * A function that is used to communicate the location of destinations.
     *
     * @param agentState: The state of the agent
     * @param agentCommunication: The interface for communication
     */
    public static void handleDestinationLocations(AgentState agentState, AgentCommunication agentCommunication) {
        // Share the destinations
        shareDestinationsInformation(agentState, agentCommunication);

        // Update list
        updateDestinationsInformation(agentState, agentCommunication);
    }

    /**
     * A function that updates the destination information with the information received from other agents.
     *
     * @param agentState: The state of the agent
     * @param agentCommunication: The interface for communication
     */
    private static void updateDestinationsInformation(AgentState agentState, AgentCommunication agentCommunication) {
        // Get the current destinations
        ArrayList<Destination> currentDestinations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_DESTINATIONS, Destination.class);

        // Get the updated destinations
        ArrayList<Destination> updatedDestinations = CommunicationUtils.getListFromMails(agentState, agentCommunication, MemoryKeys.DISCOVERED_DESTINATIONS, Destination.class);

        // Loop over updated destinations
        for(Destination updatedDestination: updatedDestinations) {
            // Check if the destinations is not included in the current list and add it if so skip
            if (currentDestinations.contains(updatedDestination)) continue;

            // Add the new destinations to the charging stations
            currentDestinations.add(updatedDestination);

            // Inform
            System.out.printf("%s: Add a new destination from communication (%s) [%s]\n", agentState.getName(), updatedDestination, currentDestinations.size());
        }

        // Update the current destinations
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.DISCOVERED_DESTINATIONS, currentDestinations));
    }

    /**
     * A function that is used to share the destinations with other agents in its perception.
     *
     * @param agentState: The state of the agent
     * @param agentCommunication: The interface for communication
     */
    private static void shareDestinationsInformation(AgentState agentState, AgentCommunication agentCommunication) {
        // Retrieve the perception
        Perception agentPerception = agentState.getPerception();

        // Loop over the whole perception
        for (int x = 0; x < agentPerception.getWidth(); x++) {
            for (int y = 0; y < agentPerception.getHeight(); y++) {
                CellPerception cellPerception = agentPerception.getCellAt(x, y);

                // Check if the cell is null and continue with the next cell if so
                if (cellPerception == null) continue;

                // Retrieve the agent representation on the cell
                Optional<AgentRep> agentRep = cellPerception.getAgentRepresentation();

                // If no agent on the cell, go to the next cell
                if (agentRep.isEmpty()) continue;

                // If the agentRep is us, continue to next cell
                // TODO: Improve this so it isn't name based
                if (agentRep.get().getName().equals(agentState.getName())) continue;

                // Create a message string to send to the agent on the cell
                String memoryFragmentString = agentState.getMemoryFragment(MemoryKeys.DISCOVERED_DESTINATIONS);
                String messageString = CommunicationUtils.makeMessageString(memoryFragmentString, MemoryKeys.DISCOVERED_DESTINATIONS);

                // Communicate the message to the agent
                agentCommunication.sendMessage(agentRep.get(), messageString);

                // Display a message to dev
                System.out.printf("%s: Sends it's destinations to agent: %s\n", agentState.getName(), agentRep.get().getName());
            }
        }
    }


        /**
         * A function that is used for updating and sharing the charging stations.
         *
         * @param agentState The current state of the agent
         * @param agentCommunication Perform communication with the agent
         */
    public static void handleChargingStations(AgentState agentState, AgentCommunication agentCommunication) {
        // Check if we changed something
        boolean changed = Boolean.TRUE.equals(MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.UPDATED_STATIONS, Boolean.class));

        // Broadcast if we changed something
        if (changed) {
            shareChargingStationsInformation(agentState, agentCommunication);
            MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.UPDATED_STATIONS, false));
        }

        // Look at messages, If we shared something the others can not have changed something or give priority to true statements
        if (!changed) updateChargingStationsInformation(agentState, agentCommunication);
    }

    /**
     * Share own charging station information
     * 
     * @param agentState The current state of the agent
     * @param agentCommunication Perform communication with the agent
     */
    private static void shareChargingStationsInformation(AgentState agentState, AgentCommunication agentCommunication) {
        CommunicationUtils.broadcastMemoryFragment(agentState, agentCommunication, MemoryKeys.DISCOVERED_CHARGING_STATIONS);
    }

    /**
     * Update own charging station information by means of message exchange with other agents
     * 
     * @param agentState The current state of the agent
     * @param agentCommunication Perform communication with the agent
     */
    private static void updateChargingStationsInformation(AgentState agentState, AgentCommunication agentCommunication) {
        // Get the current charging stations
        ArrayList<ChargingStation> currentChargingStations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_CHARGING_STATIONS, ChargingStation.class);

        // Get the updated charging stations
        ArrayList<ChargingStation> updatedChargingStations = CommunicationUtils.getListFromMails(agentState, agentCommunication, MemoryKeys.DISCOVERED_CHARGING_STATIONS, ChargingStation.class);

        // Loop over updated charging stations
        for(ChargingStation updatedChargingStation: updatedChargingStations) {
            // Check if the charging station is not included in the current list and add it if so
            if(!currentChargingStations.contains(updatedChargingStation)) {
                // Add the new charging station to the charging stations
                currentChargingStations.add(updatedChargingStation);

                // Inform
                System.out.printf("%s: Add a new charging station from communication (%s) [%s]\n", agentState.getName(), updatedChargingStation, currentChargingStations.size());
                continue;
            }            

            // Loop over current charging stations
            for(ChargingStation currentChargingStation: currentChargingStations) {
                // Check if charging stations correspond
                if(currentChargingStation.equals(updatedChargingStation)) {
                    // Update the current charging station if needed
                    currentChargingStation.setInUse(updatedChargingStation.isInUse());
                    currentChargingStation.setBatteryOfUser(updatedChargingStation.getBatteryOfUser());
                
                    // Inform
                    System.out.printf("%s: Updated a known charging station from communication (%s)\n", agentState.getName(), currentChargingStation);
                }
            }
        }

        // Update the current charging stations
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.DISCOVERED_CHARGING_STATIONS, currentChargingStations));
    }
    
    /**
     * A function to know if the agent has reached the position
     * 
     * @param agentState The current state of the agent
     * @param coordinate The coordinate of the position to reach
     * @return True is the agent is next to the position, otherwise false
     */
    public static boolean hasReachedPosition(AgentState agentState, Coordinate coordinate) {
        // Get the positions
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        int coordinateX = coordinate.getX();
        int coordinateY = coordinate.getY();

        // Calculate the difference between the positions
        int dX = Math.abs(agentX - coordinateX);
        int dY = Math.abs(agentY - coordinateY);

        // Return true if the distance is less than 1 for both axes
        return (dX <= 1) && (dY <= 1);
    }
    
    /**
     * A function to know if a specific position is in the perception of the agent
     * 
     * @param agentState The current state of the agent
     * @param coordinate The coordinate of the position to check
     * @return True is the position is in the perception of the agent, otherwise false
     */
    public static boolean positionInPerception(AgentState agentState, Coordinate coordinate) {
        // Get the position
        int coordinateX = coordinate.getX();
        int coordinateY = coordinate.getY();

        // Get the perception of the agent
        Perception agentPerception = agentState.getPerception();

        // Loop over the whole perception
        for (int x = 0; x < agentPerception.getWidth(); x++) {
            for (int y = 0; y < agentPerception.getHeight(); y++) {
                CellPerception cellPerception = agentPerception.getCellAt(x,y);

                // Check if the cell is null and continue with the next cell if so
                if(cellPerception == null) continue;

                // Get the position of the cell
                int cellX = cellPerception.getX();
                int cellY = cellPerception.getY();
                
                // Check if the positions correspond
                if(cellX == coordinateX && cellY == coordinateY) 
                    return true;
            }
        }

        return false;
    }
    
    /**
     * A function to know if a specific position is in the graph
     * 
     * @param agentState The current state of the agent
     * @param coordinate The coordinate of the position to check
     * @return True is the position is in the graph, otherwise false
     */
    public static boolean positionInGraph(AgentState agentState, Coordinate coordinate) {
        // Get the graph
        Graph graph = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class);

        // Check if the graph is null and return false turn if so
        if(graph == null) return false;

        // Get the graph map
        Map<Node, List<Node>> map = graph.getMap();

        // Loop over graph nodes
        for(Node node: map.keySet()) {
            // Get the position of the node
            Coordinate nodeCoordinate = node.getCoordinate();

            // Check if coordinates correspond
            if(nodeCoordinate.equals(coordinate)) return true;

        }

        return false;
    }

    /**
     * A function that determines whether the agents has enough energy left to pick up the given packet en deliver it
     * to the given destination. The cost is based on where the agent is currently standing.
     *
     * @param agentState: The state of the agent
     * @param packet: The packet it has to check
     * @param destination: The destination the packet has to go to
     *
     * @return True if enough energy to perform the task, false otherwise.
     */
    public static boolean hasEnoughBatteryToCompleteTask(AgentState agentState, Packet packet, Destination destination) {
        // First calculate the power to go to the packet location
        Coordinate packetPosition = packet.getCoordinate();
        int cellsToWalk1 = Perception.distance(agentState.getX(), agentState.getY(), packetPosition.getX(), packetPosition.getY());
        double powerToGoToPacket = cellsToWalk1 * GeneralUtils.WALK_WITHOUT_PACKET;

        // Second calculate the power to go from packet to destination
        Coordinate destinationPosition = destination.getCoordinate();
        int cellsToWalk2 = Perception.distance(packetPosition.getX(), packetPosition.getY(), destinationPosition.getX(), destinationPosition.getY());
        double powerToGoToDestination = cellsToWalk2 * GeneralUtils.WALK_WITH_PACKET;

        // See if there is enough power to complete the task and have an extra buffer
        return agentState.getBatteryState() > (powerToGoToDestination + powerToGoToPacket + 150);
    }

    /**
     * A function to calculate the Euclidean distance between two coordinates
     *
     * @param coordinate1 The first coordinate
     * @param coordinate2 The second coordinate
     */
    public static double calculateEuclideanDistance(Coordinate coordinate1, Coordinate coordinate2) {
        // Get the positions
        int coordinate1X = coordinate1.getX();
        int coordinate1Y = coordinate1.getY();
        int coordinate2X = coordinate2.getX();
        int coordinate2Y = coordinate2.getY();

        // Calculate the distance
        return Math.sqrt(((coordinate2Y - coordinate1Y) * (coordinate2Y - coordinate1Y)) + ((coordinate2X - coordinate1X) * (coordinate2X - coordinate1X)));
    }

    /**
     * A function that is used to figure out if the agent is on the charging station.
     *
     * @param agentState: The state of the agent
     *
     * @return true if the agent is an the charging station, false otherwise.
     */
    public static boolean isOnChargingPad(AgentState agentState) {
        // Retrieve the coordinates of the agent
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        Coordinate agentPosition = new Coordinate(agentX, agentY);

        // Retrieve the charging stations from memory
        ArrayList<ChargingStation> discoveredChargingStations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_CHARGING_STATIONS, ChargingStation.class);

        // Iterate through all stations
        for (ChargingStation station : discoveredChargingStations) {

            // Calculate the coordinates of the station
            int stationX = station.getCoordinate().getX();
            int stationY = station.getCoordinate().getY() - 1;
            Coordinate stationPosition = new Coordinate(stationX, stationY);

            // If the positions are equal, the agent is on a charging station
            if (stationPosition.equals(agentPosition)) return true;
        }

        // If the loop finished, the agent isn't on a charging station
        return false;

    }
}
