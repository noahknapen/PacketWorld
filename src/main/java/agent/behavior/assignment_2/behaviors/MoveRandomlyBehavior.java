package agent.behavior.assignment_2.behaviors;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import util.assignments.general.CommunicationUtils;
import util.assignments.general.GeneralUtils;
import util.assignments.graph.GraphUtils;
import util.assignments.general.ActionUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;

import java.util.Map;

/**
 * A behavior where the agent moves randomly
 */
public class MoveRandomlyBehavior extends Behavior {

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        // Communicate the charging stations with all the other agents
        GeneralUtils.handleChargingStationsCommunication(agentState, agentCommunication);

        // Communicate the priority tasks with agents in perception
        GeneralUtils.handlePriorityTaskCommunication(agentState, agentCommunication);

        // Communicate the graph with agents in perception  
        GeneralUtils.handleGraphCommunication(agentState, agentCommunication);

        // Check if multiple messages were sent and ignore
        checkForEmergencyNotificationsAndIgnore(agentState, agentCommunication);
    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        // Check the perception of the agent
        //GeneralUtils.checkPerception(agentState);

        // Build the graph
        GraphUtils.build(agentState);

        // Move the agent randomly
        ActionUtils.moveRandomly(agentState, agentAction);

        // Update behavior
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.EMERGENCY, false));
    }

    /**
     * To avoid the multiple usage of an emergency message, all the emergency messages will be erased in the
     * moveRandomBehavior. This is due to the fact that the agent came from the charging station due to this message
     * but it is possible that the message was sent 2 or more times. So the agent removes the message if it isn't
     * necessary anymore.
     *
     * @param agentState: The state of the agent
     * @param agentCommunication: The interface for communication.
     */
    private void checkForEmergencyNotificationsAndIgnore(AgentState agentState, AgentCommunication agentCommunication) {
        // Ensure that there are messages before continuing
        if (agentCommunication.getNbMessages() == 0) return;

        // Retrieve the messages and delete them
        CommunicationUtils.getObjectsFromMails(agentCommunication, "boolean", Boolean.class);
    }
}