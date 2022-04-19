package agent.behavior.assignment_2.changes;

import java.util.Set;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import agent.behavior.assignment_1_A.utils.Packet;
import agent.behavior.assignment_1_A.utils.Task;
import agent.behavior.assignment_1_B.utils.Graph;
import agent.behavior.assignment_1_B.utils.MemoryKeys;
import environment.CellPerception;
import environment.Perception;

public class PacketAlreadyHandled extends BehaviorChange{

    private boolean packetAlreadyHandled = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        System.out.println("[PacketAlreadyHandled]{updateChange}");

        AgentState agentState = this.getAgentState();
        
        // Packet already handled
        packetAlreadyHandled = checkPacketAlreadyHandled(agentState);

        if(packetAlreadyHandled) {
            Graph graph = getGraph(agentState);
            Task task = getTask(agentState);

            // graph.removeNode(task.getPacket().getCoordinate());

            updateMappingMemory(agentState, graph);
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

        System.out.println("[PacketAlreadyHandled]{updateMappingMemory} Graph updated in memory");
    }    
}
