package agent.behavior.assignment_2.behaviors;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.Coordinate;
import util.assignments.general.ActionUtils;

public class PutDownPacketBehavior extends Behavior {

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        // Put down the packet
        handlePutDown(agentState, agentAction);
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * A function to let the agent put down the packet
     * 
     * @param agentState The current state of the agent
     * @param agentAction Perform an action with the agent
     */
    private void handlePutDown(AgentState agentState, AgentAction agentAction) {
        Coordinate coordinate = null;

        ActionUtils.putDownPacket(agentAction, coordinate);
    }
}