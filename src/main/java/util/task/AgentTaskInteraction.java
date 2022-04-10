package util.task;

import java.util.ArrayList;
import java.util.Set;

import com.google.gson.Gson;

import agent.AgentState;
import util.MemoryKeys;
import util.targets.Target;

public class AgentTaskInteraction {
    
    /**
     * Retrieve task from memory
     * 
     * @param agentState Current state of agent
     * @return Task
     */
    public static Task getTask(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        // Check if task exists in memory
        if(memoryFragments.contains(MemoryKeys.TASK)) {
            // Retrieve task
            String taskString = agentState.getMemoryFragment(MemoryKeys.TASK);
            return Task.fromJson(taskString);
        }
        else return null;
    } 

    /**
     * Update task memory of agent
     * 
     * @param agentState Current state of the agent
     * @param discoveredPackets List of discovered packets
     * @param discoveredDestinations List of discovered destinations
     * @param discoveredBatteryStations List of discovered battery stations
     * @param nonBroadCastedBatteryStations List of non-broadcasted battery stations
     */
    public static void updateTaskMemory(AgentState agentState, ArrayList<Target> discoveredPackets, ArrayList<Target> discoveredDestinations, ArrayList<Target> discoveredBatteryStations, ArrayList<Target> nonBroadcastedBatteryStations) 
    {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();
        Gson gson = new Gson();

        if (discoveredPackets != null)
        {
            if(memoryFragments.contains(MemoryKeys.DISCOVERED_PACKETS)) 
                agentState.removeMemoryFragment(MemoryKeys.DISCOVERED_PACKETS);
         
            String discoveredPacketsString = gson.toJson(discoveredPackets);
            agentState.addMemoryFragment(MemoryKeys.DISCOVERED_PACKETS, discoveredPacketsString);
        }

        if (discoveredDestinations != null) 
        {
            if(memoryFragments.contains(MemoryKeys.DISCOVERED_DESTINATIONS)) 
                agentState.removeMemoryFragment(MemoryKeys.DISCOVERED_DESTINATIONS);

            String discoveredDestinationsString = gson.toJson(discoveredDestinations);
            agentState.addMemoryFragment(MemoryKeys.DISCOVERED_DESTINATIONS, discoveredDestinationsString);
        }

        if (discoveredBatteryStations != null)
        {
            if (memoryFragments.contains(MemoryKeys.DISCOVERED_BATTERY_STATIONS))
                agentState.removeMemoryFragment(MemoryKeys.DISCOVERED_BATTERY_STATIONS);

            String discoveredBatteryStationsString = gson.toJson(discoveredBatteryStations);
            agentState.addMemoryFragment(MemoryKeys.DISCOVERED_BATTERY_STATIONS, discoveredBatteryStationsString);
        }

        if (nonBroadcastedBatteryStations != null) 
        {
            if (memoryFragments.contains(MemoryKeys.NON_BROADCASTED_BATTERY_STATIONS))
                agentState.removeMemoryFragment(MemoryKeys.NON_BROADCASTED_BATTERY_STATIONS);

            String nonBroadcastedBatteryStationsString = gson.toJson(nonBroadcastedBatteryStations);
            agentState.addMemoryFragment(MemoryKeys.NON_BROADCASTED_BATTERY_STATIONS, nonBroadcastedBatteryStationsString);

        }
    }

}
