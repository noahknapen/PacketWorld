package agent.behavior.assignment_2.behaviors;

import java.util.Map;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.Coordinate;
import util.assignments.general.ActionUtils;
import util.assignments.general.GeneralUtils;
import util.assignments.graph.GraphUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.Packet;
import util.assignments.task.Task;
import util.assignments.task.TaskType;

/**
 * A behavior where the agent picks up a packet
 */
public class PickUpPacketBehavior extends Behavior {

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        // Communicate the charging stations with all the other agents
        GeneralUtils.handleChargingStations(agentState, agentCommunication);

        // Communicate the destination locations with agents in perception
        GeneralUtils.handleDestinationLocations(agentState, agentCommunication);
    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        // Pick up the packet
        handlePickUp(agentState, agentAction);

        // Build the graph
        GraphUtils.build(agentState);

        // Update task
        updateTask(agentState);
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * A function to let the agent pick up the packet
     * 
     * @param agentState The current state of the agent
     * @param agentAction Perform an action with the agent
     */
    private void handlePickUp(AgentState agentState, AgentAction agentAction) {
        // Get the task
        Task task = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.TASK, Task.class);

        // Check if the task has other task type than MOVE_TO_PACKET or has no packet and raise exception if so
        if(task.getType() != TaskType.MOVE_TO_PACKET || task.getPacket().isEmpty()) agentAction.skip();

        // Get the coordinate of the packet
        Packet packet= task.getPacket().get();
        Coordinate packetCoordinate = packet.getCoordinate();

        // Pick up the packet
        ActionUtils.pickUpPacket(agentState, agentAction, packetCoordinate);
    }

    /**
     * A function to update the task type to MOVE_TO_DESTINATION
     * 
     * @param agentState The current state of the agent
     */
    private void updateTask(AgentState agentState) {
        // Get the task
        Task task = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.TASK, Task.class);

        // Check if the task is null and raise exception if so
        if(task == null) return;

        // Update the task type
        task.setType(TaskType.MOVE_TO_DESTINATION);

        // Update the memory
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.TASK, task));
    }
}