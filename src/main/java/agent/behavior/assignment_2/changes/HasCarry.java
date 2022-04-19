package agent.behavior.assignment_2.changes;

import agent.AgentState;
import agent.behavior.BehaviorChange;

public class HasCarry extends BehaviorChange{

    private boolean hasCarry = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        AgentState agentState = this.getAgentState();

        hasCarry = agentState.hasCarry();
    }

    @Override
    public boolean isSatisfied() {
        return hasCarry;
    }
}  