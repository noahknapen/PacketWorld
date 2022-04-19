package util.assignments.general;

import java.util.ArrayList;
import java.util.Map;

import agent.AgentState;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;
import environment.world.destination.DestinationRep;
import environment.world.packet.PacketRep;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.Destination;
import util.assignments.targets.Packet;
import util.assignments.task.Task;

import java.awt.*;

/**
 * A class that implements general functions
 */
public class GeneralUtils {
    
    /**
     * Check the perception of the agent
     *  
     * @param agentState The current state of the agent
     */
    public static void checkPerception(AgentState agentState) {
        // Get the perception of the agent
        Perception agentPerception = agentState.getPerception();

        // Get the memory fragments
        Task task = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.TASK, Task.class);
        ArrayList<Packet> discoveredPackets = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_PACKETS, Packet.class);
        ArrayList<Destination> discoveredDestinations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_DESTINATIONS, Destination.class);

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
                    String message = String.format("%s: Discovered a new packet (%d)", agentState.getName(), discoveredPackets.size());
                    System.out.println(message);
                }


                 // Check if the cell contains a destination
                 if(cellPerception.containsAnyDestination()) {
                    // Get the color of the destination
                    Color destinationColor = cellPerception.getRepOfType(DestinationRep.class).getColor();
                    int destinationRgbColor = destinationColor.getRGB();
                    
                    // Create the corresponding destination
                    Destination destination= new Destination(cellCoordinate, destinationRgbColor);

                    // Check if the destination was already discoverd and continue with next cell if so
                    if(discoveredDestinations.contains(destination)) continue;

                    // Add the destination to the list of discovered destinations
                    discoveredDestinations.add(destination);

                    // Inform
                    String message = String.format("%s: Discovered a new destination (%d)", agentState.getName(), discoveredDestinations.size());
                    System.out.println(message);
                }
            }
        }

        // Update the memory
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.DISCOVERED_PACKETS, discoveredPackets, MemoryKeys.DISCOVERED_DESTINATIONS, discoveredDestinations));        
    }
}
