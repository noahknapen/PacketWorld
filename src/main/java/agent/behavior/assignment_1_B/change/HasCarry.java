package agent.behavior.assignment_1_B.change;

import java.util.Set;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import agent.behavior.assignment_1_A.utils.Task;
import agent.behavior.assignment_1_A.utils.TaskState;
import agent.behavior.assignment_1_B.utils.MemoryKeys;

public class HasCarry extends BehaviorChange{

    private boolean hasCarry = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        System.out.println("[HasCarry]{updateChange}");

        AgentState agentState = this.getAgentState();

        // Has carry if task state is TO_DESTINATION and if agent carries a packet
        hasCarry = toDestinationTask(agentState) && agentCarriesPacket(agentState);
    }

    @Override
    public boolean isSatisfied() {
        return hasCarry;
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Check if current state of task is TO_DESTINATION
     * 
     * @param agentState Current state of agent
     * @return True if task state is TO_DESTINATION
     */
    private boolean toDestinationTask(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        // Check if task exists in memory
        if(memoryFragments.contains(MemoryKeys.TASK)) {
            // Retrieve task
            String taskString = agentState.getMemoryFragment(MemoryKeys.TASK);
            Task task = Task.fromJson(taskString);

            // Check if state is TO_DESTINATION
            return task.getState() == TaskState.TO_DESTINATION;
        }
        else return false;
    }

    /**
     * Check if agent carries a packet
     * 
     * @param agentState Current state of agent
     * @return True if agent has carry
     */
    private boolean agentCarriesPacket(AgentState agentState) {
        return agentState.hasCarry();
    }
}
