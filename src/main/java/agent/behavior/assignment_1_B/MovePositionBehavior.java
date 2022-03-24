package agent.behavior.assignment_1_B;

import java.util.Set;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import agent.behavior.assignment_1_A.utils.Task;
import agent.behavior.assignment_1_B.utils.MemoryKeys;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;

public class MovePositionBehavior extends Behavior {

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        // TODO Auto-generated method stub
    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        if(memoryFragments.contains(MemoryKeys.TASK)) {
            String taskString = agentState.getMemoryFragment(MemoryKeys.TASK);
            Task task = Task.fromJson(taskString);

            switch(task.getTaskState()) {
                case TO_DESTINATION:
                    Coordinate positionDestination = task.getDestination().getCoordinate();
                    
                    moveToPosition(agentState, agentAction, positionDestination);
                    
                    break;
                case TO_PACKET:
                    Coordinate positionPacket = task.getPacket().getCoordinate();

                    moveToPosition(agentState, agentAction, positionPacket);
                    
                    break;
                default:
                    agentAction.skip();
                    
                    break;
            }
      
            return;
        }

        agentAction.skip();
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Move towards a specific position
     * 
     * @param agentState The current state of the agent
     * @param agentAction Perform an action with the agent
     * @param position The position to move towards
     */
    private void moveToPosition(AgentState agentState, AgentAction agentAction, Coordinate position) {
        Perception agentPerception = agentState.getPerception();
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        int positionX = position.getX();
        int positionY = position.getY();

        int dX = positionX - agentX;
        int dY = positionY - agentY;
        int relativePositionX = (dX > 0) ? 1 : ((dX < 0) ? -1 : 0);
        int relativePositionY = (dY > 0) ? 1 : ((dY < 0) ? -1 : 0);
        CellPerception cellPerception = agentPerception.getCellPerceptionOnRelPos(relativePositionX, relativePositionY);


        if (cellPerception != null && cellPerception.isWalkable()) {
            int newPositionX = agentX + relativePositionX;
            int newPositionY = agentY + relativePositionY;
            
            agentAction.step(newPositionX, newPositionY);
            
            return;
        }

        agentAction.skip();
    }
}