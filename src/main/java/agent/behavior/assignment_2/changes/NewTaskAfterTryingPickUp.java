package agent.behavior.assignment_2.changes;

import java.awt.Color;
import java.util.*;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import environment.Perception;
import util.assignments.comparators.PacketComparator;
import util.assignments.general.GeneralUtils;
import util.assignments.graph.Graph;
import util.assignments.graph.GraphUtils;
import util.assignments.graph.Node;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.Destination;
import util.assignments.targets.Packet;
import util.assignments.task.Task;

/**
 * A behavior change class that checks if a new task can be defined
 */
public class NewTaskAfterTryingPickUp extends BehaviorChange{

    private boolean newTaskAfterTryingPickUp = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        AgentState agentState = this.getAgentState();

        // Handle the possible task definition
        if (agentState.hasCarry())
            newTaskAfterTryingPickUp = false;
        else
            newTaskAfterTryingPickUp = GeneralUtils.checkTaskDefinition(agentState);
    }

    @Override
    public boolean isSatisfied() {
        return newTaskAfterTryingPickUp;
    }

    /////////////
    // METHODS //
    /////////////

}