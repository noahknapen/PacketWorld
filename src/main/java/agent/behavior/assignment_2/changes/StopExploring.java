package agent.behavior.assignment_2.changes;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;

import java.util.Map;

public class StopExploring extends BehaviorChange{

    private boolean stopExploring;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        // Retrieve the agent state
        AgentState agentState = this.getAgentState();

        Object batteryState = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.BATTERY_STATE, Integer.class);
        Object exploringTurns = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.EXPLORING_TURNS, Integer.class);

        if (exploringTurns == null) return;

        if (batteryState == null) batteryState = agentState.getBatteryState();
        else if (((int) batteryState) == agentState.getBatteryState()) {
            stopExploring = true;
            return;
        }
        else batteryState = agentState.getBatteryState();

        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.BATTERY_STATE, batteryState));


        stopExploring = ((int) exploringTurns ) >= 60;
    }

    @Override
    public boolean isSatisfied() {
        return stopExploring;
    }
}