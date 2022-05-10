package agent.behavior.assignment_2.changes;

import agent.AgentState;
import agent.behavior.BehaviorChange;

/**
 * A behavior change class that checks if the agent carries a packet.
 */
public class HasCarry extends BehaviorChange{

    private boolean hasCarry = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        // Retrieve the agent state
        AgentState agentState = this.getAgentState();

        // If the agent carries something, hasCarry is true
        hasCarry = agentState.hasCarry();
    }

    @Override
    public boolean isSatisfied() {
        return hasCarry;
    }
}  