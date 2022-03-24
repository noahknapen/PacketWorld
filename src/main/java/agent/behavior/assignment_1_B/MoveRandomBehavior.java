package agent.behavior.assignment_1_B;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;

public class MoveRandomBehavior extends Behavior {

    final ArrayList<Coordinate> RELATIVE_POSITIONS = new ArrayList<Coordinate>(List.of(
        new Coordinate(1, 1), 
        new Coordinate(-1, -1),
        new Coordinate(1, 0), 
        new Coordinate(-1, 0),
        new Coordinate(0, 1), 
        new Coordinate(0, -1),
        new Coordinate(1, -1), 
        new Coordinate(-1, 1)
    ));

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        // TODO Auto-generated method stub
    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        moveRandom(agentState, agentAction);
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Move randomly
     * 
     * @param agentState The current state of the agent
     * @param agentAction Perform an action with the agent
     */
    private void moveRandom(AgentState agentState, AgentAction agentAction) {
        Perception agentPerception = agentState.getPerception();
        int agentX = agentState.getX();
        int agentY = agentState.getY();

        Collections.shuffle(RELATIVE_POSITIONS);

        for (Coordinate relativePosition : RELATIVE_POSITIONS) {
            int relativePositionX = relativePosition.getX();
            int relativePositionY = relativePosition.getY();
            CellPerception cellPerception = agentPerception.getCellPerceptionOnRelPos(relativePositionX, relativePositionY);

            if (cellPerception != null && cellPerception.isWalkable()) {
                int newPositionX = agentX + relativePositionX;
                int newPositionY = agentY + relativePositionY;
                
                agentAction.step(newPositionX, newPositionY);
                
                return;
            }
        }

        agentAction.skip();
    }
}