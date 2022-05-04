package agent.behavior.assignment_2.behaviors;

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
        System.out.println("Comm1");
        // Communicate the charging stations with all the other agents
        GeneralUtils.handleChargingStations(agentState, agentCommunication);
        System.out.println("Comm2");
        // Communicate the destination locations with agents in perception
        GeneralUtils.handleDestinationLocations(agentState, agentCommunication);

    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        System.out.println("act1");
        // Check the perception of the agent
        GeneralUtils.checkPerception(agentState);
        System.out.println("act2");

        // Build the graph
        GraphUtils.build(agentState);
        System.out.println("act3");

        // Move the agent randomly
        ActionUtils.moveRandomly(agentState, agentAction);
    }
}