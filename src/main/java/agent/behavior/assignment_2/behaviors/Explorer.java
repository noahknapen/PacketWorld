package agent.behavior.assignment_2.behaviors;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import util.assignments.general.GeneralUtils;
import util.assignments.graph.GraphUtils;
import util.assignments.general.ActionUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;

import java.util.Map;

/**
 * A behavior where the agent moves randomly
 */
public class Explorer extends Behavior {

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        GeneralUtils.handleChargingStationsCommunication(agentState, agentCommunication);

        // Communicate the priority tasks with agents in perception
        GeneralUtils.handlePriorityTaskCommunication(agentState, agentCommunication);

        GeneralUtils.handleGraphCommunication(agentState, agentCommunication);
    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        // Check the perception of the agent
        // GeneralUtils.checkPerception(agentState);

        // Build the graph
        GraphUtils.build(agentState);

        // Move the agent randomly
        ActionUtils.moveRandomly(agentState, agentAction);

        // Update memory
        // Retrieve the amount of turns exploring

        Object exploringTurns = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.EXPLORING_TURNS, Integer.class);

        if (exploringTurns == null) MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.EXPLORING_TURNS, 1));
        else MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.EXPLORING_TURNS, (int) exploringTurns + 1));
    }
}