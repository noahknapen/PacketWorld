package util.assignments.general;

import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.util.List;

import agent.AgentCommunication;
import agent.AgentState;
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

import java.awt.*;
import java.io.IOException;

/**
 * A class that implements general functions
 */
public class GeneralUtils {
    
    /**
     * Check the perception of the agent
     *  
     * @param agentState The current state of the agent
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    public static void checkPerception(AgentState agentState) throws JsonParseException, JsonMappingException, IOException {
        // Get the perception of the agent
        Perception agentPerception = agentState.getPerception();

        // Get the memory fragments
        Task task = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.TASK, Task.class);
        ArrayList<Packet> discoveredPackets = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_PACKETS, Packet.class);
        ArrayList<Destination> discoveredDestinations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_DESTINATIONS, Destination.class);
        ArrayList<ChargingStation> discoveredChargingStations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_CHARGING_STATIONS, ChargingStation.class);

        // Loop over the whole perception
        for (int x = 0; x < agentPerception.getWidth(); x++) {
            for (int y = 0; y < agentPerception.getHeight(); y++) {
                CellPerception cellPerception = agentPerception.getCellAt(x,y);

                // Check if the cell is null and continue with the next cell if so
                if(cellPerception == null) continue;

                // Get the coordinate of the cell
                Coordinate cellCoordinate = new Coordinate(cellPerception.getX(), cellPerception.getY());

                // Check if the cell contains a packet
                if(cellPerception.containsPacket()) {
                    // Get the color of the packet
                    Color packetColor = cellPerception.getRepOfType(PacketRep.class).getColor();
                    int packetRgbColor = packetColor.getRGB();
                    
                    // Create the corresponding packet
                    Packet packet= new Packet(cellCoordinate, packetRgbColor);

                    // Check if the packet was already discovered and continue with the next cell if so
                    if(discoveredPackets.contains(packet)) continue;

                    // Check if packet is currently handled and continue with the next cell if so
                    // Because it should not be added to list again
                    if(task != null && task.getPacket().isPresent() && task.getPacket().get().equals(packet)) continue;

                    // Add the packet to the list of discovered packets 
                    discoveredPackets.add(packet);

                    // Inform
                    String message = String.format("%s: Discovered a new packet (%s) [%s]", agentState.getName(), packet, discoveredPackets.size());
                    System.out.println(message);
                }


                 // Check if the cell contains a destination
                 if(cellPerception.containsAnyDestination()) {
                    // Get the color of the destination
                    Color destinationColor = cellPerception.getRepOfType(DestinationRep.class).getColor();
                    int destinationRgbColor = destinationColor.getRGB();
                    
                    // Create the corresponding destination
                    Destination destination= new Destination(cellCoordinate, destinationRgbColor);

                    // Check if the destination was already discovered and continue with next cell if so
                    if(discoveredDestinations.contains(destination)) continue;

                    // Add the destination to the list of discovered destinations
                    discoveredDestinations.add(destination);

                    // Inform
                    String message = String.format("%s: Discovered a new destination (%s) [%s]", agentState.getName(), destination, discoveredDestinations.size());
                    System.out.println(message);
                }

                // Check if the cell contains a charging station
                if(cellPerception.containsEnergyStation()) {                    
                    // Create the corresponding chargin station
                    ChargingStation chargingStation = new ChargingStation(cellCoordinate);

                    // Check if the charging station was already discovered and continue with next cell if so
                    if(discoveredChargingStations.contains(chargingStation)) continue;

                    // Add the charging station to the list of discovered charging stations
                    discoveredChargingStations.add(chargingStation);

                    // Inform
                    String message = String.format("%s: Discovered a new charging station (%s) [%s]", agentState.getName(), chargingStation, discoveredChargingStations.size());
                    System.out.println(message);
                }
            }
        }

        // Update the memory
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.DISCOVERED_PACKETS, discoveredPackets, MemoryKeys.DISCOVERED_DESTINATIONS, discoveredDestinations, MemoryKeys.DISCOVERED_CHARGING_STATIONS, discoveredChargingStations));
    }

    /**
     * Handle the charging stations
     * 
     * @param agentState The current state of the agent
     * @param agentCommunication Perform communication with the agent
     * @throws IOException
     */
    public static void handleChargingStations(AgentState agentState, AgentCommunication agentCommunication) throws IOException {
        // Share charging station information
        shareChargingStationsInformation(agentState, agentCommunication);

        // Update charging station information
        updateChargingStationsInformation(agentState, agentCommunication);
    }

    /**
     * Share own charging station information
     * 
     * @param agentState The current state of the agent
     * @param agentCommunication Perform communication with the agent
     * @throws JsonProcessingException
     */
    private static void shareChargingStationsInformation(AgentState agentState, AgentCommunication agentCommunication) throws JsonProcessingException {
        CommunicationUtils.broadcastMemoryFragment(agentState, agentCommunication, MemoryKeys.DISCOVERED_CHARGING_STATIONS);
    }

    /**
     * Update own charging station information by means of message exchange with other agents
     * 
     * @param agentState The current state of the agent
     * @param agentCommunication Perform communication with the agent
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    private static void updateChargingStationsInformation(AgentState agentState, AgentCommunication agentCommunication) throws JsonParseException, JsonMappingException, IOException {
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
                String message = String.format("%s: Add a new charging station from communication (%s) [%s]", agentState.getName(), updatedChargingStation, currentChargingStations.size());
                System.out.println(message);

                continue;
            }            

            // Loop over current charging stations
            for(ChargingStation currentChargingStation: currentChargingStations) {
                // Check if charging stations correspond
                if(currentChargingStation.equals(updatedChargingStation)) {
                    // Update the current chargint station is needed
                    if(!currentChargingStation.isInUse() && updatedChargingStation.isInUse()) currentChargingStation.setInUse(true);
                    if(!currentChargingStation.getBatteryOfUser().isPresent() && updatedChargingStation.getBatteryOfUser().isPresent()) currentChargingStation.setBatteryOfUser(updatedChargingStation.getBatteryOfUser());
                
                    // Inform
                    String message = String.format("%s: Updated a known charging station from communication (%s)", agentState.getName(), currentChargingStation);
                    System.out.println(message);
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
                
                // Check if the positions correpond
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
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    public static boolean positionInGraph(AgentState agentState, Coordinate coordinate) throws JsonParseException, JsonMappingException, IOException {
        // Get the graph
        Graph graph = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class);

        // Check if the graph is null and return false turn if so
        if(graph == null)
            return false;

        // Get the graph map
        Map<Node, List<Node>> map = graph.getMap();

        // Loop over graph nodes
        for(Node node: map.keySet()) {
            // Get the position of the node
            Coordinate nodeCoordinate = node.getCoordinate();

            // Check if coordinates correspond
            if(nodeCoordinate.equals(coordinate)) {
                return true;
            }
        }

        return false;
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
        double distance = Math.sqrt(((coordinate2Y - coordinate1Y) * (coordinate2Y - coordinate1Y)) + ((coordinate2X - coordinate1X) * (coordinate2X - coordinate1X)));

        return distance;
    } 
}
