package agent.behavior.assignment_1_B.change;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import agent.behavior.assignment_1_A.utils.Packet;
import agent.behavior.assignment_1_B.utils.MemoryKeys;

public class NoTasks extends BehaviorChange{

    private boolean noTasks = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        AgentState agentState = this.getAgentState();

        noTasks = noPacketsToBeDelivered(agentState);    
    }

    @Override
    public boolean isSatisfied() {
        return noTasks;
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Check if there are no packets to be delivered
     * 
     * @param agentState The current state of the agent
     */
    private boolean noPacketsToBeDelivered(AgentState agentState) {
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        if(memoryFragments.contains(MemoryKeys.DISCOVERED_PACKETS)) {
            Gson gson = new Gson();
            String discoveredPacketsString = agentState.getMemoryFragment(MemoryKeys.DISCOVERED_PACKETS);
            ArrayList<Packet> discoveredPackets = gson.fromJson(discoveredPacketsString, new TypeToken<List<Packet>>(){}.getType());

            return discoveredPackets.isEmpty();
        }

        return false;
    }   
}
