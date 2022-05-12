package agent.behavior.assignment_2.changes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


import agent.AgentState;
import agent.behavior.BehaviorChange;
import environment.Perception;
import util.assignments.general.GeneralUtils;
import util.assignments.graph.Graph;
import util.assignments.graph.GraphUtils;
import util.assignments.graph.Node;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.Destination;
import util.assignments.targets.Packet;
import util.assignments.task.Task;

/**
 * A behavior change class that checks if a new task cannot be defined
 */
public class TaskDefinitionNotPossible extends BehaviorChange{

    private boolean taskDefinitionNotPossible = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        AgentState agentState = this.getAgentState();

        // Check if the task definition is not possible
        taskDefinitionNotPossible = checkNoTaskDefinition(agentState);
    }

    @Override
    public boolean isSatisfied() {
        return taskDefinitionNotPossible;
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Check if no task can be defined
     *
     * @param agentState The current state of the agent
     *
     * @return True if the task definition is not possible, otherwise false
     */
    private boolean checkNoTaskDefinition(AgentState agentState) {
        // Get the discovered packets
        ArrayList<Packet> discoveredPackets = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class).getTargets(Packet.class);

        // Check if no packets were discovered yet and return true if so
        if(discoveredPackets.isEmpty()) return true;

        // Get the discovered destinations
        ArrayList<Destination> discoveredDestinations = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class).getTargets(Destination.class);

        // Check if no destinations were discovered yet and return true if so
        if(discoveredDestinations.isEmpty()) return true;

        // Loop over the sorted discovered packets
        for(int i = 0; i < discoveredPackets.size(); i++) {

            // Get a candidate packet
            Packet candidatePacket = discoveredPackets.get(i);

            // Get the color of the candidate packet
            Color candidatePacketColor = candidatePacket.getColor();

            if (agentState.getColor().isPresent() && agentState.getColor().get().getRGB() != candidatePacketColor.getRGB()) continue;

            // Check if path exists to packet
            ArrayList<Node> packetPath = GraphUtils.performAStarSearch(agentState, candidatePacket.getCoordinate(), false);

            if (packetPath == null) continue;


            // Loop over the discovered destinations
            for (Destination candidateDestination : discoveredDestinations) {
                // Get the color of the candidate destination
                Color candidateDestinationColor = candidateDestination.getColor();

                // Check if the colors correspond
                if (!candidatePacketColor.equals(candidateDestinationColor)) continue;

                // If the agent hasn't got enough energy to work on it, it will not start the work
                if (!GeneralUtils.hasEnoughBatteryToCompleteTask(agentState, candidatePacket, candidateDestination)) continue;

                // Check if path exists to destination
                ArrayList<Node> destinationPath = GraphUtils.performAStarSearch(agentState, candidateDestination.getCoordinate(), false);

                if (destinationPath == null) continue;


                // Task is possible if reached here
                return false;

            }
        }

        return true;
    }
}