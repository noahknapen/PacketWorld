package agent.behavior.assignment_2.changes;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import util.assignments.general.GeneralUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.task.Task;

/**
 * A behavior change class that checks if the agent carries a packet
 */
public class HasCarryHasDestination extends BehaviorChange{

    private boolean hasCarry = false;
    private boolean hasDestination = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        // Retrieve the agent state
        AgentState agentState = this.getAgentState();

        // If the agent carries something, hasCarry is true
        hasCarry = agentState.hasCarry();

        Task task = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.TASK, Task.class);

        // Check if task has a destination
        if (task != null && task.getDestination() != null) {
            hasDestination = true;
        }
        else if (task != null){
            hasDestination = GeneralUtils.canReachDestination(agentState, task);
        }
    }

    @Override
    public boolean isSatisfied() {
        return hasCarry && hasDestination;
    }
}  