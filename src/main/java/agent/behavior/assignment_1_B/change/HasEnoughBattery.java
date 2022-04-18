package agent.behavior.assignment_1_B.change;

import agent.behavior.BehaviorChange;

public class HasEnoughBattery extends BehaviorChange {
    private boolean hasEnoughBattery = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        hasEnoughBattery =  this.getAgentState().getBatteryState() >= 900;
    }

    @Override
    public boolean isSatisfied() {
        return hasEnoughBattery;
    }

}

