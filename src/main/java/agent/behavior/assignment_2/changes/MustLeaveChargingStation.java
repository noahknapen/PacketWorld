package agent.behavior.assignment_2.changes;

import agent.behavior.BehaviorChange;

public class MustLeaveChargingStation extends BehaviorChange {
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
