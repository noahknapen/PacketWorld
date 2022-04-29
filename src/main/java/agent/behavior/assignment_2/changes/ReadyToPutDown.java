package agent.behavior.assignment_2.changes;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import environment.Coordinate;
import util.assignments.general.GeneralUtils;
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
        try {
            readyToPutDown = handlePositionReached(agentState);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    private boolean handlePositionReached(AgentState agentState) throws JsonParseException, JsonMappingException, IOException {
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
        return GeneralUtils.hasReachedPosition(agentState, destinationCoordinate);
    }
}
