package agent.behavior.assignment_1_B;

import java.util.List;
import java.util.Set;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;

import com.google.gson.Gson;
import environment.Coordinate;
import util.MemoryKeys;
import util.graph.Graph;
import util.graph.NodeType;
import util.task.Task;
import util.task.TaskState;

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

        // Update agents previous position
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        Coordinate agentPosition = new Coordinate(agentX, agentY);

        // Handle graph
        handleGraph(agentState);

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


            updateMappingMemory(agentState, null, null, agentPosition, null, null);
        }
        else agentAction.skip();   
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Dynamically builds the graph
     * Agent saves potential starts of edges in edgeStartPos.
     *
     * If agent goes in a straight line -> agents previous position lies on the line between
     * edgeStartPos and agents current position.
     *
     * If agent turns -> agents previous position DOES NOT lie on the line between
     * edgeStartPos and agents current position
     * -> create new edge between edgeStartPos and agents previous position.
     *
     * @param agentState Current state of agent
     */
    private void handleGraph(AgentState agentState) {
        // Retrieve positions
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        Coordinate agentPosition = new Coordinate(agentX, agentY);

        Graph graph = getGraph(agentState);
        Coordinate previousPosition = getPreviousPosition(agentState);
        Coordinate edgeStartPosition = getEdgeStartPosition(agentState);
        if (!edgeStartPosition.equals(previousPosition) && !previousPosition.equals(agentPosition)) {
            if (!graph.onTheLine(edgeStartPosition, agentPosition, previousPosition)) {
                if (!graph.nodeExists(previousPosition)) graph.addNode(previousPosition, NodeType.FREE);
                graph.addEdge(edgeStartPosition, previousPosition);
                edgeStartPosition = previousPosition;
            }
        }

        // Update mapping memory
        updateMappingMemory(agentState, graph, null, null, edgeStartPosition, null);
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
     * Retrieve previous position from memory
     * Create previous position if not yet created
     *
     * @param agentState Current state of agent
     * @return Previous position
     */
    private Coordinate getPreviousPosition(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        Gson gson = new Gson();
        // Check if previous position exists in memory
        if(memoryFragments.contains(MemoryKeys.PREVIOUS_POSITION)) {
            // Retrieve previous position
            String previousPositionString = agentState.getMemoryFragment(MemoryKeys.PREVIOUS_POSITION);
            return gson.fromJson(previousPositionString, Coordinate.class);
        }
        else {
            // Create previous position
            int agentX = agentState.getX();
            int agentY = agentState.getY();
            Coordinate previousPosition = new Coordinate(agentX, agentY);

            // Add edge start position to memory
            String previousPositionString = gson.toJson(previousPosition);
            agentState.addMemoryFragment(MemoryKeys.PREVIOUS_POSITION, previousPositionString);

            return previousPosition;
        }
    }

    /**
     * Retrieve edge start position from memory
     * Create edge start position if not yet created
     *
     * @param agentState Current state of agent
     * @return Edge start position
     */
    private Coordinate getEdgeStartPosition(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        Gson gson = new Gson();
        // Check if edge start position exists in memory
        if(memoryFragments.contains(MemoryKeys.EDGE_START_POSITION)) {
            // Retrieve edge start position
            String edgeStartPositionString = agentState.getMemoryFragment(MemoryKeys.EDGE_START_POSITION);
            return gson.fromJson(edgeStartPositionString, Coordinate.class);
        }
        else {
            // Create edge start position
            int agentX = agentState.getX();
            int agentY = agentState.getY();
            Coordinate edgeStartPosition = new Coordinate(agentX, agentY);

            // Add edge start position to memory
            String edgeStartPositionString = gson.toJson(edgeStartPosition);
            agentState.addMemoryFragment(MemoryKeys.EDGE_START_POSITION, edgeStartPositionString);

            return edgeStartPosition;
        }
    }

    /**
     * Update mapping memory of agent
     *
     * @param agentState Current state of the agent
     * @param graph Graph
     * @param path Path
     * @param previousPosition Previous position
     * @param edgeStartPosition Edge start position
     * @param shouldBeHerePosition Should be here position
     */
    private void updateMappingMemory(AgentState agentState, Graph graph, List<Coordinate> path, Coordinate previousPosition, Coordinate edgeStartPosition, Coordinate shouldBeHerePosition) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        Gson gson = new Gson();
        if(graph != null) {
            // Remove graph from memory
            if(memoryFragments.contains(MemoryKeys.GRAPH)) agentState.removeMemoryFragment(MemoryKeys.GRAPH);

            // Add updated graph to memory
            String graphString = graph.toJson();
            agentState.addMemoryFragment(MemoryKeys.GRAPH, graphString);

        }

        if(path != null) {
            // Remove path from memory
            if(memoryFragments.contains(MemoryKeys.PATH)) agentState.removeMemoryFragment(MemoryKeys.PATH);

            // Add updated path to memory
            String pathString = gson.toJson(path);
            agentState.addMemoryFragment(MemoryKeys.PATH, pathString);

        }

        if(previousPosition != null) {
            // Remove previous position from memory
            if(memoryFragments.contains(MemoryKeys.PREVIOUS_POSITION)) agentState.removeMemoryFragment(MemoryKeys.PREVIOUS_POSITION);

            // Add updated previous position to memory
            String previousPositionString = gson.toJson(previousPosition);
            agentState.addMemoryFragment(MemoryKeys.PREVIOUS_POSITION, previousPositionString);

        }

        if(edgeStartPosition != null) {
            // Remove edge start position from memory
            if(memoryFragments.contains(MemoryKeys.EDGE_START_POSITION)) agentState.removeMemoryFragment(MemoryKeys.EDGE_START_POSITION);

            // Add updated edge start position to memory
            String edgeStartPositionString = gson.toJson(edgeStartPosition);
            agentState.addMemoryFragment(MemoryKeys.EDGE_START_POSITION, edgeStartPositionString);

        }

        if(shouldBeHerePosition != null) {
            // Remove should be here position from memory
            if(memoryFragments.contains(MemoryKeys.SHOULD_BE_HERE_POSITION)) agentState.removeMemoryFragment(MemoryKeys.SHOULD_BE_HERE_POSITION);

            // Add updated should be here position to memory
            String shouldBeHerePositionString = gson.toJson(shouldBeHerePosition);
            agentState.addMemoryFragment(MemoryKeys.SHOULD_BE_HERE_POSITION, shouldBeHerePositionString);

        }
    }




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