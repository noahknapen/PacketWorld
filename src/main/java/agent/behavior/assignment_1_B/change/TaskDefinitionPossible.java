package agent.behavior.assignment_1_B.change;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import agent.behavior.assignment_1_A.utils.Destination;
import agent.behavior.assignment_1_A.utils.Packet;
import agent.behavior.assignment_1_A.utils.PacketComparator;
import agent.behavior.assignment_1_A.utils.Task;
import agent.behavior.assignment_1_A.utils.TaskState;
import agent.behavior.assignment_1_B.utils.MemoryKeys;

public class TaskDefinitionPossible extends BehaviorChange{

    private boolean taskDefinitionPossible = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        System.out.println("[TasksAvailable] updateChange");

        AgentState agentState = this.getAgentState();

        taskDefinitionPossible = checkTaskDefinition(agentState);    
    }

    @Override
    public boolean isSatisfied() {
        return taskDefinitionPossible;
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Check if a task can be defined, and do so if yes
     * 
     * @param agentState The current state of the agent
     * @param True if task definition is possible
     */
    private boolean checkTaskDefinition(AgentState agentState) {
        ArrayList<Packet> discoveredPackets = getDiscoveredPackets(agentState);
        ArrayList<Destination> discoveredDestinations = getDiscoveredDestinations(agentState);
    
        // Sort the packets in the discovered packets list
        PacketComparator packComparator = new PacketComparator(agentState, discoveredDestinations);
        Collections.sort(discoveredPackets, packComparator);

        // Loop through the sorted list of packets to be delivered
        for(int i = 0; i < discoveredPackets.size(); i++) {
            // Define a candidate packet
            Packet candidatepacket= discoveredPackets.get(i);
            Color candidatePackColor = candidatepacket.getColor();
            
            // Loop through the list of discovered destinations
            for(int j = 0; j < discoveredDestinations.size(); j++) {
                Color destinationColor = discoveredDestinations.get(j).getColor();
                
                // Check if a corresponding (color) destination was already discovered
                if(candidatePackColor.equals(destinationColor)) {
                    Destination destination = discoveredDestinations.get(j);

                    // Remove the packet from the list
                    candidatepacket= discoveredPackets.remove(i);

                    // Define the task
                    Task task = new Task(candidatepacket, destination, TaskState.TO_PACKET);

                    // Update the memory
                    updateTaskMemory(agentState, discoveredPackets, task);

                    return true;
                }
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
     * Update memory of agent
     * 
     * @param agentState Current state of the agent
     * @param task Current task
     * @param discoveredPackets List of discovered packets
     */
    private void updateTaskMemory(AgentState agentState, ArrayList<Packet> discoveredPackets, Task task) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        // Remove discovered packets from memory
        if(memoryFragments.contains(MemoryKeys.DISCOVERED_PACKETS)) agentState.removeMemoryFragment(MemoryKeys.DISCOVERED_PACKETS);

        // Add updated discovered packets and updated task to memory
        Gson gson = new Gson();
        String discoveredPacketsString = gson.toJson(discoveredPackets);
        String taskString = task.toJson();
        agentState.addMemoryFragment(MemoryKeys.DISCOVERED_PACKETS, discoveredPacketsString);
        agentState.addMemoryFragment(MemoryKeys.TASK, taskString);
        
        System.out.println("[TaskDefinitionPossible]{updateTaskMemory} Discovered packets and task updated in memory");
    }
}
