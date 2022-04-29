package agent.behavior.assignment_2.changes;

import agent.AgentState;
import agent.behavior.BehaviorChange;

public class HasEnoughBattery extends BehaviorChange {
    private boolean hasEnoughBattery = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        hasEnoughBattery =  this.getAgentState().getBatteryState() >= 950;
    }

    @Override
    public boolean isSatisfied() {
        return hasEnoughBattery;
    }

}
