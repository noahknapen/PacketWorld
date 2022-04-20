package agent.behavior.assignment_2.behaviors;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.Coordinate;
import util.assignments.general.ActionUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.Destination;
import util.assignments.task.Task;
import util.assignments.task.TaskType;

/**
 * A behavior where the agent puts down a packet
 */
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
        // Put down the packet
        handlePutDown(agentState, agentAction);
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * A function to let the agent put down the packet
     * 
     * @param agentState The current state of the agent
     * @param agentAction Perform an action with the agent
     */
    private void handlePutDown(AgentState agentState, AgentAction agentAction) {
        // Get the task
        Task task = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.TASK, Task.class);

        // Check if the task has other task type than MOVE_TO_DESTINATION or has no destination and raise exception if so
        if(task.getType() != TaskType.MOVE_TO_DESTINATION || !task.getDestination().isPresent()) throw new IllegalArgumentException("Task type is not MOVE_TO_DESTINATION or task has no destination");

        // Get the coordinate of the destination
        Destination destination= task.getDestination().get();
        Coordinate destinationCoordinate = destination.getCoordinate();

        // Put down the packet
        ActionUtils.putDownPacket(agentState, agentAction, destinationCoordinate);
    }
}