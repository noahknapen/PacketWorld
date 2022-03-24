package agent.behavior.assignment_1_B;

import java.util.Set;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import agent.behavior.assignment_1_A.utils.Task;
import agent.behavior.assignment_1_B.utils.MemoryKeys;
import environment.Coordinate;

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
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        if(memoryFragments.contains(MemoryKeys.TASK)) {
            String taskString = agentState.getMemoryFragment(MemoryKeys.TASK);
            Task task = Task.fromJson(taskString);
            Coordinate position = task.getDestination().getCoordinate();
            
            putDownPacket(agentAction, position);
      
            return;
        }

        agentAction.skip();
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Put down a packet at a certain position
     * 
     * @param agentAction Perfom an action with the agent
     * @param position The position of the destination
     */
    private void putDownPacket(AgentAction agentAction, Coordinate position) {
        int positionX = position.getX();
        int positionY = position.getY();
        
        agentAction.putPacket(positionX, positionY);
    }
}