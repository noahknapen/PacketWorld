package util.assignments.general;

import java.util.*;

import agent.AgentCommunication;
import agent.AgentState;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;
import util.assignments.graph.Graph;
import util.assignments.graph.GraphUtils;
import util.assignments.graph.Node;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.ChargingStation;
import util.assignments.targets.Destination;
import util.assignments.targets.Packet;

/**
 * A class that implements general functions
 */
public class GeneralUtils {

    // A static data member holding the cost of a walk without a packet
    public static final int COST_WALK_WITHOUT_PACKET = 10;
    // A static data member holding the cost of a walk with a packet
    public static final int COST_WALK_WITH_PACKET = 25;
    // A static data member holding if messages should be printed in the output
    public static final boolean PRINT = false;

    ////////////////
    // PERCEPTION //
    ////////////////

    /**
     * Check the perception of the agent
     * It performs the appropriate action when there is something in a cell in the perception of the agent.
     *  
     * @param agentState The current state of the agent
     */
    public static void checkPerception(AgentState agentState) {
        // Get the perception of the agent
        Perception agentPerception = agentState.getPerception();

        // Loop over the whole perception
        for (int x = 0; x <= agentPerception.getWidth(); x++) {
            for (int y = 0; y <= agentPerception.getHeight(); y++) {
                // Get the perception of the cell
                CellPerception cellPerception = agentPerception.getCellAt(x, y);

                // Check if the perception of the cell is null and continue with the next cell if so
                if(cellPerception == null) {
                    continue;
                }

                // Get the coordinate of the cell
                int cellX = cellPerception.getX();
                int cellY = cellPerception.getY();
                Coordinate cellCoordinate = new Coordinate(cellX, cellY);

                // Check if the position of the cell equals the position of the agent
                // It checks if the cell coordinate is (0,0)
                if(cellX == 0 && cellY == 0) {
                    continue;
                }

                // Check if the cell contains a charging station
                if (cellPerception.containsEnergyStation())
                    // Add a charging station
                    GeneralUtils.discoverChargingStation(agentState, cellCoordinate);
            }
        }
    }

    /**
     * Discover a charging station
     *
     * @param agentState The current state of the agent
     * @param chargingStationCoordinate The coordinates of the charging station
     */
    private static void discoverChargingStation(AgentState agentState, Coordinate chargingStationCoordinate) {
        // Get the list of charging stations
        ArrayList<ChargingStation> chargingStations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.CHARGING_STATIONS, ChargingStation.class);

        // Create a charging station
        ChargingStation chargingStation = new ChargingStation(chargingStationCoordinate);

        // Check if the charging station was already discovered and return if so
        if(chargingStations.contains(chargingStation)) return;

        // Add the charging station to the charging stations
        chargingStations.add(chargingStation);

        // Inform
        if(GeneralUtils.PRINT) {
            System.out.printf("%s: Discovered a new charging station (%s) [%s]\n", agentState.getName(), chargingStation, chargingStations.size());
        } 

        // Update memory
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.CHARGING_STATIONS, chargingStations, MemoryKeys.UPDATED_STATIONS, true));
    }

    /////////////////////////
    // INFORMATION SHARING //
    /////////////////////////

        /////////////
        // GENERAL //
        /////////////

    /**
     * Handle the communication of the charging stations between agents
     *
     * @param agentState The current state of the agent
     * @param agentCommunication The communication interface of the agent
     */
    public static void handleChargingStationsCommunication(AgentState agentState, AgentCommunication agentCommunication) {
        // Get if the charging stations were updated
        boolean chargingStationsUpdated = Boolean.TRUE.equals(MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.UPDATED_STATIONS, Boolean.class));

        // Check if the charging stations were updated
        if(chargingStationsUpdated) {
            // Share the charging stations
            GeneralUtils.shareChargingStations(agentState, agentCommunication);

            // Update the memory
            MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.UPDATED_STATIONS, false));
        }

        // Update the charging stations
        GeneralUtils.updateChargingStations(agentState, agentCommunication);
    }

    /**
     * Handle the communication of the graph between agents
     *
     * @param agentState The current state of the agent
     * @param agentCommunication The communication interface of the agent
     */
    public static void handleGraphCommunication(AgentState agentState, AgentCommunication agentCommunication) {
        // Get if the graph was updated
        boolean graphUpdated = Boolean.TRUE.equals(MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.UPDATED_GRAPH, Boolean.class));

        // Check if the graph was updated
        if(graphUpdated) {
            // Share the graph
            GeneralUtils.shareGraph(agentState, agentCommunication);

            // Update the memory
            MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.UPDATED_GRAPH, false));
        }

        // Update the graph
        GeneralUtils.updateGraph(agentState, agentCommunication);
    }

        ///////////
        // SHARE //
        ///////////

    /**
     * Share the charging stations with other agents
     *
     * @param agentState The current state of the agent
     * @param agentCommunication The communication interface of the agent
     */
    private static void shareChargingStations(AgentState agentState, AgentCommunication agentCommunication) {
        // Broadcast the charging stations
        CommunicationUtils.broadcastMemoryFragment(agentState, agentCommunication, MemoryKeys.CHARGING_STATIONS);
    }

    /**
     * Share the graph with other agents
     *
     * @param agentState The current state of the agent
     * @param agentCommunication The communication interface of the agent
     */
    private static void shareGraph(AgentState agentState, AgentCommunication agentCommunication) {
        // Send the graph to agents in the perception of the agent
        CommunicationUtils.sendMemoryFragment(agentState, agentCommunication, MemoryKeys.GRAPH);
    }

        ////////////
        // UPDATE //
        ////////////

    /**
     * Update the charging stations based on communication with other agents
     *
     * @param agentState The current state of the agent
     * @param agentCommunication The communication interface of the agent
     */
    private static void updateChargingStations(AgentState agentState, AgentCommunication agentCommunication) {    
        // Get the current charging stations
        ArrayList<ChargingStation> currentChargingStations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.CHARGING_STATIONS, ChargingStation.class);

        // Get the messages
        HashMap<String, ArrayList<ChargingStation>> chargingStationMessages = CommunicationUtils.getObjectListsFromMails(agentState, agentCommunication, MemoryKeys.CHARGING_STATIONS, ChargingStation.class);

        // Check if no charging station messages were received and return if so
        if (chargingStationMessages.size() == 0) {
            return;
        }

        // Loop over all charging station messages
        for(String sender: chargingStationMessages.keySet()) {
            // Check if sender equals the agent and continue with next charging station message if so
            if(sender.equals(agentState.getName())) {
                continue;
            }

            // Get the update charging stations of the charging station message
            ArrayList<ChargingStation> updatedChargingStations = chargingStationMessages.get(sender);

            // Loop over updated charging stations
            for(ChargingStation updatedChargingStation: updatedChargingStations) {
                // Check if the current charging stations do not contain the updated charging station
                if(!currentChargingStations.contains(updatedChargingStation)) {
                    // Add the updated charging station to the current charging stations
                    currentChargingStations.add(updatedChargingStation);

                    // Inform
                    if (GeneralUtils.PRINT) {
                        System.out.printf("%s: Added a new charging station from communication (%s) [%s]\n", agentState.getName(), updatedChargingStation, currentChargingStations.size());
                    }

                    continue;
                }            

                // Loop over current charging stations
                for(ChargingStation currentChargingStation: currentChargingStations) {
                    // Check if the current charging station equals the updated charging station
                    if(currentChargingStation.equals(updatedChargingStation)) {
                        // Check if the current charging station is not in use
                        // The current charging station should only be set to in use if it is not already
                        if(!currentChargingStation.isInUse()) {
                            currentChargingStation.setInUse(updatedChargingStation.isInUse());
                            currentChargingStation.setBatteryOfUser(updatedChargingStation.getBatteryOfUser());

                            // Inform
                            if(GeneralUtils.PRINT) {
                                System.out.printf("%s: Updated a known charging station from communication (%s)\n", agentState.getName(), currentChargingStation);
                            }
                        }
                    }
                }
            }
        }
        
        // Update the memory
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.CHARGING_STATIONS, currentChargingStations));
    }

    /**
     * Update the graph based on communication with other agents
     *
     * @param agentState The current state of the agent
     * @param agentCommunication The communication interface of the agent
     */
    private static void updateGraph(AgentState agentState, AgentCommunication agentCommunication) {
        // Get the messages
        HashMap<String, Graph> graphMessages = CommunicationUtils.getObjectsFromMails(agentCommunication, MemoryKeys.GRAPH, Graph.class);

        // Check if no graph messages were received and return if so
        if (graphMessages.size() == 0) {
            return;
        }

        // Loop over all graph messages
        for(String sender: graphMessages.keySet()) {
            // Check if sender equals the agent and continue with next graph message if so
            if(sender.equals(agentState.getName())) {
                continue;
            }

            // Get the update graph of the graph message
            Graph updatedGraph = graphMessages.get(sender);

            // Update the current graph
            GraphUtils.update(agentState, updatedGraph);

            // Inform
            if (GeneralUtils.PRINT) {
                System.out.printf("%s: Updated the graph from communication\n", agentState.getName());
            }
        }
    }


    ///////////
    // UTILS //
    ///////////
    
    /**
     * Has the agent reached the position?
     * 
     * @param agentState The current state of the agent
     * @param coordinate The coordinate of the position to reach
     * @return True is the agent is next to the position, otherwise false
     */
    public static boolean hasReachedPosition(AgentState agentState, Coordinate coordinate) {
        // Get the position of the agent and the position
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        int coordinateX = coordinate.getX();
        int coordinateY = coordinate.getY();

        // Calculate the difference between the positions
        int dX = Math.abs(agentX - coordinateX);
        int dY = Math.abs(agentY - coordinateY);

        // Return true if the distance is less than 1 for both axes, otherwise return false
        return (dX <= 1) && (dY <= 1);
    }
    
    
    /**
     * Does the graph contains a node with a given coordinate?
     * 
     * @param agentState The current state of the agent
     * @param coordinate The coordinate of the position to check
     * @return True is the position is in the graph, otherwise false
     */
    public static boolean positionInGraph(AgentState agentState, Coordinate coordinate) {
        // Get the graph
        Graph graph = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class);

        // Check if the graph is null and return false turn if so
        if(graph == null) {
            return false;
        }

        // Loop over the graph nodes
        for(Node node: graph.getMap().keySet()) {
            // Get the coordinate of the node
            Coordinate nodeCoordinate = node.getCoordinate();

            // Check if the coordinate of the node equals the coordinate of the position
            if(nodeCoordinate.equals(coordinate)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Calculate the Euclidean distance between two coordinates
     *
     * @param coordinate1 The first coordinate
     * @param coordinate2 The second coordinate
     * @return The euclidean distance between the two coordinates
     */
    public static double calculateEuclideanDistance(Coordinate coordinate1, Coordinate coordinate2) {
        // Get the positions of both coordinates
        int coordinate1X = coordinate1.getX();
        int coordinate1Y = coordinate1.getY();
        int coordinate2X = coordinate2.getX();
        int coordinate2Y = coordinate2.getY();

        // Calculate and return the distance
        return Math.sqrt(((coordinate2Y - coordinate1Y) * (coordinate2Y - coordinate1Y)) + ((coordinate2X - coordinate1X) * (coordinate2X - coordinate1X)));
    }

    /**
     * Has the agent enough battery to complete a task?
     *
     * @param agentState The current state of the agent
     * @param packet The packet the agent has to deliver
     * @param destination The destination to which the agent has to deliver the packet
     * @return True if enough energy to perform the task, false otherwise.
     */
    public static boolean hasEnoughBatteryToCompleteTask(AgentState agentState, Packet packet, Destination destination) {
        // Get the position of the agent
        int agentX = agentState.getX();
        int agentY = agentState.getY();

        // Get the position of the packet
        int packetX = packet.getCoordinate().getX();
        int packetY = packet.getCoordinate().getY();

        // Get the number of cells between the agent and the packet
        int numberOfCellsToPacket = Perception.distance(agentX, agentY, packetX, packetY);

        // Calculate the energy required to go to the packet
        double requiredEnergyToPacket = numberOfCellsToPacket * GeneralUtils.COST_WALK_WITHOUT_PACKET;

        // Get the position of the destination
        int destinationX = destination.getCoordinate().getX();
        int destinationY = destination.getCoordinate().getY();

        // Get the number of cells between the packet and the destination
        int numberOfCellsToDestination = Perception.distance(packetX, packetY, destinationX, destinationY);

        // Calculate the energy required to go to the destination
        double requiredEnergyToDestination = numberOfCellsToDestination * GeneralUtils.COST_WALK_WITH_PACKET;

        // Calculate total required energy
        // Hereby, a margin of 150 is added.
        double totalRequiredEnergy = requiredEnergyToPacket + requiredEnergyToDestination + 150;

        // Return true if the battery state of the agent is higher than the total required energy, otherwise return false
        return agentState.getBatteryState() > totalRequiredEnergy;
    }
}
