package agent.behavior.assignment_2.behaviors;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.Coordinate;
import util.assignments.general.ActionUtils;

public class PickUpPacketBehavior extends Behavior {

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        // Pick up the packet
        handlePickUp(agentState, agentAction);
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * A function to let the agent pick up the packet
     * 
     * @param agentState The current state of the agent
     * @param agentAction Perform an action with the agent
     */
    private void handlePickUp(AgentState agentState, AgentAction agentAction) {
        Coordinate coordinate = null;

        ActionUtils.pickUpPacket(agentAction, coordinate);
    }
}