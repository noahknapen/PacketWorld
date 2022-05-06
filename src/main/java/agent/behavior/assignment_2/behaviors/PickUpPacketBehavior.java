package agent.behavior.assignment_2.behaviors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.Coordinate;
import util.assignments.general.ActionUtils;
import util.assignments.general.GeneralUtils;
import util.assignments.graph.Graph;
import util.assignments.graph.GraphUtils;
import util.assignments.graph.Node;
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
        GeneralUtils.handleChargingStationsCommunication(agentState, agentCommunication);

        // Communicate the destination locations with agents in perception
        GeneralUtils.handleDestinationsCommunication(agentState, agentCommunication);

        // Communicate the graph with agents in perception
        GeneralUtils.handleGraphCommunication(agentState, agentCommunication);
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

        // Check if the task has other task type than MOVE_TO_PACKET and raise exception if so
        if(task.getType() != TaskType.MOVE_TO_PACKET) throw new IllegalArgumentException("Task type is not MOVE_TO_PACKET");

        // Get the coordinate of the packet
        Packet packet = task.getPacket();
        Coordinate packetCoordinate = packet.getCoordinate();

        // Pick up the packet
        ActionUtils.pickUpPacket(agentState, agentAction, packetCoordinate);

        // Update graph to let it now the packetCoordinate cell is walkable
        GeneralUtils.upDateGraphWhenPacketIsGone(agentState, packetCoordinate);
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