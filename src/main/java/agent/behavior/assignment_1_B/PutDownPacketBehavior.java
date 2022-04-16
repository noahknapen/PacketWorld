package agent.behavior.assignment_1_B;

import java.util.Set;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;

import environment.Coordinate;
import util.MemoryKeys;
import util.graph.AgentGraphInteraction;
import util.task.Task;

public class PutDownPacketBehavior extends Behavior {
    
    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        // No communication necessary as the agent does not move to a new location when putting down a packet
    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {

        // Update agents previous position
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        Coordinate agentPosition = new Coordinate(agentX, agentY);

        // Handle graph
        AgentGraphInteraction.handleGraph(agentState);

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
            updateTaskMemory(agentState);
        }
        else agentAction.skip();

        AgentGraphInteraction.updateMappingMemory(agentState, null, null, agentPosition, null, null, null);
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
        
    }

    /**
     * Update memory of agent
     * 
     * @param agentState Current state of the agent
     */
    private void updateTaskMemory(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        // Remove task from memory
        if(memoryFragments.contains(MemoryKeys.TASK)) agentState.removeMemoryFragment(MemoryKeys.TASK);
        
    }
}