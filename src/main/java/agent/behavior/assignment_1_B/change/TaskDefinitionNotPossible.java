package agent.behavior.assignment_1_B.change;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import util.MemoryKeys;
import util.target.Destination;
import util.target.Packet;

public class TaskDefinitionNotPossible extends BehaviorChange{

    private boolean taskDefinitionNotPossible = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {

        AgentState agentState = this.getAgentState();

        // Task definition is not possible
        taskDefinitionNotPossible = checkNoTaskDefinition(agentState);    
    }

    @Override
    public boolean isSatisfied() {
        return taskDefinitionNotPossible;
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Check if no task can be defined
     * 
     * @param agentState Current state of agent
     * @return True is task defintion is not possible
     */
    private boolean checkNoTaskDefinition(AgentState agentState) {
        // Check if not discovered packets
        ArrayList<Packet> discoveredPackets = getDiscoveredPackets(agentState);
        if(discoveredPackets.isEmpty()) return true;

        // Check if no discovered destinations
        ArrayList<Destination> discoveredDestinations = getDiscoveredDestinations(agentState);
        if(discoveredDestinations.isEmpty()) return true;

        // Check if there are no destinations for the packets in the discovered packets list
        ArrayList<Color> discoveredPacketColors = (ArrayList<Color>) discoveredPackets.stream().map(Packet::getColor).collect(Collectors.toList());
        ArrayList<Color> discoveredDestinationColors = (ArrayList<Color>) discoveredDestinations.stream().map(Destination::getColor).collect(Collectors.toList());
        if(discoveredPacketColors.stream().noneMatch(e -> discoveredDestinationColors.contains(e))) return true;

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
}
