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
import util.task.TaskState;

public class PickUpPacketBehavior extends Behavior {

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        // No communication necessary as the agent does not move to a new location when picking up a packet
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
        // Check if task exists in memory
        if(memoryFragments.contains(MemoryKeys.TASK)) {
            // Retrieve task
            String taskString = agentState.getMemoryFragment(MemoryKeys.TASK);
            Task task = Task.fromJson(taskString);

            // Pick up packet
            pickUpPacket(agentState, agentAction, task);

            task.setState(TaskState.TO_DESTINATION);

            // Update memory
            updateTaskMemory(agentState, task);


            AgentGraphInteraction.updateMappingMemory(agentState, null, null, agentPosition, null, null, null);
        }
        else agentAction.skip();   
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Pick up packet
     * 
     * @param agentState Current state of agent
     * @param agentAction Perfom an action with agent
     * @param task Current task
     */
    private void pickUpPacket(AgentState agentState, AgentAction agentAction, Task task) {
        // Retrieve position
        Coordinate position = task.getPacket().getCoordinate();
        int positionX = position.getX();
        int positionY = position.getY();

        // Pick up packet
        agentAction.pickPacket(positionX, positionY);

    }

    /**
     * Update memory of agent
     * 
     * @param agentState Current state of agent
     * @param task Current task
     */
    private void updateTaskMemory(AgentState agentState, Task task) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        // Remove task from memory
        if(memoryFragments.contains(MemoryKeys.TASK)) agentState.removeMemoryFragment(MemoryKeys.TASK);

        // Add updated task to memory
        String taskString = task.toJson();
        agentState.addMemoryFragment(MemoryKeys.TASK, taskString);
        
    }
}