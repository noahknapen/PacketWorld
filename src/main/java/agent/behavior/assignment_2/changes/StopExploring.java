package agent.behavior.assignment_2.changes;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;

public class StopExploring extends BehaviorChange{

    private boolean stopExploring;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        // Retrieve the agent state
        AgentState agentState = this.getAgentState();

        Object exploringTurns = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.EXPLORING_TURNS, Integer.class);

        if (exploringTurns == null) return;

        stopExploring = ((int) exploringTurns ) >= 0;
    }

    @Override
    public boolean isSatisfied() {
        return stopExploring;
    }
}