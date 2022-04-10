package util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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

                        System.out.println("[MoveRandomBehavior]{checkPerception} New destination discovered (" + discoveredDestinations.size() + ")");
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

                        System.out.println("[MoveRandomBehavior]{checkPerception} New packet discovered (" + discoveredPackets.size() + ")");
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
                        System.out.println(String.format("[MoveRandomBehavior]{checkPerception} Agent on location (%d,%d) has discovered a new battery station (" + discoveredBatteryStations.size() + ")", agentState.getX(), agentState.getY()));
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
