package agent.behavior.assignment_2.changes;

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
        packetAlreadyHandled = checkPacketAlreadyHandled(agentState);
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
     *
     * @return True is packet is not at initial place, otherwise false
     */
    private boolean checkPacketAlreadyHandled(AgentState agentState) {
        // Get the task
        Task task = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.TASK, Task.class);

        // Check if the task is null and return false if so
        if(task == null) return false;

        // Get the position of the packet
        Packet packet= task.getPacket();
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
                
                // Check if the positions correspond
                if(cellX == packetX && cellY == packetY) {
                    // Return if the cell does not contain a packet
                    return !cellPerception.containsPacket();
                }
            }
        }

        return false;
    }
}
