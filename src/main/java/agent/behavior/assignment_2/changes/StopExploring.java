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

        Object exploringTurns2 = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.EXPLORING_TURNS, Integer.class);

        if (exploringTurns2 == null) return;

        stopExploring = ((int) exploringTurns2 ) >= 60;

        System.out.println(stopExploring);
    }

    @Override
    public boolean isSatisfied() {
        return stopExploring;
    }
}