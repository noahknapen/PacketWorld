package agent.behavior.assignment_1_B.change;

import java.util.Set;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import agent.behavior.assignment_1_A.utils.Task;
import agent.behavior.assignment_1_A.utils.TaskState;
import agent.behavior.assignment_1_B.utils.MemoryKeys;

public class ReadyToPickUp extends BehaviorChange{

    private boolean readyToPickUp = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        AgentState agentState = this.getAgentState();
        
        readyToPickUp = toPacketTask(agentState) && positionReached(agentState);      
    }

    @Override
    public boolean isSatisfied() {
        return readyToPickUp;
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Check if the current state of the task is TO_PACKET
     * 
     * @param agentState The current state of the agent
     */
    private boolean toPacketTask(AgentState agentState) {
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        if(memoryFragments.contains(MemoryKeys.TASK)) {
            String taskString = agentState.getMemoryFragment(MemoryKeys.TASK);
            Task task = Task.fromJson(taskString);

            return task.getTaskState() == TaskState.TO_PACKET;
        }

        return false;
    }

    /**
     * Check if position is reached
     * 
     * @param agentState The current state of the agent
     * @param position The position to reach
     * @return True if agent is next to position
     */
    private boolean positionReached(AgentState agentState) {
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        if(memoryFragments.contains(MemoryKeys.TASK)) {
            String taskString = agentState.getMemoryFragment(MemoryKeys.TASK);
            Task task = Task.fromJson(taskString);
            int agentX = agentState.getX();
            int agentY = agentState.getY();
            int positionX = task.getPacket().getCoordinate().getX();
            int positionY = task.getPacket().getCoordinate().getY();
    
            int dx = Math.abs(agentX - positionX);
            int dy = Math.abs(agentY - positionY);

            return (dx <= 1) && (dy <= 1);
        }

        return false;  
    }
    
}
