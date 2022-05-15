package agent.behavior.assignment_2.changes;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import util.assignments.general.GeneralUtils;

/**
 * A behavior change class that checks if a new task cannot be defined
 */
public class TaskDefinitionNotPossible extends BehaviorChange{

    private boolean taskDefinitionNotPossible = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        AgentState agentState = this.getAgentState();

        // Check if the task definition is not possible
        taskDefinitionNotPossible = GeneralUtils.checkNoTaskDefinition(agentState);
    }

    @Override
    public boolean isSatisfied() {
        return taskDefinitionNotPossible;
    }
}