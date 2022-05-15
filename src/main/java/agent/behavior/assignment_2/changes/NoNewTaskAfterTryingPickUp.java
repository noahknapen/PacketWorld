package agent.behavior.assignment_2.changes;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import util.assignments.general.GeneralUtils;

/**
 * A behavior change class that checks if a new task cannot be defined
 */
public class NoNewTaskAfterTryingPickUp extends BehaviorChange{

    private boolean noNewTaskAfterTryingPickUp = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        AgentState agentState = this.getAgentState();

        // Check if the task definition is not possible
        if (agentState.hasCarry())
            noNewTaskAfterTryingPickUp = false;
        else 
            noNewTaskAfterTryingPickUp = GeneralUtils.checkNoTaskDefinition(agentState);
    }

    @Override
    public boolean isSatisfied() {
        return noNewTaskAfterTryingPickUp;
    }

    /////////////
    // METHODS //
    /////////////

}
