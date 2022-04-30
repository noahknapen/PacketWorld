package agent.behavior.assignment_3.changes;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import environment.CellPerception;
import environment.Perception;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.Packet;
import util.assignments.task.Task;

/**
 * A behavior change class that checks if the packet towards which the agent is moving 
 * hasn't already been picked up by another agent
 */
public class PacketAlreadyHandled extends BehaviorChange{

    private boolean packetAlreadyHandled = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        AgentState agentState = this.getAgentState();
        
        // Check if the packet was already handled
        try {
            packetAlreadyHandled = checkPacketAlreadyHandled(agentState);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isSatisfied() {
        return packetAlreadyHandled;
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Check if the packet was already handled by another agent
     * 
     * @param agentState The current state of the agent
     * @return True is packet is not at initial place, otherwise false
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    private boolean checkPacketAlreadyHandled(AgentState agentState) throws JsonParseException, JsonMappingException, IOException {
        // Get the task
        Task task = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.TASK, Task.class);

        // Check if the task is null and return false if so
        if(task == null) return false;

        // Check if the task has no packet and return false if so
        if(!task.getPacket().isPresent()) return false;

        // Get the position of the packet
        Packet packet= task.getPacket().get();
        int packetX = packet.getCoordinate().getX();
        int packetY = packet.getCoordinate().getY();

        // Get the perception of the agent
        Perception agentPerception = agentState.getPerception();

        // Loop over the whole perception
        for (int x = 0; x < agentPerception.getWidth(); x++) {
            for (int y = 0; y < agentPerception.getHeight(); y++) {
                CellPerception cellPerception = agentPerception.getCellAt(x,y);

                // Check if the cell is null and continue with the next cell if so
                if(cellPerception == null) continue;

                // Get the position of the cell
                int cellX = cellPerception.getX();
                int cellY = cellPerception.getY();
                
                // Check if the positions correpond
                if(cellX == packetX && cellY == packetY) {
                    // Return if the cell does not contain a packet
                    return !cellPerception.containsPacket();
                }
            }
        }

        return false;
    }
}
