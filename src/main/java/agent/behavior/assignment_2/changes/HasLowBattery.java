package agent.behavior.assignment_2.changes;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import util.assignments.general.GeneralUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.ChargingStation;

import java.io.IOException;
import java.util.ArrayList;


public class HasLowBattery extends BehaviorChange {
    private boolean hasLowBattery = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        AgentState agentState = this.getAgentState();
        System.out.println("Here");
        hasLowBattery = agentState.getBatteryState() < 350;
    }

    @Override
    public boolean isSatisfied() {
        return hasLowBattery;
    }

}