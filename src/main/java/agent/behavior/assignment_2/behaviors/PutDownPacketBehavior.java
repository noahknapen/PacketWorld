package agent.behavior.assignment_2.behaviors;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.Coordinate;
import util.assignments.general.ActionUtils;
import util.assignments.general.GeneralUtils;
import util.assignments.graph.GraphUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.Destination;
import util.assignments.task.Task;

/**
 * A behavior where the agent puts down a packet
 */
public class PutDownPacketBehavior extends Behavior {

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        // Communicate the charging stations with all the other agents
        GeneralUtils.handleChargingStationsCommunication(agentState, agentCommunication);

        // Communicate the destination locations with agents in perception
        // GeneralUtils.handleDestinationsCommunication(agentState, agentCommunication);

        // Communicate the graph with agents in perception
        GeneralUtils.handleGraphCommunication(agentState, agentCommunication);

    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        // Build the graph
        GraphUtils.build(agentState);

        // Put down the packet
        handlePutDown(agentState, agentAction);
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * A function to let the agent put down the packet
     * 
     * @param agentState The current state of the agent
     * @param agentAction Perform an action with the agent
     */
    private void handlePutDown(AgentState agentState, AgentAction agentAction)  {
        // Get the task
        Task task = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.TASK, Task.class);

        // Get the coordinate of the destination
        Destination destination= task.getDestination();
        Coordinate destinationCoordinate = destination.getCoordinate();

        // Put down the packet
        ActionUtils.putDownPacket(agentState, agentAction, destinationCoordinate);
    }
}