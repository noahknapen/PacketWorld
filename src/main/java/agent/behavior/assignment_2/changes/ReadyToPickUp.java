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
import util.assignments.targets.Packet;
import util.assignments.task.Task;

/**
 * A behavior change class that checks if the agent can pick up the desired packet
 */
public class ReadyToPickUp extends BehaviorChange{

    private boolean readyToPickUp = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        AgentState agentState = this.getAgentState();
        
        // Check if the position is reached
        readyToPickUp = handlePositionReached(agentState);
    }

    @Override
    public boolean isSatisfied() {
        return readyToPickUp;
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Check if the position of the packet is reached by the agent
     * 
     * @param agentState The current state of the agent
     *
     * @return True if agent has reached the position of the packet, otherwise false
     */
    private boolean handlePositionReached(AgentState agentState) {
        // Get the task
        Task task = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.TASK, Task.class);

        // Check if the task is null and return false if so
        if(task == null) return false;

        // Check if the task has no packet and return false if so
        if(task.getPacket().isEmpty()) return false;

        // Get the coordinate of the packet
        Packet packet = task.getPacket().get();
        Coordinate packetCoordinate = packet.getCoordinate();

        // Return if the agent has reached the position
        return GeneralUtils.hasReachedPosition(agentState, packetCoordinate);
    }
}
