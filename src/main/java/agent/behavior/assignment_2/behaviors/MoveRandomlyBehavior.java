package agent.behavior.assignment_2.behaviors;

import java.io.IOException;

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
        // Handle the charging stations
        GeneralUtils.handleChargingStations(agentState, agentCommunication);
    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) { 
        try {        
            // Check the perception of the agent
            GeneralUtils.checkPerception(agentState);

            // Build the graph
            GraphUtils.build(agentState);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Move the agent randomly
        ActionUtils.moveRandomly(agentState, agentAction);
    }
}