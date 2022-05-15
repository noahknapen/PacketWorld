package agent.behavior.assignment_2.changes;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import util.assignments.general.GeneralUtils;

/**
 * A behavior change class that checks if a new task can be defined
 */
public class TaskDefinitionPossible extends BehaviorChange{

    private boolean taskDefinitionPossible = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        AgentState agentState = this.getAgentState();

        taskDefinitionPossible = GeneralUtils.checkTaskDefinition(agentState);
    }

    @Override
    public boolean isSatisfied() {
        return taskDefinitionPossible;
    }
}