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
        System.out.println("[ReadyToPickUp] updateChange");

        AgentState agentState = this.getAgentState();
        
        // Ready to pick up if task state is TO_PACKET and if position is reached
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
     * Check if current state of task is TO_PACKET
     * 
     * @param agentState Current state of agent
     * @return True if task state is TO_PACKET
     */
    private boolean toPacketTask(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        // Check if task exists in memory
        if(memoryFragments.contains(MemoryKeys.TASK)) {
            // Retrieve task
            String taskString = agentState.getMemoryFragment(MemoryKeys.TASK);
            Task task = Task.fromJson(taskString);

            // Check if state is TO_PACKET
            return task.getState() == TaskState.TO_PACKET;
        }
        else return false;
    }

    /**
     * Check if position is reached
     * 
     * @param agentState Current state of agent
     * @param position Position to reach
     * @return True if agent is next to position
     */
    private boolean positionReached(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        // Check if task exists in memory
        if(memoryFragments.contains(MemoryKeys.TASK)) {
            // Retrieve task
            String taskString = agentState.getMemoryFragment(MemoryKeys.TASK);
            Task task = Task.fromJson(taskString);

            // Retrieve position
            int agentX = agentState.getX();
            int agentY = agentState.getY();
            int positionX = task.getPacket().getCoordinate().getX();
            int positionY = task.getPacket().getCoordinate().getY();
    
            int dX = Math.abs(agentX - positionX);
            int dY = Math.abs(agentY - positionY);

            return (dX <= 1) && (dY <= 1);
        }
        else return false;  
    }
    
}
