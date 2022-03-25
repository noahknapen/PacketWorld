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
        AgentState agentState = this.getAgentState();

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
     * Check if the current state of the task is TO_DESTINATION
     * 
     * @param agentState The current state of the agent
     */
    private boolean toDestinationTask(AgentState agentState) {
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        if(memoryFragments.contains(MemoryKeys.TASK)) {
            String taskString = agentState.getMemoryFragment(MemoryKeys.TASK);
            Task task = Task.fromJson(taskString);

            return task.getTaskState() == TaskState.TO_DESTINATION;
        }

        return false;
    }

    /**
     * Check if the agent carries a packet
     * 
     * @param agentState The current state of the agent
     */
    private boolean agentCarriesPacket(AgentState agentState) {
        return agentState.hasCarry();
    }
}
