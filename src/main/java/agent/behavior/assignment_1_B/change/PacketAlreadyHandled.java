package agent.behavior.assignment_1_B.change;

import java.util.Set;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import environment.CellPerception;
import environment.Perception;
import util.MemoryKeys;
import util.targets.Packet;
import util.task.Task;

public class PacketAlreadyHandled extends BehaviorChange{

    private boolean packetAlreadyHandled = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {

        AgentState agentState = this.getAgentState();
        
        // Packet already handled
        this.packetAlreadyHandled = checkPacketAlreadyHandled(agentState);
    }

    @Override
    public boolean isSatisfied() {
        return packetAlreadyHandled;
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Check if packet was already handled by another agent
     * 
     * @param agentState Current state of the agent
     * @return True is packet is not at initial place, otherwise false
     */
    private boolean checkPacketAlreadyHandled(AgentState agentState) {       
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        // Check if task exists in memory
        if(memoryFragments.contains(MemoryKeys.TASK)) {
            // Retrieve task
            String taskString = agentState.getMemoryFragment(MemoryKeys.TASK);
            Task task = Task.fromJson(taskString);

            // Retrieve position
            Packet packet= task.getPacket();
            int positionX = packet.getCoordinate().getX();
            int positionY = packet.getCoordinate().getY();

            Perception perception = agentState.getPerception();
            for (int x = 0; x < perception.getWidth(); x++) {
                for (int y = 0; y < perception.getHeight(); y++) {
                    CellPerception cell = perception.getCellAt(x,y);

                    if(cell == null) continue;

                    int cellX = cell.getX();
                    int cellY = cell.getY();
                    
                    // Check if positions correponds
                    if(cellX == positionX && cellY == positionY) {
                        return !cell.containsPacket();
                    }
                }
            }

            return false;
        }
        else return false;
    }
  }
