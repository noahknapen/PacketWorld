package agent.behavior.assignment_1_B;

import java.util.Set;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import agent.behavior.assignment_1_A.utils.Task;
import agent.behavior.assignment_1_B.utils.MemoryKeys;
import environment.Coordinate;

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
        System.out.println("[PutDownPacketBehavior]{act}");
        
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();
        //Check if task exists in memory
        if(memoryFragments.contains(MemoryKeys.TASK)) {
            // Retrieve task
            String taskString = agentState.getMemoryFragment(MemoryKeys.TASK);
            Task task = Task.fromJson(taskString);

            // Put down packet
            putDownPacket(agentState, agentAction, task);

            // Update memory
            updateMemory(agentState);
        }
        else agentAction.skip();
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Put down packet
     * 
     * @param agentState Current state of agent
     * @param agentAction Perfom an action with agent
     * @param task Current task
     */
    private void putDownPacket(AgentState agentState, AgentAction agentAction, Task task) {
        // Retrieve position
        Coordinate position = task.getDestination().getCoordinate();
        int positionX = position.getX();
        int positionY = position.getY();
        
        // Put down packet
        agentAction.putPacket(positionX, positionY);
        
        System.out.println("[PutDownPacketBehavior]{putDownPacket} Packet put down (" + task.getPacket().getColor() + ")");
    }

    /**
     * Update memory of agent
     * 
     * @param agentState Current state of the agent
     */
    private void updateMemory(AgentState agentState) {
        // Remove task from memory
        agentState.removeMemoryFragment(MemoryKeys.TASK);
        
        System.out.println("[PutDownPacketBehavior]{updateMemory} Task deleted from memory");
    }
}