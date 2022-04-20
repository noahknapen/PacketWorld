package agent.behavior.assignment_2.changes;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import environment.Coordinate;
import util.assignments.general.ActionUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.Destination;
import util.assignments.task.Task;

/**
 * A behavior change class that checks if the agent can put down the carried packet
 */
public class ReadyToPutDown extends BehaviorChange{

    private boolean readyToPutDown = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        AgentState agentState = this.getAgentState();
        
        // Check if the position is reached
        readyToPutDown = handlePositionReached(agentState);
    }

    @Override
    public boolean isSatisfied() {
        return readyToPutDown;
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Check if the position of the destination is reached by the agent
     * 
     * @param agentState The current state of the agent
     * @return True if agent has reached the position of the destination, otherwise false
     */
    private boolean handlePositionReached(AgentState agentState) {
        // Get the task
        Task task = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.TASK, Task.class);

        // Check if the task is null and return false if so
        if(task == null) return false;

        // Check if the task has no destination and return false if so
        if(!task.getDestination().isPresent()) return false;

        // Get the coordinate of the destination
        Destination destination= task.getDestination().get();
        Coordinate destinationCoordinate = destination.getCoordinate();

        // Return if the agent has reached the position
        return ActionUtils.hasReachedPosition(agentState, destinationCoordinate);
    }
}
