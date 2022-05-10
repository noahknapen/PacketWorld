package agent.behavior.assignment_2.changes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Map;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import environment.Coordinate;
import environment.Perception;
import util.assignments.comparators.PacketComparator;
import util.assignments.general.GeneralUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.Destination;
import util.assignments.targets.Packet;
import util.assignments.task.Task;
import util.assignments.task.TaskType;

/**
 * A behavior change class that checks if the packet towards which the agent is moving 
 * hasn't already been picked up by another agent
 */
public class NewTaskAfterTryingPickUp extends BehaviorChange{

    private boolean newTaskAfterTryingPickUp = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        AgentState agentState = this.getAgentState();
        
        if (agentState.hasCarry())
            // If the packet is not already handled by another agent, an alternate task definition is not necessary
            newTaskAfterTryingPickUp = false;
        else
        {
            newTaskAfterTryingPickUp = checkAlternateTaskDefintion(agentState);
        }
    }

    @Override
    public boolean isSatisfied() {
        return newTaskAfterTryingPickUp;
    }

    /////////////
    // METHODS //
    /////////////

    private boolean checkAlternateTaskDefintion(AgentState agentState) {
        // Get the discovered packets and discovered destinations
        ArrayList<Packet> discoveredPackets = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_PACKETS, Packet.class);
        ArrayList<Destination> discoveredDestinations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_DESTINATIONS, Destination.class);

        // Get the packet the agent is currently moving to
        Coordinate packetCoordinate = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.TASK, Task.class).getPacket().getCoordinate();

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

            if (candidatePacket.getCoordinate().equals(packetCoordinate))
                continue;

            // Get the color of the candidate packet
            Color candidatePacketColor = candidatePacket.getColor();

            // Loop over the discovered destinations
            for (Destination candidateDestination : discoveredDestinations) {
                // Get the color of the candidate destination
                Color candidateDestinationColor = candidateDestination.getColor();

                // Check if the colors correspond
                if (!candidatePacketColor.equals(candidateDestinationColor)) continue;

                // If the agent hasn't got enough energy to work on it, it will not start the work
                if (!GeneralUtils.hasEnoughBatteryToCompleteTask(agentState, candidatePacket, candidateDestination)) continue;


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
            Task task = new Task(TaskType.MOVE_TO_PACKET, candidatePacket, closestDestination);

            // Remove the packet at packet index from the discovered packets. No idea why this error exist because line 104 changes packageIndex
            if (packageIndex != Integer.MAX_VALUE) discoveredPackets.remove(packageIndex);

            // Update the memory
            MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.TASK, task, MemoryKeys.DISCOVERED_PACKETS, discoveredPackets));

            return true;
        }

        return false;
    }
}
