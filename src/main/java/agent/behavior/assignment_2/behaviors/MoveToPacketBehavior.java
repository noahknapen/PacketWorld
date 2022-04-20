package agent.behavior.assignment_2.behaviors;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.Coordinate;
import util.assignments.general.ActionUtils;
import util.assignments.general.GeneralUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.Destination;
import util.assignments.targets.Packet;
import util.assignments.task.Task;
import util.assignments.task.TaskType;

/**
 * A behavior where the agent moves towards a packet
 */
public class MoveToPacketBehavior extends Behavior {

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
        // Get the task
        Task task = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.TASK, Task.class);

        // Check if the task is null and raise exception if so
        if(task == null) throw new IllegalArgumentException("Task is null");

        // Check if the task has task type MOVE_TO_PACKET but has no packet and raise exception if so
        if(task.getType() == TaskType.MOVE_TO_PACKET && !task.getPacket().isPresent()) throw new IllegalArgumentException("Task has no packet");

        // Check if the task has task type MOVE_TO_PACKET but has no packet and raise exception if so
        if(task.getType() == TaskType.MOVE_TO_DESTINATION && !task.getDestination().isPresent()) throw new IllegalArgumentException("Task has no destination");

        Coordinate coordinate = null;
        if(task.getType() == TaskType.MOVE_TO_PACKET) {
            // Get the coordinate of the packet
            Packet packet= task.getPacket().get();
            coordinate = packet.getCoordinate();
        }
        else if(task.getType() == TaskType.MOVE_TO_DESTINATION) {
            // Get the coordinate of the destination
            Destination destination = task.getDestination().get();
            coordinate = destination.getCoordinate();
        }

        // Check if the coordinate is null and raise exception if so
        if(coordinate == null) throw new IllegalArgumentException("Task has wrong task type");

        ActionUtils.moveToPosition(agentState, agentAction, coordinate);
    }
}