package agent.behavior.assignment_1_B.change;

import java.util.Set;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import agent.behavior.assignment_1_A.utils.Task;
import agent.behavior.assignment_1_A.utils.TaskState;
import agent.behavior.assignment_1_B.utils.Graph;
import agent.behavior.assignment_1_B.utils.MemoryKeys;

public class ReadyToPickUp extends BehaviorChange{

    private boolean readyToPickUp = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        System.out.println("[ReadyToPickUp] updateChange");

        AgentState agentState = this.getAgentState();
        
        // Ready to pick up if task state is TO_PACKET and if position is reached
        readyToPickUp = toPacketTask(agentState) && positionReached(agentState);
        
        if(readyToPickUp) {
            Graph graph = getGraph(agentState);
            Task task = getTask(agentState);

            graph.removeNode(task.getPacket().getCoordinate());

            updateMappingMemory(agentState, graph);
        }
    }

    @Override
    public boolean isSatisfied() {
        return readyToPickUp;
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Check if current state of task is TO_PACKET
     * 
     * @param agentState Current state of agent
     * @return True if task state is TO_PACKET
     */
    private boolean toPacketTask(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        // Check if task exists in memory
        if(memoryFragments.contains(MemoryKeys.TASK)) {
            // Retrieve task
            String taskString = agentState.getMemoryFragment(MemoryKeys.TASK);
            Task task = Task.fromJson(taskString);

            // Check if state is TO_PACKET
            return task.getState() == TaskState.TO_PACKET;
        }
        else return false;
    }

    /**
     * Check if position is reached
     * 
     * @param agentState Current state of agent
     * @param position Position to reach
     * @return True if agent is next to position
     */
    private boolean positionReached(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        // Check if task exists in memory
        if(memoryFragments.contains(MemoryKeys.TASK)) {
            // Retrieve task
            String taskString = agentState.getMemoryFragment(MemoryKeys.TASK);
            Task task = Task.fromJson(taskString);

            // Retrieve positions
            int agentX = agentState.getX();
            int agentY = agentState.getY();
            int positionX = task.getPacket().getCoordinate().getX();
            int positionY = task.getPacket().getCoordinate().getY();
    
            int dX = Math.abs(agentX - positionX);
            int dY = Math.abs(agentY - positionY);

            return (dX <= 1) && (dY <= 1);
        }
        else return false;  
    }

    /**
     * Retrieve graph from memory
     * Create graph if not yet created
     * 
     * @param agentState Current state of agent
     * @return Graph
     */
    private Graph getGraph(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        // Check if graph exists in memory
        if(memoryFragments.contains(MemoryKeys.GRAPH)) {
            // Retrieve graph
            String graphString = agentState.getMemoryFragment(MemoryKeys.GRAPH);
            return Graph.fromJson(graphString);
        }
        else {
            // Create graph
            Graph graph = new Graph(agentState.getX(), agentState.getY());
        
            // Add graph to memory
            String graphString = graph.toJson();
            agentState.addMemoryFragment(MemoryKeys.GRAPH, graphString);

            return graph;
        }
    }

    /**
     * Retrieve task from memory
     * 
     * @param agentState Current state of agent
     * @return Task
     */
    private Task getTask(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        // Check if task exists in memory
        if(memoryFragments.contains(MemoryKeys.TASK)) {
            // Retrieve task
            String taskString = agentState.getMemoryFragment(MemoryKeys.TASK);
            return Task.fromJson(taskString);
        }
        else return null;
    }

    /**
     * Update mapping memory of agent
     * 
     * @param agentState Current state of the agent
     * @param graph Graph
     */
    private void updateMappingMemory(AgentState agentState, Graph graph) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        // Remove graph from memory
        if(memoryFragments.contains(MemoryKeys.GRAPH)) agentState.removeMemoryFragment(MemoryKeys.GRAPH);
            
        // Add updated graph to memory
        String graphString = graph.toJson();
        agentState.addMemoryFragment(MemoryKeys.GRAPH, graphString);

        System.out.println("[ReadyToPickUp]{updateMappingMemory} Graph updated in memory");
    }    
}