package agent.behavior.assignment_1_B.change;

import agent.AgentState;
import agent.behavior.BehaviorChange;

public class HasEnoughBattery extends BehaviorChange {
    private boolean hasEnoughBattery = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        AgentState agentState = this.getAgentState();
        hasEnoughBattery = agentState.getBatteryState() == 1000;
    }

    @Override
    public boolean isSatisfied() {
        return hasEnoughBattery;
    }

}

