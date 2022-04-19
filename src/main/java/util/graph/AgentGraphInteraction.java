package util.graph;

import java.util.List;
import java.util.Set;

import com.google.gson.Gson;

import agent.AgentState;
import environment.Coordinate;
import util.MemoryKeys;
import util.targets.BatteryStation;
import util.targets.Destination;
import util.targets.Packet;
import util.targets.Target;

public class AgentGraphInteraction {
    
    /**
     * Adds new target to graph
     * Draws an edge connecting this target to the relevant positions
     * 
     * @param agentState: The current state of the agent
     * @param target: The target position we want to add to the graph
     */
    public static void addTargetToGraph(AgentState agentState, Target target) {
        // Create a Coordinate of the agent's position
        Coordinate agentPosition = new Coordinate(agentState.getX(), agentState.getY());

        // Retrieve the graph stored in the agent's memory
        Graph graph = AgentGraphInteraction.getGraph(agentState);

        Coordinate edgeStartPosition = AgentGraphInteraction.getEdgeStartPosition(agentState);

        // If agent position is not in the graph -> Add the position and an edge from edgeStartPos.
        if (!graph.nodeExists(agentPosition)) {
            graph.addNode(agentPosition, NodeType.FREE);
            graph.addEdge(edgeStartPosition, agentPosition);
            edgeStartPosition = agentPosition;
        }

        // Determine the right type of target
        NodeType nodeType;
        if (target instanceof Packet) nodeType = NodeType.PACKET;
        else if (target instanceof Destination) nodeType = NodeType.DESTINATION;
        else if (target instanceof BatteryStation) nodeType = NodeType.BATTERYSTATION;
        else nodeType = NodeType.FREE;

        // Add the target to the graph
        graph.addNode(target.getCoordinate(), nodeType);

        // TODO: Check if path is free from obstacles (It should be but not sure)
        graph.addEdge(agentPosition, target.getCoordinate());

        // Update memory
        AgentGraphInteraction.updateMappingMemory(agentState, graph, null, null, edgeStartPosition, null, null);
    }

    public static boolean checkNodes(AgentState agentState) {
        Graph graph = AgentGraphInteraction.getGraph(agentState);

        for (Node node : graph.getNodes().values()) {
            if (node.getEdges().isEmpty()){
                return true;
            }
        }
        return false;
    }

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
    public static void handleGraph(AgentState agentState) {
        // Create a Coordination object for the agent's position
        Coordinate agentPosition = new Coordinate(agentState.getX(), agentState.getY());

        // Get the graph in the memory of the agent
        Graph graph = AgentGraphInteraction.getGraph(agentState);

        checkIfExpandGraph(agentState, agentPosition, graph);

        // Update mapping memory
        AgentGraphInteraction.updateMappingMemory(agentState, graph, null, null, null, null, null);
    }

    private static void checkIfExpandGraph(AgentState agentState, Coordinate agentPosition, Graph graph) {
        // Check if map width needs increasing
        if (agentPosition.getX() + 1 > graph.getMapWidth()) {
            graph.addColumns(agentPosition.getX());
        }

        // Check if map height needs increasing
        if (agentPosition.getY() + 1 > graph.getMapHeight()) {
            graph.addRows(agentPosition.getY());
        }
    }

    /**
     * Retrieves the graph from memory or creates a new graph if not yet created
     * 
     * @param agentState: The current state of agent
     * @return The graph in the agents memory
     */
    public static Graph getGraph(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        // Check if graph exists in memory
        if(memoryFragments.contains(MemoryKeys.GRAPH)) {
            // Retrieve graph
            return Graph.fromJson(agentState.getMemoryFragment(MemoryKeys.GRAPH));
        } else {
            // Create graph with the agents position as initial node
            Graph graph = new Graph(agentState.getX(), agentState.getY());
        
            // Add graph to memory
            agentState.addMemoryFragment(MemoryKeys.GRAPH, graph.toJson());

            // return the created graph
            return graph;
        }
    }


    /**
     * Retrieves the previous position from memory or creates a new previous position if not yet created
     * 
     * @param agentState: The current state of agent
     * @return The previous position
     */ 
    public static Coordinate getPreviousPosition(AgentState agentState) {
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
     * Retrieve edge start position from memory or creates a new edge start position if not yet created
     * 
     * @param agentState: Current state of agent
     * @return Edge start position
     */ 
    public static Coordinate getEdgeStartPosition(AgentState agentState) {
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
    public static void updateMappingMemory(AgentState agentState, Graph graph, List<Coordinate> path, Coordinate previousPosition, Coordinate edgeStartPosition, Coordinate shouldBeHerePosition, List<Coordinate> visitedNodes) {
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

        if(visitedNodes != null) {
            // Remove path from memory
            if(memoryFragments.contains(MemoryKeys.VISITED_NODES)) agentState.removeMemoryFragment(MemoryKeys.VISITED_NODES);

            // Add updated path to memory
            String visitedString = gson.toJson(visitedNodes);
            agentState.addMemoryFragment(MemoryKeys.VISITED_NODES, visitedString);

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

}
