package util.assignments.graph;

import java.util.Map;

import agent.AgentState;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;

/**
 * A class that implements functions regarding the graph
 */
public class GraphUtils {
    
    ///////////
    // BUILD //
    ///////////

    /**
     * Build the graph based on the perception of the agent
     * 
     * @param agentState The current state of the agent
     */
    public static void build(AgentState agentState) {
        // Get the perception the agent
        Perception agentPerception = agentState.getPerception();

        // Get the graph
        Graph graph = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class);

        // Check if graph is null and create one if so
        if(graph == null)
            graph = new Graph();
        
        // Loop over the whole perception to create nodes
        for (int x = 0; x < agentPerception.getWidth(); x++) {
            for (int y = 0; y < agentPerception.getHeight(); y++) {
                CellPerception cellPerception = agentPerception.getCellAt(x,y);

                // Check if the cell is null and continue with the next cell if so
                if(cellPerception == null) continue;
        
                // Get the position of the cell
                int cellX = cellPerception.getX();
                int cellY = cellPerception.getY();
                Coordinate cellCoordinate = new Coordinate(cellX, cellY);

                // Create a node
                Node cellNode = new Node(cellCoordinate);

                // Add the node to the graph
                graph.addNode(cellNode);

                // Loop over neighbourhood to add edges
                for(int i = -1; i <= 1; i++) {
                    for(int j = -1; j <= 1; j++) {
                        // Get the position of the neighbour cell
                        int neighbourCellX = cellX + i;
                        int neighbourCellY = cellY + j;

                        // Get the corresponding neighbour cell
                        CellPerception neighbourCell = agentPerception.getCellAt(neighbourCellX, neighbourCellY);

                        // Check if the neightbour cell is null and continue with the next cell if so
                        if(neighbourCell == null) continue;

                        // Get the position of the neighbour cell
                        Coordinate neighbourCellCoordinate = new Coordinate(neighbourCellX, neighbourCellY);

                        // Create a node
                        Node neighbourNode = new Node(neighbourCellCoordinate);

                        // Check if node is equal to cell and continue with the next cell if so
                        if(cellNode.equals(neighbourNode)) continue;

                        // Add the edges between the cells
                        graph.addEdge(cellNode, neighbourNode);
                    }
                }
            }
        }

        // Update the memory
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.GRAPH, graph));
    }
}
