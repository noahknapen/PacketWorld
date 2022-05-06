package agent.behavior.assignment_2.behaviors;

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

import java.util.Map;

/**
 * A behavior where the agent moves towards a packet
 */
public class MoveToPacketBehavior extends Behavior {

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
        // Check the perception of the agent
        GeneralUtils.checkPerception(agentState);

        // Build the graph
        GraphUtils.build(agentState);

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

        // Check if the task has other task type than MOVE_TO_PACKET and raise exception if so
        if(task.getType() != TaskType.MOVE_TO_PACKET) throw new IllegalArgumentException("Task type is not MOVE_TO_PACKET");

        // Get the coordinate of the packet
        Packet packet = task.getPacket();
        Coordinate packetCoordinate = packet.getCoordinate();

        // Perform move to the position of the packet
        ActionUtils.moveToPosition(agentState, agentAction, packetCoordinate);
    }
}