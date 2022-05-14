package agent.behavior.assignment_2.behaviors;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.CellPerception;
import environment.Coordinate;
import util.assignments.general.ActionUtils;
import util.assignments.general.GeneralUtils;
import util.assignments.graph.GraphUtils;
import util.assignments.graph.Node;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.Destination;
import util.assignments.task.Task;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

/**
 * A behavior where the agent moves a packet to another location
 */
public class MovePacketBehavior extends Behavior {

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        // Communicate the charging stations with all the other agents
        GeneralUtils.handleChargingStationsCommunication(agentState, agentCommunication);

        // Communicate the priority tasks with agents in perception
        GeneralUtils.handlePriorityTaskCommunication(agentState, agentCommunication);

        // Communicate the graph with agents in perception
        GeneralUtils.handleGraphCommunication(agentState, agentCommunication);

    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        // Check the perception of the agent
        // GeneralUtils.checkPerception(agentState);

        // Build the graph
        GraphUtils.build(agentState);

        // Move the agent to the target
        handleMove(agentState, agentAction);
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * A function to let the agent move
     *
     * @param agentState The current state of the agent
     * @param agentAction Perform an action with the agent
     */
    private void handleMove(AgentState agentState, AgentAction agentAction) {
        // Get the task
        Task task = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.TASK, Task.class);

        // Check if the task is null and raise exception if so
        if(task == null) throw new IllegalArgumentException("Task is null");

        if (task.getDestination() == null)
            generateMoveDestinationCoordinate(agentState, task);


        // If still no destination
        if (task.getDestination() == null) {
            ActionUtils.moveRandomly(agentState, agentAction);
        }

        // If agent just entered this behavior and the destination is right next to it -> Skip this turn
        else if (GeneralUtils.hasReachedPosition(agentState, task.getDestination().getCoordinate())) {
            agentAction.skip();
        }

        else {
            ActionUtils.moveToPosition(agentState, agentAction, task.getDestination().getCoordinate());
        }
    }

    private void generateMoveDestinationCoordinate(AgentState agentState, Task task) {
        int maxIter = 10;
        Random random = new Random();

        for (int i = 0; i < maxIter; i++) {
            int randX = random.nextInt(agentState.getPerception().getWidth());
            int randY = random.nextInt(agentState.getPerception().getWidth());

            CellPerception cellPerception = agentState.getPerception().getCellAt(randX,randY);

            if (cellPerception == null) continue;

            // Get the position of the cell
            int cellX = cellPerception.getX();
            int cellY = cellPerception.getY();
            Coordinate cellCoordinate = new Coordinate(cellX, cellY);

            // Don't put the packet in the same position as before
            if (task.getPacket().getCoordinate().equals(cellCoordinate)) continue;

            // Check if cell is free
            if (!cellPerception.isFree() && !cellPerception.containsAgent()) continue;

            // Check if path exists to destination
            ArrayList<Node> destinationPath = GraphUtils.performAStarSearch(agentState, cellCoordinate, false);

            if (destinationPath == null) continue;

            // Create destination at free cell
            task.setDestination(new Destination(cellCoordinate, 0));
            MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.TASK, task));
            return;
        }
    }
}