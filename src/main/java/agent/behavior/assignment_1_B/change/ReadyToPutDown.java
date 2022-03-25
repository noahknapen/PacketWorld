package agent.behavior.assignment_1_B.change;

import java.util.Set;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import agent.behavior.assignment_1_A.utils.Task;
import agent.behavior.assignment_1_A.utils.TaskState;
import agent.behavior.assignment_1_B.utils.MemoryKeys;

public class ReadyToPutDown extends BehaviorChange{

    private boolean readyToPutDown = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        AgentState agentState = this.getAgentState();
        
        readyToPutDown = toDestinationTask(agentState) && positionReached(agentState);      
    }

    @Override
    public boolean isSatisfied() {
        return readyToPutDown;
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Check if the current state of the task is TO_DESTINATION
     * 
     * @param agentState The current state of the agent
     */
    private boolean toDestinationTask(AgentState agentState) {
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        if(memoryFragments.contains(MemoryKeys.TASK)) {
            String taskString = agentState.getMemoryFragment(MemoryKeys.TASK);
            Task task = Task.fromJson(taskString);

            return task.getTaskState() == TaskState.TO_DESTINATION;
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
            int positionX = task.getDestination().getCoordinate().getX();
            int positionY = task.getDestination().getCoordinate().getY();
    
            int dx = Math.abs(agentX - positionX);
            int dy = Math.abs(agentY - positionY);

            return (dx <= 1) && (dy <= 1);
        }

        return false;  
    }
    
}
