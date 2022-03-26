package agent.behavior.assignment_1_B;

import java.util.Set;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import agent.behavior.assignment_1_A.utils.Task;
import agent.behavior.assignment_1_A.utils.TaskState;
import agent.behavior.assignment_1_B.utils.MemoryKeys;
import environment.Coordinate;

public class PickUpPacketBehavior extends Behavior {

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        // TODO Auto-generated method stub
    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        System.out.println("[PickUpPacketBehavior]{act}");

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

        System.out.println("[PickUpPacketBehavior]{pickUpPacket} Packet picked up (" + task.getPacket().getColor() + ")");   
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
        
        System.out.println("[PickUpPacketBehavior]{updateTaskMemory} Task updated in memory");
    }
}