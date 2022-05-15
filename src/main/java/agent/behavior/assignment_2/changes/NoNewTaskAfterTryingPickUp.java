package agent.behavior.assignment_2.changes;

import java.awt.Color;
import java.util.ArrayList;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import util.assignments.general.GeneralUtils;
import util.assignments.graph.Graph;
import util.assignments.graph.GraphUtils;
import util.assignments.graph.Node;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.Destination;
import util.assignments.targets.Packet;

/**
 * A behavior change class that checks if a new task cannot be defined
 */
public class NoNewTaskAfterTryingPickUp extends BehaviorChange{

    private boolean noNewTaskAfterTryingPickUp = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        AgentState agentState = this.getAgentState();

        // Check if the task definition is not possible
        if (agentState.hasCarry())
            noNewTaskAfterTryingPickUp = false;
        else 
            noNewTaskAfterTryingPickUp = GeneralUtils.checkNoTaskDefinition(agentState);
    }

    @Override
    public boolean isSatisfied() {
        return noNewTaskAfterTryingPickUp;
    }

    /////////////
    // METHODS //
    /////////////

}
