package agent.behavior.assignment_3.behaviors;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import util.assignments.general.GeneralUtils;
import util.assignments.graph.GraphUtils;
import util.assignments.general.ActionUtils;

/**
 * A behavior where the agent moves randomly
 */
public class MoveRandomlyBehavior extends Behavior {

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        // Communicate the charging stations with all the other agents
        GeneralUtils.handleChargingStationsCommunication(agentState, agentCommunication);

        // Communicate the destination locations with agents in perception
        GeneralUtils.handleDestinationsCommunication(agentState, agentCommunication);

    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        // Check the perception of the agent
        GeneralUtils.checkPerception(agentState);

        // Build the graph
        GraphUtils.build(agentState);

        // Move the agent randomly
        ActionUtils.moveRandomly(agentState, agentAction);
    }
}