package agent.behavior.assignment_1_B.change;

import agent.AgentState;
import agent.behavior.BehaviorChange;

public class HasLowBattery extends BehaviorChange {
    private boolean hasLowBattery = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        AgentState agentState = this.getAgentState();
        hasLowBattery = agentState.getBatteryState() < 250;
    }

    @Override
    public boolean isSatisfied() {
        return hasLowBattery;
    }

}
