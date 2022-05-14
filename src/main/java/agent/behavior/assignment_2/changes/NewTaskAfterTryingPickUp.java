package agent.behavior.assignment_2.changes;

import java.awt.Color;
import java.util.*;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import environment.Perception;
import util.assignments.comparators.PacketComparator;
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
 * A behavior change class that checks if a new task can be defined
 */
public class NewTaskAfterTryingPickUp extends BehaviorChange{

    private boolean newTaskAfterTryingPickUp = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        AgentState agentState = this.getAgentState();

        // Handle the possible task definition
        if (agentState.hasCarry())
            newTaskAfterTryingPickUp = false;
        else
            newTaskAfterTryingPickUp = checkTaskDefinition(agentState);
    }

    @Override
    public boolean isSatisfied() {
        return newTaskAfterTryingPickUp;
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Check if a task can be defined and do so if yes
     *
     * @param agentState The current state of the agent
     * @return True if the task definition is possible and was done, otherwise false
     *
     */
    private boolean checkTaskDefinition(AgentState agentState) {

        // Get priority tasks
        ArrayList<Task> priorityTasks = MemoryUtils.getListFromMemory(agentState, MemoryKeys.PRIORITY_TASKS, Task.class);

        // Check if one of the priority tasks can be done
        for (Task task : priorityTasks) {
            if (!task.isHandled() && task.conditionsSatisfied(agentState)) {
                task.setHandled(true);
                MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.TASK, task, MemoryKeys.PRIORITY_TASKS, priorityTasks));
                return true;
            }
        }

        // Get the discovered packets and discovered destinations

        // Only get packets of same color here
        ArrayList<Packet> discoveredPackets = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class).getTargets(Packet.class);
        ArrayList<Destination> discoveredDestinations = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class).getTargets(Destination.class);

        // Sort the discovered packets
        PacketComparator packetComparator = new PacketComparator(agentState, discoveredDestinations);
        discoveredPackets.sort(packetComparator);

        // Some variables used to find the closest destination
        int packageIndex = Integer.MAX_VALUE;
        double bestDistance = Integer.MAX_VALUE;
        Destination closestDestination = null;

        // Loop over the sorted discovered packets
        for(int i = 0; i < discoveredPackets.size(); i++) {

            // Get a candidate packet
            Packet candidatePacket = discoveredPackets.get(i);


            // Get the color of the candidate packet
            Color candidatePacketColor = candidatePacket.getColor();

            if (agentState.getColor().isPresent() && agentState.getColor().get().getRGB() != candidatePacketColor.getRGB()) continue;

            // Check if path exists to packet
            ArrayList<Node> packetPath = GraphUtils.performAStarSearch(agentState, candidatePacket.getCoordinate(), true);

            if (packetPath == null) continue;

            // Checks if path is blocked and if so continue with next
            if (GraphUtils.checkIfBlocked(agentState, packetPath)) continue;


            // Loop over the discovered destinations
            for (Destination candidateDestination : discoveredDestinations) {
                // Get the color of the candidate destination
                Color candidateDestinationColor = candidateDestination.getColor();

                // Check if the colors correspond
                if (!candidatePacketColor.equals(candidateDestinationColor)) continue;

                // If the agent hasn't got enough energy to work on it, it will not start the work
                if (!GeneralUtils.hasEnoughBatteryToCompleteTask(agentState, candidatePacket, candidateDestination)) continue;

                // Check if path exists to destination
                ArrayList<Node> destinationPath = GraphUtils.performAStarSearch(agentState, candidateDestination.getCoordinate(), true);

                if (destinationPath == null) continue;

                // Checks if path is blocked and if so continue with next
                if (GraphUtils.checkIfBlocked(agentState, destinationPath)) continue;


                // Remove the packet from the discovered packets
                candidatePacket = discoveredPackets.get(i);

                // Calculate the distance to the destination from the packet
                double tempDistance = Perception.distance(
                        candidatePacket.getCoordinate().getX(),
                        candidatePacket.getCoordinate().getY(),
                        candidateDestination.getCoordinate().getX(),
                        candidateDestination.getCoordinate().getY()
                );

                // Ensure that the distance for this destination is closer than the previous one
                if (tempDistance > bestDistance) continue;

                // Update the variables
                bestDistance = tempDistance;
                closestDestination = candidateDestination;
                packageIndex = i;
            }

            // If no destination is found, continue with a next packet
            if (closestDestination == null) continue;

            // Define the task
            Task task = new Task(candidatePacket, closestDestination);

            // Remove the packet at packet index from the discovered packets. No idea why this error exist because line 104 changes packageIndex
            if (packageIndex != Integer.MAX_VALUE) discoveredPackets.remove(packageIndex);

            Graph graph = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class);
            // graph.getNode(candidatePacket.getCoordinate()).get().setTarget(Optional.empty(), false);

            // Update the memory
            MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.TASK, task, MemoryKeys.GRAPH, graph));

            return true;
        }

        return false;
    }
}