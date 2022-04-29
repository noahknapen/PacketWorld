package agent.behavior.assignment_2.behaviors;

import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

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
        // Handle the charging stations
        try {
            GeneralUtils.handleChargingStations(agentState, agentCommunication);
        } catch (IOException e) {
            e.printStackTrace();
        }        
    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) { 
        try {
            // Check the perception of the agent
            GeneralUtils.checkPerception(agentState);

            // Build the graph
            GraphUtils.build(agentState);

            // Move the agent to the target
            handleMove(agentState, agentAction);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * A function to let the agent move
     * 
     * @param agentState The current state of the agent
     * @param agentAction Perform an action with the agent
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    private void handleMove(AgentState agentState, AgentAction agentAction) throws JsonParseException, JsonMappingException, IOException {
        // Get the task
        Task task = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.TASK, Task.class);

        // Check if the task is null and raise exception if so
        if(task == null) throw new IllegalArgumentException("Task is null");

        // Check if the task has other task type than MOVE_TO_PACKET or has no packet and raise exception if so
        if(task.getType() != TaskType.MOVE_TO_PACKET || task.getPacket().isEmpty()) throw new IllegalArgumentException("Task type is not MOVE_TO_PACKET or task has no packet");

        // Get the coordinate of the packet
        Packet packet = task.getPacket().get();
        Coordinate packetCoordinate = packet.getCoordinate();

        // Perform move to the position of the packet
        ActionUtils.moveToPosition(agentState, agentAction, packetCoordinate);
    }
}