package agent.behavior.assignment_2.behaviors;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.Coordinate;
import util.assignments.general.GeneralUtils;
import util.assignments.general.ActionUtils;

public class MoveToTargetBehavior extends Behavior {

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        // Check the perception of the agent
        GeneralUtils.checkPerception(agentState);
        
        // Move the agent to the target
        handleMove(agentState, agentAction);
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * A function to let the agent move
     * 
     * @param agentState The current state of the agent
     * @param agentAction Perform an action with the agent
     */
    private void handleMove(AgentState agentState, AgentAction agentAction) {
        Coordinate coordinate = null;

        ActionUtils.moveToCoordinate(agentAction, coordinate);
    }
}