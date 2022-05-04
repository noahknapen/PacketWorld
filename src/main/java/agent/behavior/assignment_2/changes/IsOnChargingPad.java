package agent.behavior.assignment_2.changes;

import agent.behavior.BehaviorChange;
import util.assignments.general.GeneralUtils;


public class IsOnChargingPad extends BehaviorChange {
    private boolean isOnChargingPad = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        // True if the agent is on the charging pad, false otherwise
        isOnChargingPad = GeneralUtils.isOnChargingPad(this.getAgentState());
    }

    @Override
    public boolean isSatisfied() {
        return isOnChargingPad;
    }

}