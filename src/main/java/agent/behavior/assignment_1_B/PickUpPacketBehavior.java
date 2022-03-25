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
        System.out.println("[PickUpPacketBehavior] act");

        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        if(memoryFragments.contains(MemoryKeys.TASK)) {
            String taskString = agentState.getMemoryFragment(MemoryKeys.TASK);
            Task task = Task.fromJson(taskString);
            Coordinate position = task.getPacket().getCoordinate();
            
            pickUpPacket(agentState, agentAction, position);
      
            return;
        }

        agentAction.skip();
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Pick up a packet at a certain position
     * 
     * @param agentAction Perfom an action with the agent
     * @param position The position of the packet
     */
    private void pickUpPacket(AgentState agentState, AgentAction agentAction, Coordinate position) {
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();
        int positionX = position.getX();
        int positionY = position.getY();
        
        agentAction.pickPacket(positionX, positionY);

        Task task = null;
        if(memoryFragments.contains(MemoryKeys.TASK)) {
            String taskString = agentState.getMemoryFragment(MemoryKeys.TASK);
            task = Task.fromJson(taskString);
        }
        else return;

        if(task == null) return;

        task.setTaskState(TaskState.TO_DESTINATION);

        if(memoryFragments.contains(MemoryKeys.TASK)) agentState.removeMemoryFragment(MemoryKeys.TASK);
        String taskString = task.toJson();
        agentState.addMemoryFragment(MemoryKeys.TASK, taskString);
    }
}