package agent.behavior.assignment_2.behaviors;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;
import util.assignments.general.ActionUtils;
import util.assignments.general.GeneralUtils;
import util.assignments.graph.GraphUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.Packet;
import util.assignments.task.Task;

/**
 * A behavior where the agent picks up a packet
 */
public class PickUpPacketBehavior extends Behavior {

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
        // Pick up the packet
        handlePickUp(agentState, agentAction);

        // Build the graph
        GraphUtils.build(agentState);
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * A function to let the agent pick up the packet
     * 
     * @param agentState The current state of the agent
     * @param agentAction Perform an action with the agent
     */
    private void handlePickUp(AgentState agentState, AgentAction agentAction) {
        // Get the task
        Task task = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.TASK, Task.class);

        // Get the coordinate of the packet
        Packet packet= task.getPacket();
        Coordinate packetCoordinate = packet.getCoordinate();
        Perception agentPerception = agentState.getPerception();
        CellPerception packetCellPerception = agentPerception.getCellPerceptionOnAbsPos(packetCoordinate.getX(), packetCoordinate.getY());

        // Pick up the packet
        if (packetCellPerception.containsPacket())
            ActionUtils.pickUpPacket(agentState, agentAction, packetCoordinate);
        else
            agentAction.skip();

        // Remove the packet from the discovered packets
        // graph.getNode(task.getPacket().getCoordinate()).get().setTarget(Optional.empty());
        // MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.GRAPH, graph));
    }
}