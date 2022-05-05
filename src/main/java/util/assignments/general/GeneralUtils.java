package util.assignments.general;

import java.util.ArrayList;
import java.util.Map;

import java.util.List;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.assignment_2.behaviors.ChargingBehavior;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;
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

import java.awt.Color;

/**
 * A class that implements general functions
 */
public class GeneralUtils {

    // Energy values
    public static final int WALK_WITHOUT_PACKET = 10;
    public static final int WALK_WITH_PACKET = 25;

    ////////////////
    // PERCEPTION //
    ////////////////

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
     * @param agentState The current state of the agent
     * @param cellPerception The perception of the cell
     * @param packetCoordinate The coordinates of the packet
     */
    private static void addPacket(AgentState agentState, CellPerception cellPerception, Coordinate packetCoordinate) {
        // Retrieve memory fragments
        Task task = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.TASK, Task.class);
        ArrayList<Packet> discoveredPackets = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_PACKETS, Packet.class);

        // Get the color of the packet
        Color packetColor = cellPerception.getRepOfType(PacketRep.class).getColor();
        int packetRgbColor = packetColor.getRGB();

        // Create the corresponding packet
        Packet packet = new Packet(packetCoordinate, packetRgbColor);

        // Check if the packet was already discovered and continue with the next cell if so
        if(discoveredPackets.contains(packet)) return;

        // Check if packet is currently handled and continue with the next cell if so because it should not be added to list again
        if(task != null && task.getPacket().equals(packet)) return;

        // Add the packet to the list of discovered packets
        discoveredPackets.add(packet);

        // Inform
        System.out.printf("%s: Discovered a new packet (%s) [%s]\n", agentState.getName(), packet, discoveredPackets.size());

        // Update memory
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.DISCOVERED_PACKETS, discoveredPackets));
    }

    /**
     * A function that adds a destination to its memory.
     *
     * @param agentState The current state of the agent
     * @param cellPerception The perception of the cell
     * @param destinationCoordinate The coordinates of the destination
     */
    private static void addDestination(AgentState agentState, CellPerception cellPerception, Coordinate destinationCoordinate) {
        // Retrieve the memory fragments
        ArrayList<Destination> discoveredDestinations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_DESTINATIONS, Destination.class);

        // Get the color of the destination
        int destinationRgbColor = cellPerception.getRepOfType(DestinationRep.class).getColor().getRGB();

        // Create the corresponding destination
        Destination destination = new Destination(destinationCoordinate, destinationRgbColor);

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
     * A function that adds a charging station to its memory.
     *
     * @param agentState The current state of the agent
     * @param chargingStationCoordinate The coordinates of the charging station
     */
    private static void addChargingStation(AgentState agentState, Coordinate chargingStationCoordinate) {
        // Retrieve the memory fragment
        ArrayList<ChargingStation> discoveredChargingStations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_CHARGING_STATIONS, ChargingStation.class);

        // Create the corresponding charging station
        ChargingStation chargingStation = new ChargingStation(chargingStationCoordinate);

        // Check if the charging station was already discovered and continue with next cell if so
        if(discoveredChargingStations.contains(chargingStation)) return;

        // Add the charging station to the list of discovered charging stations
        discoveredChargingStations.add(chargingStation);

        // Inform
        System.out.printf("%s: Discovered a new charging station (%s) [%s]\n", agentState.getName(), chargingStation, discoveredChargingStations.size());

        // Update memory
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.DISCOVERED_CHARGING_STATIONS, discoveredChargingStations, MemoryKeys.UPDATED_STATIONS, true));
    }

    /////////////////////////
    // INFORMATION SHARING //
    /////////////////////////

        /////////////
        // GENERAL //
        /////////////

    /**
     * A function that is used to communicate information about the destination.
     *
     * @param agentState The current state of the agent
     * @param agentCommunication The interface for communication
     */
    public static void handleDestinationsCommunication(AgentState agentState, AgentCommunication agentCommunication) {
        // Share the destinations
        shareDestinations(agentState, agentCommunication);

        // Update list
        updateDestinations(agentState, agentCommunication);
    }

    /**
     * A function that is used to communicate information about the charging stations.
     *
     * @param agentState The current state of the agent
     * @param agentCommunication The interface for communication
     */
    public static void handleChargingStationsCommunication(AgentState agentState, AgentCommunication agentCommunication) {
        // Get if the list of discovered charging stations was updated
        boolean updatedStations = Boolean.TRUE.equals(MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.UPDATED_STATIONS, Boolean.class));

        // Check if the list of discovered charging stations was updated
        if(updatedStations) {
            // Share information about the charging stations
            shareChargingStations(agentState, agentCommunication);

            // Update the memory
            MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.UPDATED_STATIONS, false));
        }
        else {
            // Look at messages, If we shared something the others can not have changed something or give priority to true statements
            updateChargingStations(agentState, agentCommunication);
        }
    }

        ///////////
        // SHARE //
        ///////////

    /**
     * A function that is used to share the destinations with other agents in its perception.
     *
     * @param agentState The current state of the agent
     * @param agentCommunication The interface for communication
     */
    private static void shareDestinations(AgentState agentState, AgentCommunication agentCommunication) {
        // Send messages with the list of discovered destinations
        CommunicationUtils.sendMemoryFragment(agentState, agentCommunication, MemoryKeys.DISCOVERED_DESTINATIONS);
    }

    /**
     * A function that is used to share the charging stations with other agents.
     *
     * @param agentState The current state of the agent
     * @param agentCommunication The interface for communication
     */
    private static void shareChargingStations(AgentState agentState, AgentCommunication agentCommunication) {
        // Broadcast the list of discovered charging stations
        CommunicationUtils.broadcastMemoryFragment(agentState, agentCommunication, MemoryKeys.DISCOVERED_CHARGING_STATIONS);
    }

        ////////////
        // UPDATE //
        ////////////

    /**
     * A function that updates the list of discovered destinations with the information received from other agents.
     *
     * @param agentState The current state of the agent
     * @param agentCommunication The interface for communication
     */
    private static void updateDestinations(AgentState agentState, AgentCommunication agentCommunication) {
        // Get the current destinations
        ArrayList<Destination> currentDestinations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_DESTINATIONS, Destination.class);

        // Get the updated destinations
        ArrayList<Destination> updatedDestinations = CommunicationUtils.getListFromMails(agentState, agentCommunication, MemoryKeys.DISCOVERED_DESTINATIONS, Destination.class);

        // Loop over updated destinations
        for(Destination updatedDestination: updatedDestinations) {
            // Check if the destinations is included in the current list and continue with the next destination if so
            if (currentDestinations.contains(updatedDestination)) continue;

            // Add the new destination to the list
            currentDestinations.add(updatedDestination);

            // Inform
            System.out.printf("%s: Added a new destination from communication (%s) [%s]\n", agentState.getName(), updatedDestination, currentDestinations.size());
        }

        // Update the current destinations
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.DISCOVERED_DESTINATIONS, currentDestinations));
    }

    /**
     * A function that updates the list of discovered charging stations with the information received from other agents.
     *
     * @param agentState The current state of the agent
     * @param agentCommunication The interface for communication
     */
    private static void updateChargingStations(AgentState agentState, AgentCommunication agentCommunication) {
        // Get the current charging stations
        ArrayList<ChargingStation> currentChargingStations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_CHARGING_STATIONS, ChargingStation.class);

        // Get the updated charging stations
        ArrayList<ChargingStation> updatedChargingStations = CommunicationUtils.getListFromMails(agentState, agentCommunication, MemoryKeys.DISCOVERED_CHARGING_STATIONS, ChargingStation.class);

        // Loop over updated charging stations
        for(ChargingStation updatedChargingStation: updatedChargingStations) {
            // Check if the charging station is not included in the current list
            if(!currentChargingStations.contains(updatedChargingStation)) {
                // Add the new charging station to the charging stations
                currentChargingStations.add(updatedChargingStation);

                // Inform
                System.out.printf("%s: Added a new charging station from communication (%s) [%s]\n", agentState.getName(), updatedChargingStation, currentChargingStations.size());
                
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

    ///////////
    // UTILS //
    ///////////
    
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

    public static void handleEmergencyMessage(AgentState agentState, AgentCommunication agentCommunication) {
        // Check if the battery level is low enough to send emergency notification
        if (agentState.getBatteryState() <= 50) {
            String msg = "Emergency";
            String type = "String";
            CommunicationUtils.sendEmergencyMessage(agentState, agentCommunication, msg, type);
        }

        // Guard clause to ensure the behavior is the chargingBehavior
        if (!agentState.getCurrentBehavior().getClass().equals(ChargingBehavior.class)) return;

        // Retrieve the msg from the communication channel
        String msg = CommunicationUtils.getObjectFromMails(agentCommunication, "String", String.class);

        if (msg == null || !msg.equals("Emergency")) return;

        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.EMERGENCY, true));

    }
}
